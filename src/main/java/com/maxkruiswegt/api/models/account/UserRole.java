package com.maxkruiswegt.api.models.account;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    REGULAR, ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
