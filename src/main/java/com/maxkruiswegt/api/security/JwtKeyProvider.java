package com.maxkruiswegt.api.security;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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
        ClassPathResource resource = new ClassPathResource(keystore);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(resource.getInputStream(), password.toCharArray());

        privateKey = keyStore.getKey(alias, password.toCharArray());
        publicKey = keyStore.getCertificate(alias).getPublicKey();
    }
}
