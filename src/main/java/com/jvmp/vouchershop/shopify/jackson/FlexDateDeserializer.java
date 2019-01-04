package com.jvmp.vouchershop.shopify.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * @see <a href="http://www.baeldung.com/jackson-serialize-dates">Jackon Custom date serialization</a>
 * @see <a href="http://www.leveluplunch.com/java/tutorials/033-custom-jackson-date-deserializer/">Jackon Custom date serialization</a>
 */
public final class FlexDateDeserializer extends JsonDeserializer<Date>
{
    @Override
    public Date deserialize(final JsonParser parser, final DeserializationContext context) throws IOException
    {
        final String date = parser.getText();
        try
        {
            return FlexDateFormat.formatter.parse(date);
        }
        catch (final ParseException ex)
        {
            // Not worked, so let the default date serializer give it a try.
            return DateDeserializer.instance.deserialize(parser, context);
        }
    }
}