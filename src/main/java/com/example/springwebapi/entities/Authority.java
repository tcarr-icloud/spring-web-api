package com.example.springwebapi.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Authority {
    @Id
    private String authority;

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
