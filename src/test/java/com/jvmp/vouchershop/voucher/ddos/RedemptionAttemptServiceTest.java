package com.jvmp.vouchershop.voucher.ddos;

import org.junit.Before;
import org.junit.Test;

import static com.jvmp.vouchershop.utils.RandomUtils.randomString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RedemptionAttemptServiceTest {

    private RedemptionAttemptService subject;

    @Before
    public void setUp() {
        subject = new RedemptionAttemptService();
    }

    @Test
    public void testBlockingAndReset() {
        String ip = randomString(); // random ip?

        for (int i = 0; i < 9; i++) {
            assertFalse(subject.isBlocked(ip));
            subject.failed(ip);
        }

        assertFalse(subject.isBlocked(ip));

        subject.succeeded(ip); // should reset counter

        for (int i = 0; i < 10; i++) {
            assertFalse(subject.isBlocked(ip));
            subject.failed(ip);
        }

        assertTrue(subject.isBlocked(ip));
    }
}