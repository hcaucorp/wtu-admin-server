package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.voucher.VoucherService;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class VoucherControllerTest {

    @Mock
    private VoucherService service;

    private VoucherController controller;

    @Before
    public void setUp() {
        controller
                = new VoucherController(service);
    }

    @Test
    public void getAllVouchers() {
        controller.getAllVouchers();
        verify(service, times(1)).findAll();
    }

    @Test
    public void deleteVoucherById() {
        long id = RandomUtils.nextLong(1, 1_000);
        controller.deleteVoucherById(id);
        fail("not implemented");
    }

    @Test
    public void generateVouchers() {
        fail("not implemented");
    }
}