package com.jvmp.vouchershop.security;

import org.junit.Before;
import org.junit.Test;

import static com.jvmp.vouchershop.utils.RandomUtils.randomString;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RedemptionAttemptServiceTest {

    private RedemptionAttemptService subject;

    private int maxAttempts;

    @Before
    public void setUp() {
        maxAttempts = nextInt(10, 20);

        subject = new RedemptionAttemptService(nextInt(10, 20), "MILLISECONDS", maxAttempts);
    }

    @Test
    public void testBlockingAndReset() {
        String ip = randomString(); // random ip?

        for (int i = 0; i < maxAttempts - 1; i++) {
            assertFalse(subject.isBlocked(ip));
            subject.failed(ip);
        }

        assertFalse(subject.isBlocked(ip));

        subject.succeeded(ip); // should reset counter

        for (int i = 0; i < maxAttempts; i++) {
            assertFalse(subject.isBlocked(ip));
            subject.failed(ip);
        }

        assertTrue(subject.isBlocked(ip));
    }
}