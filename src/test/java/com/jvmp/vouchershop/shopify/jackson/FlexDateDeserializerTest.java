package com.jvmp.vouchershop.shopify.jackson;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FlexDateDeserializerTest {

    private FlexDateDeserializer flexDateDeserializer;

    @Mock
    private JsonParser parser;

    @Before
    public void setUp() {
        flexDateDeserializer = new FlexDateDeserializer();
    }

    @Test
    public void deserialize() throws IOException {
        String toParse = "2018-12-30T12:06:56-00:00";
        when(parser.getText()).thenReturn(toParse);

        Date deserialized = flexDateDeserializer.deserialize(parser, null);
        Date result = Date.from(LocalDateTime.of(2018, 12, 30, 12, 6, 56).toInstant(ZoneOffset.UTC));

        assertEquals(result, deserialized);
    }
}