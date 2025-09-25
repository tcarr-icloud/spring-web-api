package com.example.springwebapi.dtos;

public class UserDTO {
    public String username;
    public String password;
    public boolean enabled;
    public String[] authorities;

    public UserDTO(String username, String password, boolean enabled, String[] authorities) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }
}
