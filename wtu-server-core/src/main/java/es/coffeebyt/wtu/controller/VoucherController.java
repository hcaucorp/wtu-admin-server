package es.coffeebyt.wtu.controller;

import static es.coffeebyt.wtu.metrics.ActuatorConfig.COUNTER_REDEMPTION_FAILURE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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

import es.coffeebyt.wtu.api.ApiErrorValues;
import es.coffeebyt.wtu.exception.IllegalOperationException;
import es.coffeebyt.wtu.exception.MaltaCardException;
import es.coffeebyt.wtu.notifications.NotificationService;
import es.coffeebyt.wtu.security.EnumerationProtectionService;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherInfoResponse;
import es.coffeebyt.wtu.voucher.VoucherService;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import es.coffeebyt.wtu.voucher.impl.RedemptionResponse;
import es.coffeebyt.wtu.voucher.impl.VoucherGenerationSpec;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@Slf4j
public class VoucherController {

    private final NotificationService notificationService;
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
    public ResponseEntity<?> generateVouchers(@RequestBody @Valid VoucherGenerationSpec details) {
        voucherService.save(voucherService.generateVouchers(details));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/vouchers"))
                .build();
    }

    @PostMapping("/redeem")
    public RedemptionResponse redeemVoucher(@RequestBody @Valid RedemptionRequest detail) {

        // handle API test values, testing in production? 😅
        ApiErrorValues.handleGiftCardRedemption(detail);

        try {

            // can we proceed?
            enumerationProtectionService.checkIfBlocked(request);

            // try to redeem
            RedemptionResponse response = voucherService.redeemVoucher(detail);

            // log is for us in case we have to check transactions?
            log.info("Redeemed voucher: {} to address: {} in tx: {}", detail.getVoucherCode(), detail.getDestinationAddress(), response.getTransactionId());

            // whitelist current IP

            return response;
        } catch (MaltaCardException e) {
            log.error("Failed redemption ({}) with exception {}, message: {}", detail.toString(), e.getClass().getSimpleName(), e.getMessage());

            enumerationProtectionService.failed(request);
            meterRegistry.counter(COUNTER_REDEMPTION_FAILURE).increment();

            // in case of Malta event error we can feed back more info
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed redemption ({}) with exception {}, message: {}", detail.toString(), e.getClass().getSimpleName(), e.getMessage());

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

    @GetMapping("/{voucherCode}")
    public ResponseEntity<VoucherInfoResponse> voucherInfo(@PathVariable String voucherCode) {

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
