package com.maxkruiswegt.api.security;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;

@Configuration
public class JwtKeyProvider {

    private final String keystore;

    private final String alias;

    private final String password;

    @Getter
    private Key privateKey;

    @Getter
    private PublicKey publicKey;

    public JwtKeyProvider() {
        Dotenv dotenv = Dotenv.load();
        keystore = dotenv.get("JWT_KEYSTORE");
        alias = dotenv.get("JWT_ALIAS");
        password = dotenv.get("JWT_PASSWORD");
    }

    @PostConstruct
    public void init() throws Exception {
        // Get the current working directory
        String currentDirectory = System.getProperty("user.dir");

        // Build the path of the store.p12 file
        Path keyStorePath = Paths.get(currentDirectory, keystore);

        try (InputStream keyStoreInputStream = Files.newInputStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, password.toCharArray());

            privateKey = keyStore.getKey(alias, password.toCharArray());
            publicKey = keyStore.getCertificate(alias).getPublicKey();
        }
    }
}
