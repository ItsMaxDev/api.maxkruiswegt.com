package com.maxkruiswegt.api.security;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Objects;

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
        InputStream keyStoreInputStream;

        // Check if the application is running from a JAR file
        if (Objects.requireNonNull(JwtKeyProvider.class.getResource("")).toString().startsWith("jar:")) {
            // Get the path of the JAR file
            String jarPath = new File(JwtKeyProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

            // Build the path of the store.p12 file
            Path keyStorePath = Paths.get(jarPath, keystore);

            // Load the file
            keyStoreInputStream = Files.newInputStream(keyStorePath);
        } else {
            // Load the file from the classpath
            keyStoreInputStream = new ClassPathResource(keystore).getInputStream();
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, password.toCharArray());

            privateKey = keyStore.getKey(alias, password.toCharArray());
            publicKey = keyStore.getCertificate(alias).getPublicKey();
        } finally {
            keyStoreInputStream.close();
        }
    }
}
