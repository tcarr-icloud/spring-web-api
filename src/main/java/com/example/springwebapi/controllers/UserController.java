package com.example.springwebapi.controllers;

import com.example.springwebapi.dtos.UserDTO;
import com.example.springwebapi.exceptions.UserDataInvalid;
import com.example.springwebapi.exceptions.UsernameConflictException;
import com.example.springwebapi.repositories.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing user-related operations.
 */
@RestController
public class UserController {
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserController(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userDetailsManager = userDetailsManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user with the provided UserDTO.
     * Validates the UserDTO and checks for username conflicts.
     * Encodes the password and creates a new UserDetails object.
     * Saves the user details and returns the created UserDTO.
     */
    @PostMapping("/user")
    UserDTO createUser(@RequestBody UserDTO userDTO) {
        validateUserDTO(userDTO);
        if (userDetailsManager.userExists(userDTO.username)) {
            throw new UsernameConflictException("Username " + userDTO.username + " already exists");
        }
        UserDetails userDetails = User.builder().disabled(!userDTO.enabled).username(userDTO.username).password(passwordEncoder.encode(userDTO.password)).roles(userDTO.authorities).build();
        userDetailsManager.createUser(userDetails);
        userDetails = userDetailsManager.loadUserByUsername(userDTO.username);
        return new UserDTO(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), userDetails.getAuthorities().stream().map(authority -> authority.toString().substring(5)).toArray(String[]::new));
    }

    /**
     * Get all users.
     *
     * @return
     */
    @GetMapping("/users")
    List<UserDTO> getUsers() {
        List<UserDTO> userDTOS = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            String[] strings = user.getAuthorities().stream().map(authority -> authority.getAuthority().substring(5)).toArray(String[]::new);
            UserDTO userDTO = new UserDTO(user.getUsername(), user.getPassword(), user.isEnabled(), strings);
            userDTOS.add(userDTO);
        });
        return userDTOS;
    }

    /**
     * Get a single user by username.
     *
     * @param username
     * @return
     */
    @GetMapping("/user/{username}")
    UserDTO getUser(@PathVariable String username) {
        UserDetails userDetails = userDetailsManager.loadUserByUsername(username);
        return new UserDTO(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), userDetails.getAuthorities().stream().map(authority -> authority.toString().substring(5)).toArray(String[]::new));
    }

    /**
     * Update a user by username.
     *
     * @param username
     * @param userDTO
     * @return
     */
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
        return new UserDTO(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), userDetails.getAuthorities().stream().map(authority -> authority.toString().substring(5)).toArray(String[]::new));
    }

    /**
     * Delete a user by username.
     *
     * @param username
     */
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
        // Default message: "Username {username} not found"
        return e.getLocalizedMessage();
    }

    @ExceptionHandler(UsernameConflictException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.CONFLICT)
    String handleUsernameAlreadyExistsException(UsernameConflictException e) {
        // Default message: "Username {username} already exists"
        return e.getLocalizedMessage();
    }

    @ExceptionHandler(UserDataInvalid.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    String handleUserInvalidException(UserDataInvalid e) {
        // Default message: "Username {username} cannot be changed"
        // Default message: "Username cannot be empty"
        // Default message: "Password cannot be empty"
        // Default message: "Authorities cannot be empty"
        return e.getLocalizedMessage();
    }
}
