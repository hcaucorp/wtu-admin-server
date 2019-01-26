package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.system.PropertyNames;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
public class VoucherController {

    private final NotificationService notificationService;
    private final VoucherService voucherService;

    @Value(PropertyNames.AWS_SNS_TOPIC_REDEMPTIONS)
    private String redemptionsTopic;

    @GetMapping("/vouchers")
    public List<Voucher> getAllVouchers() {
        return voucherService.findAll();
    }

    @DeleteMapping("/vouchers/{sku}")
    public ResponseEntity<?> deleteVoucherBySku(@PathVariable String sku) {
        voucherService.deleteBySku(sku);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/vouchers")
    public ResponseEntity<?> generateVouchers(@RequestBody @Valid VoucherGenerationDetails details) {
        voucherService.save(voucherService.generateVouchers(details));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/vouchers"))
                .build();
    }

    @PostMapping("/vouchers/redeem")
    public RedemptionResponse redeemVoucher(@RequestBody @Valid RedemptionRequest detail) {
        try {
            RedemptionResponse response = voucherService.redeemVoucher(detail);
            notificationService.push("Redeemed " + detail.getVoucherCode(), redemptionsTopic);
            return response;
        } catch (Exception e) {
            notificationService.push("Redemption attempted but failed with exception: " + e.getMessage(), redemptionsTopic);
            throw e;
        }
    }
}
