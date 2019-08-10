package es.coffeebyt.wtu.controller;

import static es.coffeebyt.wtu.metrics.ActuatorConfig.COUNTER_REDEMPTION_FAILURE;
import static es.coffeebyt.wtu.voucher.listeners.OnePerCustomerForMaltaPromotion.MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import es.coffeebyt.wtu.api.ApiError;
import es.coffeebyt.wtu.api.ApiTestingConstants;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.exception.MaltaCardException;
import es.coffeebyt.wtu.security.EnumerationProtectionService;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherInfoResponse;
import es.coffeebyt.wtu.voucher.VoucherService;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.impl.RedemptionResponse;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@Slf4j
public class VoucherController {

    private final VoucherService voucherService;
    private final EnumerationProtectionService enumerationProtectionService;
    private final MeterRegistry meterRegistry;

    @Autowired
    private HttpServletRequest request;

    @GetMapping
    public List<Voucher> getAllVouchers(
            @RequestParam(required = false, defaultValue = "false") boolean showRedeemed,
            @RequestParam(required = false) String sku) {
        return voucherService.findBy(showRedeemed, sku);
    }

    @DeleteMapping("/{sku}")
    public void deleteVoucherBySku(@PathVariable String sku) {
        voucherService.deleteBySku(sku);
    }

    @PostMapping
    public ResponseEntity<Object> generateVouchers(@RequestBody @Valid VoucherGenerationSpec details) {
        voucherService.save(voucherService.generateVouchers(details));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/vouchers"))
                .build();
    }

    @ApiOperation(
            value = "Redeem voucher code to address provided in the request",
            notes = "A single voucher can be \"used\" (i.e. redeemed, activated) ONLY once. After successful request, all subsequent attempts will return error. " +
                    "You can perform as many correct requests and as fast as you can. However, too many invalid requests may put you on a blacklist. " +
                    "API may not be able to distinct from your wallet address, if destination wallet matches your voucher currency. It is possible to send " +
                    "us voucher-code starting with \"wtubtc-\" and destination-address of a wallet with BCH currency on it. Because some addresses are valid " +
                    "in both blockchains, making that kind of request would trigger transaction on BTC blockchain, and, unfortunately, provided BCH address " +
                    "would never receive anything from us. It is up to the client to validate if your wallet's currency matches voucher's currency. Please " +
                    "use the voucher prefix to make sure you're requesting correct redemption."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 400,
                    message = "Bad request.",
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"timestamp\":\"2019-08-09T13:33:07.482+0000\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"No message available\",\"path\":\"/api/vouchers/redeem\"}")),
                    response = ApiError.class
            ),
            @ApiResponse(
                    code = 400,
                    message = MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER,
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"timestamp\":\"2019-08-09T13:33:07.482+0000\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"" + MALTA_VOUCHER_REDEMPTION_ERROR_ONE_PER_CUSTOMER + "\",\"path\":\"/api/vouchers/redeem\"}")),
                    response = ApiError.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "IP is blocked.",
                    examples = @Example(
                            @ExampleProperty(
                                    mediaType = "application/json",
                                    value = "{\"timestamp\":\"2019-08-09T13:33:07.482+0000\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"IP is blocked: <your ip here>\",\"path\":\"/api/vouchers/redeem\"}")
                    ),
                    response = ApiError.class
            )
    })
    @PostMapping(value = "/redeem", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public RedemptionResponse redeemVoucher(
            @ApiParam(
                    value = "Request body containing code from gift card and destination address of a wallet where funds will be transferred.",
                    required = true,
                    example = "{\n"
                            + "  \"destinationAddress\": \"1PzePkRhoFN4DPsmKT4zobVd2EQqkeevtW\",\n"
                            + "  \"voucherCode\": \"wtubtc-dcb6a8a1-b9c0-4f43-81b4-f26274db772f\"\n"
                            + "}"

            ) @RequestBody @Valid RedemptionRequest detail) {

        // handle API test values, testing in production? 😅
        ApiTestingConstants.handleApiErrors(detail);

        try {

            // can we proceed?
            enumerationProtectionService.checkIfBlocked(request);

            // try to redeem
            RedemptionResponse response = voucherService.redeemVoucher(detail);

            // log is for us in case we have to check transactions?
            log.info("Redeemed voucher: {} to address: {} in tx: {}", detail.getVoucherCode(),
                    detail.getDestinationAddress(), response.getTransactionId());

            // whitelist current IP

            return response;
        } catch (MaltaCardException e) {
            log.error("Failed redemption ({}) with exception {}, message: {}", detail.toString(),
                    e.getClass().getSimpleName(), e.getMessage());

            enumerationProtectionService.failed(request);
            meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).increment();

            // in case of Malta event error we can feed back more info
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed redemption ({}) with exception {}, message: {}", detail.toString(),
                    e.getClass().getSimpleName(), e.getMessage());

            enumerationProtectionService.failed(request);
            meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).increment();

            // intentional provide no info about the problem
            throw new IllegalOperationException();
        }
    }

    @PostMapping("/{sku}/publish")
    public void publishVouchers(@PathVariable String sku) {
        voucherService.publishBySku(sku);
    }

    @PostMapping("/{sku}/unpublish")
    public void unPublishVouchers(@PathVariable String sku) {
        voucherService.unPublishBySku(sku);
    }

    @ApiOperation(
            value = "Maybe return some information about voucher code provided.",
            notes = "Provides status information about given voucher. It expects voucher code as the last path parameter. Replace {voucherCode} with a voucher code you want to verify.\n" +
                    "You can perform as many correct requests and as fast as you can. However, too many invalid requests may put you on a blacklist.\n" +
                    "Requesting info about non existent voucher code is an invalid request."
    )
    @ApiResponses({
            @ApiResponse(
                    code = 400,
                    message = "Bad request.",
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"timestamp\":\"2019-08-09T14:23:47.851+0000\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Response status 400\",\"path\":\"/api/vouchers/i_dont_exist\"}")),
                    response = ApiError.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "IP is blocked.",
                    examples = @Example(@ExampleProperty(mediaType = "application/json", value = "{\"timestamp\":\"2019-08-09T14:30:13.432+0000\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"IP is blocked: <your ip here>\",\"path\":\"/api/vouchers/i_dont_exist\"}")),
                    response = ApiError.class
            )
    })
    @GetMapping(
            value = "/{voucherCode}",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VoucherInfoResponse> voucherInfo(
            @ApiParam(value = "Code from gift card (a.k.a. voucher code)", required = true) @PathVariable String voucherCode) {

        enumerationProtectionService.checkIfBlocked(request);

        Optional<Voucher> optionalVoucher = voucherService.findByCode(voucherCode);

        return optionalVoucher
                .filter(Voucher::isPublished) // must be published to return any info about it
                .map(VoucherInfoResponse::from)
                .map(response -> ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response))
                .orElseThrow(() -> {
                    enumerationProtectionService.failed(request);
                    return new ResponseStatusException(BAD_REQUEST);
                });
    }
}
