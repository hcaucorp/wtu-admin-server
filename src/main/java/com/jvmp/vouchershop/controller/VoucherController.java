package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.domain.Voucher;
import com.jvmp.vouchershop.exception.*;
import com.jvmp.vouchershop.repository.VoucherRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class VoucherController {

    private VoucherRepository voucherRepository;

    @GetMapping("/vouchers")
    public Page<Voucher> getQuestions(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        return voucherRepository.findById(id)
                .map((Voucher v) -> {
                    if (v.published) throw new IllegalOperationException("Voucher has been published for sale and cannot be deleted");

                    voucherRepository.delete(v);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with id " + id));
    }
}
