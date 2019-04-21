package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.wallet.WalletService;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static com.jvmp.vouchershop.utils.RandomUtils.randomVouchers;
import static com.jvmp.vouchershop.utils.RandomUtils.randomWallet;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = {"it", "aws-it"})
public class AwsSesIT {

    @Autowired
    private EmailService emailService;

    @MockBean
    private WalletService walletService;

    @Test
    @Ignore(value = "This is a special kind of test meant to be run manually only to check AWS SES configuration")
    public void sendVouchers() {
        String name = "Tadzio";
        String email = "hubertinio@me.com";
        when(walletService.findById(any())).thenReturn(Optional.of(randomWallet(TestNet3Params.get())));

        Set<Voucher> fewVouchers = randomVouchers(nextInt(3, 6));

        emailService.sendVouchers(fewVouchers, new Order()
                .withOrderNumber(nextLong())
                .withCustomer(new Customer()
                        .withFirstName(name)
                        .withEmail(email)));
    }
}
