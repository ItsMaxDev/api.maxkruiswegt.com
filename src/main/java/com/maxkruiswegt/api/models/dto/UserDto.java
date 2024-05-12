package com.maxkruiswegt.api.models.dto;

import com.maxkruiswegt.api.models.account.User;
import com.maxkruiswegt.api.models.account.UserRole;
import lombok.Data;

@Data
public class UserDto {
    private int id;
    private String username;
    private String email;
    private UserRole role;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
