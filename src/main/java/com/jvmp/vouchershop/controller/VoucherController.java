package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.security.EnumerationProtectionService;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherInfoResponse;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@Slf4j
public class VoucherController {

    private final NotificationService notificationService;
    private final VoucherService voucherService;
    private final EnumerationProtectionService enumerationProtectionService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping
    public List<Voucher> getAllVouchers() {
        return voucherService.findAll();
    }

    @DeleteMapping("/{sku}")
    public void deleteVoucherBySku(@PathVariable String sku) {
        voucherService.deleteBySku(sku);
    }

    @PostMapping
    public ResponseEntity<?> generateVouchers(@RequestBody @Valid VoucherGenerationDetails details) {
        voucherService.save(voucherService.generateVouchers(details));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/vouchers"))
                .build();
    }

    @PostMapping("/redeem")
    public RedemptionResponse redeemVoucher(@RequestBody @Valid RedemptionRequest detail) {

        enumerationProtectionService.checkIfBlocked(request);

        try {
            RedemptionResponse response = voucherService.redeemVoucher(detail);
            notificationService.pushRedemptionNotification("Redeemed " + detail.getVoucherCode());
            enumerationProtectionService.succeeded(request);
            return response;
        } catch (VoucherNotFoundException e) {
            String message = format("Tried to redeem absent voucher: %s to a wallet: %s", detail.getVoucherCode(), detail.getDestinationAddress());
            log.warn(message);
            notificationService.pushRedemptionNotification(message);
            enumerationProtectionService.failed(request);
            throw e;
        } catch (Exception e) {
            String message = format("Failed redemption with exception %s, message: %s", e.getClass().getSimpleName(), e.getMessage());
            log.error(message);
            notificationService.pushRedemptionNotification(message);
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
                .filter(Voucher::isSold) // must be sold to return any info about it
                .map(VoucherInfoResponse::from)
                .map(response -> ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response))
                .orElseGet(() -> {
                    enumerationProtectionService.failed(request);
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .build();
                });
    }
}
