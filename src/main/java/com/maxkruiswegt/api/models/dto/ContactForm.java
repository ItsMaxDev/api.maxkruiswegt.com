package com.maxkruiswegt.api.models.dto;

import lombok.Data;

@Data
public class ContactForm {
    private String locale;
    private String name;
    private String email;
    private String subject;
    private String message;
}