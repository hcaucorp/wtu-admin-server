package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.voucher.VoucherGenerationSpec;
import com.jvmp.vouchershop.voucher.VoucherService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin
public class VoucherController {

    private VoucherService voucherService;

    @GetMapping("/vouchers")
    public List<Voucher> getAllVouchers() {
        return voucherService.findAll();
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<?> deleteVoucherById(@PathVariable long id) {
        voucherService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/vouchers")
    public ResponseEntity<?> generateVouchers(@RequestBody VoucherGenerationSpec details) {
        voucherService.save(voucherService.generateVouchers(details));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/vouchers"))
                .build();
    }
}
