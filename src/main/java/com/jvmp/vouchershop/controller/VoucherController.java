package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.voucher.ddos.RedemptionAttemptService;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final RedemptionAttemptService redemptionAttemptService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/vouchers")
    public List<Voucher> getAllVouchers() {
        return voucherService.findAll();
    }

    @DeleteMapping("/vouchers/{sku}")
    public void deleteVoucherBySku(@PathVariable String sku) {
        voucherService.deleteBySku(sku);
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
        String ip = getClientIP();

        if (redemptionAttemptService.isBlocked(ip)) {
            redemptionAttemptService.failed(ip);
            throw new IllegalOperationException();
        }

        try {
            RedemptionResponse response = voucherService.redeemVoucher(detail);
            notificationService.pushRedemptionNotification("Redeemed " + detail.getVoucherCode());
            redemptionAttemptService.succeeded(ip);

            return response;
        } catch (VoucherNotFoundException e) {
            log.warn("Tried to redeem absent voucher: {} to a wallet: {}", detail.getVoucherCode(), detail.getDestinationAddress());
            notificationService.pushRedemptionNotification("Tried to redeem absent voucher: " + detail.getVoucherCode() +
                    " to a wallet: " + detail.getDestinationAddress());
            redemptionAttemptService.failed(ip);
            throw e;
        } catch (Exception e) {
            log.error("Redemption attempted but failed with exception: {}", e.getMessage());
            notificationService.pushRedemptionNotification("Redemption attempted but failed with exception: " + e.getMessage());
            throw new IllegalOperationException();
        }
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
