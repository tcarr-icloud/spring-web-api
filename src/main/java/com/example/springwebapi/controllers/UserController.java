package com.example.springwebapi.controllers;

import com.example.springwebapi.dtos.UserDTO;
import com.example.springwebapi.exceptions.UserDataInvalid;
import com.example.springwebapi.exceptions.UsernameConflict;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private static final String MASKED_PASSWORD = "********";
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/user")
    UserDTO createUser(@RequestBody UserDTO userDTO) {
        validateUserDTO(userDTO);
        if (userDetailsManager.userExists(userDTO.username)) {
            throw new UsernameConflict("Username " + userDTO.username + " already exists");
        }
        UserDetails userDetails = User.builder().disabled(!userDTO.enabled).username(userDTO.username).password(passwordEncoder.encode(userDTO.password)).roles(userDTO.authorities).build();
        userDetailsManager.createUser(userDetails);
        userDetails = userDetailsManager.loadUserByUsername(userDTO.username);
        return new UserDTO(userDetails.getUsername(), MASKED_PASSWORD, userDetails.isEnabled(), userDetails.getAuthorities().stream().map(authority -> authority.toString().substring(5)).toArray(String[]::new));
    }

    @GetMapping("/user/{username}")
    UserDTO getUser(@PathVariable String username) {
        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        return new UserDTO(userDetails.getUsername(), MASKED_PASSWORD, userDetails.isEnabled(), userDetails.getAuthorities().stream().map(authority -> authority.toString().substring(5)).toArray(String[]::new));
    }

    @PutMapping("/user/{username}")
    UserDTO updateUser(@PathVariable String username, @RequestBody UserDTO userDTO) {
        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        if (!userDetails.getUsername().equals(userDTO.username)) {
            throw new UserDataInvalid("Username " + username + " cannot be changed");
        }
        validateUserDTO(userDTO);
        userDetails = User.builder().disabled(!userDTO.enabled).username(userDTO.username).password(passwordEncoder.encode(userDTO.password)).roles(userDTO.authorities).build();
        userDetailsManager.updateUser(userDetails);
        userDetails = userDetailsManager.loadUserByUsername(userDTO.username);
        return new UserDTO(userDetails.getUsername(), MASKED_PASSWORD, userDetails.isEnabled(), userDetails.getAuthorities().stream().map(authority -> authority.toString().substring(5)).toArray(String[]::new));
    }

    @DeleteMapping("/user/{username}")
    void deleteUser(@PathVariable String username) {
        if (!userDetailsManager.userExists(username)) {
            throw new UsernameNotFoundException("Username " + username + " not found");
        }
        userDetailsManager.deleteUser(username);
    }

    private void validateUserDTO(@RequestBody UserDTO userDTO) {
        if (userDTO.username == null || userDTO.username.isBlank()) {
            throw new UserDataInvalid("Username cannot be empty");
        }
        if (userDTO.password == null || userDTO.password.isBlank()) {
            throw new UserDataInvalid("Password cannot be empty");
        }
        if (userDTO.authorities == null || userDTO.authorities.length == 0) {
            throw new UserDataInvalid("Authorities cannot be empty");
        }
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    String handleUsernameNotFoundException(UsernameNotFoundException e) {
        return e.getLocalizedMessage();
    }

    @ExceptionHandler(UsernameConflict.class)
    @ResponseStatus(org.springframework.http.HttpStatus.CONFLICT)
    String handleUsernameAlreadyExistsException(UsernameConflict e) {
        return e.getLocalizedMessage();
    }

    @ExceptionHandler(UserDataInvalid.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    String handleUserInvalidException(UserDataInvalid e) {
        return e.getLocalizedMessage();
    }
}
