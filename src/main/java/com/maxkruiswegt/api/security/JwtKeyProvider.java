package com.maxkruiswegt.api.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;

@Configuration
public class JwtKeyProvider {

    @Value("${JWT_KEYSTORE}")
    private String keystore;

    @Value("${JWT_ALIAS}")
    private String alias;

    @Value("${JWT_PASSWORD}")
    private String password;

    @Getter
    private Key privateKey;

    @Getter
    private PublicKey publicKey;

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
