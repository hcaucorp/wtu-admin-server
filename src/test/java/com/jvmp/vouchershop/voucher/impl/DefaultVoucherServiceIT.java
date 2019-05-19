package com.jvmp.vouchershop.voucher.impl;


import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.repository.VoucherRepository;
import com.jvmp.vouchershop.system.DatabaseConfig;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.jvmp.vouchershop.utils.RandomUtils.randomSku;
import static com.jvmp.vouchershop.utils.RandomUtils.randomVouchers;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                Application.class,
                DatabaseConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockBeans({
        @MockBean(NotificationService.class)
})
public class DefaultVoucherServiceIT {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private DefaultVoucherService subject;

    @After
    public void tearDown() {
        voucherRepository.deleteAll();
    }

    @Test
    public void unPublishVouchersBySku_shouldNotUnPublishSoldVouchers() {

        int allCount = nextInt(10, 20);
        int soldCount = nextInt(1, allCount - 1);

        String sku = randomSku();
        List<Voucher> vouchers = randomVouchers(allCount).stream()
                .map(voucher -> voucher
                        .withSku(sku)
                        .withPublished(true)
                )
                .collect(toList());

        for (int i = 0; i < soldCount; i++) {
            vouchers.get(i).setSold(true);
        }
        voucherRepository.saveAll(vouchers);

        subject.unPublishBySku(sku);

        List<Voucher> expected = vouchers.stream()
                .filter(voucher -> !voucher.isSold())
                .map(voucher -> voucher.withPublished(false))
                .collect(toList());

        List<Voucher> allVouchers = voucherRepository.findAll();
        assertEquals(allCount, allVouchers.size());

        List<Voucher> actual = allVouchers.stream()
                .filter(voucher -> !voucher.isSold())
                .collect(toList());
        assertEquals(allCount - soldCount, actual.size());

        expected.sort(comparing(Voucher::getCode));
        actual.sort(comparing(Voucher::getCode));

        assertEquals(expected, actual);
    }
}
