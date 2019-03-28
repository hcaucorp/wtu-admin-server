package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@Slf4j
public class VoucherController {

    private final NotificationService notificationService;
    private final VoucherService voucherService;

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
    public RedemptionResponse redeemVoucher(@RequestBody @Valid RedemptionRequest detail) throws VoucherNotFoundException {
        try {
            RedemptionResponse response = voucherService.redeemVoucher(detail);
            notificationService.pushRedemptionNotification("Redeemed " + detail.getVoucherCode());
            return response;
        } catch (VoucherNotFoundException e) {
            notificationService.pushRedemptionNotification("Tried to redeem absent voucher: " + detail.getVoucherCode() +
                    " to a wallet: " + detail.getDestinationAddress());

            throw e;
        } catch (Exception e) {
            notificationService.pushRedemptionNotification("Redemption attempted but failed with exception: " + e.getMessage());
            throw e;
        }
    }
}
