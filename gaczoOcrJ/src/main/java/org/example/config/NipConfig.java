package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class NipConfig {

    private static final Properties props = new Properties();

    private NipConfig(){}

    static {
        try (InputStream is = NipConfig.class
                .getClassLoader()
                .getResourceAsStream("companyId.properties")) {

            if (is == null) {
                throw new ExceptionInInitializerError("Brak companyId.properties");
            }
            props.load(is);

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Błąd ładowania configa");
        }
    }

    public static String nip(String company) {
        return props.getProperty(company);
    }

}
