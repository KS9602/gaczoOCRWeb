package org.example.config;

import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class NipConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = NipConfig.class
                .getClassLoader()
                .getResourceAsStream("companyId.properties")) {

            if (is == null) {
                throw new RuntimeException("Brak companyId.properties");
            }
            props.load(is);

        } catch (IOException e) {
            throw new RuntimeException("Błąd ładowania configa", e);
        }
    }

    public static String nip(String company) {
        return props.getProperty(company);
    }

}
