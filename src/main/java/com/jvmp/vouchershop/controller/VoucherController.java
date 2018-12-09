package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.exception.IllegalOperationException;
import com.jvmp.vouchershop.exception.ResourceNotFoundException;
import com.jvmp.vouchershop.voucher.VoucherRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class VoucherController {

    private VoucherRepository voucherRepository;

    @GetMapping("/vouchers")
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<?> deleteVoucherById(@PathVariable Long id) {
        return voucherRepository.findById(id)
                .map((Voucher v) -> {
                    if (v.published) throw new IllegalOperationException("Voucher has been published for sale and cannot be deleted");

                    voucherRepository.delete(v);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id " + id));
    }
}
