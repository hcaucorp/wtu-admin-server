package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherGenerationSpec;
import com.jvmp.vouchershop.voucher.VoucherService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        Voucher voucher = voucherService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id " + id));

        if (voucher.isPublished()) throw new IllegalOperationException("Voucher has been published for sale and cannot be deleted");

        voucherService.delete(voucher.getId());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/vouchers")
    public ResponseEntity<?> generateVouchers(@RequestBody VoucherGenerationSpec details) {
        List<Voucher> vouchers = voucherService.generateVouchers(details);
        voucherService.save(vouchers);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
