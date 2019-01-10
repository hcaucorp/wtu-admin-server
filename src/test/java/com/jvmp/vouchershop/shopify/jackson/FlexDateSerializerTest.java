package com.jvmp.vouchershop.shopify.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FlexDateSerializerTest {

    private FlexDateSerializer flexDateSerializer;

    @Mock
    private JsonGenerator generator;

    @Before
    public void setUp() {
        flexDateSerializer = new FlexDateSerializer();
    }

    @Test
    public void serialize() throws IOException {
        Date toSerialize = Date.from(LocalDateTime.of(118, 11, 30, 12, 6, 56).toInstant(ZoneOffset.UTC));
        String expected = "2018-12-30T12:06:56Z";

        flexDateSerializer.serialize(toSerialize, generator, null);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(generator, times(1)).writeString(captor.capture());

        assertEquals(expected, captor.getValue());
    }
}