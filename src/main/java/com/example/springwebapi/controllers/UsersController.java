package com.example.springwebapi.controllers;

import com.example.springwebapi.dtos.UserDTO;
import com.example.springwebapi.repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UsersController {
    private static final String MASKED_PASSWORD = "********";
    private final UserRepository userRepository;

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    List<UserDTO> getUsers() {
        List<UserDTO> userDTOS = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            String[] authorityNames = user.getAuthorities().stream().map(authority -> authority.getAuthority().substring(5)).toArray(String[]::new);
            UserDTO userDTO = new UserDTO(user.getUsername(), MASKED_PASSWORD, user.isEnabled(), authorityNames);
            userDTOS.add(userDTO);
        });
        return userDTOS;
    }
}
