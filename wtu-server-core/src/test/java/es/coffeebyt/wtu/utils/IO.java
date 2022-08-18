package es.coffeebyt.wtu.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

@UtilityClass
public class IO {

    /**
     * Load text file content from the same package
     **/
    public static <T> String fromFile(Class<T> clazz, String fileName) throws IOException {
        return IOUtils.toString(clazz.getResourceAsStream(fileName), "UTF-8");
    }
}
