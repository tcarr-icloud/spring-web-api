package com.example.springwebapi;

import com.example.springwebapi.controllers.UserController;
import com.example.springwebapi.dtos.UserDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final String USER_USERNAME = "user";
    private static final String USER_PASSWORD = "password";
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "password";
    private static final String USER_ROLE = "USER";
    private static final String[] USER_ROLES = new String[]{USER_ROLE};
    private static final String[] ADMIN_ROLES = new String[]{"ADMIN", USER_ROLE};

    @LocalServerPort
    private int port;

    @Autowired
    private UserController userController;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        try {
            restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForObject("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME);
            restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
        } catch (Exception e) {
            // Ignore exception if the user does not exist
        }
    }

    @Test
    void contextLoads() {
        assertThat(userController).isNotNull();
    }

    @Test
    void getUser() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForObject("http://localhost:{port}/user", userDTO, UserDTO.class, port);

        // Get user by username
        userDTO = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForObject("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME);

        // Verify user data
        assertThat(userDTO.username).isEqualTo(TEST_USERNAME);
        assertThat(userDTO.password).isNotNull();
        assertThat(userDTO.enabled).isEqualTo(true);
        assertThat(userDTO.authorities).isEqualTo(USER_ROLES);

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    void getUser_ExpectingStatusCode404() {
        // Get user by username
        ResponseEntity<String> dtoResponseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForEntity("http://localhost:{port}/user/{username}", String.class, port, TEST_USERNAME + "_invalid");
        assertThat(dtoResponseEntity.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void createUser() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        assertThat(restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForObject("http://localhost:{port}/user", userDTO, UserDTO.class, port)).hasFieldOrProperty("username").hasFieldOrPropertyWithValue("username", TEST_USERNAME).hasFieldOrProperty("password").hasFieldOrProperty("enabled").hasFieldOrPropertyWithValue("enabled", true).hasFieldOrProperty("authorities").hasFieldOrPropertyWithValue("authorities", USER_ROLES);

        // Verify user data
        assertThat(restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForObject("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME)).hasFieldOrProperty("username").hasFieldOrPropertyWithValue("username", TEST_USERNAME).hasFieldOrProperty("password").hasFieldOrProperty("enabled").hasFieldOrPropertyWithValue("enabled", true).hasFieldOrProperty("authorities").hasFieldOrPropertyWithValue("authorities", USER_ROLES);

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    void createUser_ExpectingUsernameConflictException() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForObject("http://localhost:{port}/user", userDTO, UserDTO.class, port);

        // Try to install the same test user again
        ResponseEntity<String> responseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, String.class, port);

        // Verify response
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(409);
        assertThat(responseEntity.getBody()).isEqualTo("Username " + TEST_USERNAME + " already exists");

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    void createUser_ExpectingNoUsernameException() {
        // Try to install a test user with no username
        UserDTO userDTO = new UserDTO(null, TEST_PASSWORD, true, USER_ROLES);
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, String.class, port);

        // Verify response
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Username cannot be empty");
    }

    @Test
    void createUser_ExpectingNoPasswordException() {
        // Try to install a test user with no password
        UserDTO userDTO = new UserDTO(TEST_USERNAME, null, true, USER_ROLES);
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, String.class, port);

        // Verify response
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Password cannot be empty");
    }

    @Test
    void createUser_ExpectingNoAuthoritiesException() {
        // Try to install a test user with no authorities
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, null);
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, String.class, port);

        // Verify response
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("Authorities cannot be empty");
    }

    @Test
    void updateUser() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForObject("http://localhost:{port}/user", userDTO, UserDTO.class, port);

        // Get user by username
        userDTO = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForObject("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME);
        assertThat(userDTO.enabled).isTrue();

        // Update user
        userDTO.enabled = false;
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).put("http://localhost:{port}/user/{username}", userDTO, port, TEST_USERNAME);

        // Verify user data
        assertThat(restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForObject("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME)).hasFieldOrProperty("username").hasFieldOrPropertyWithValue("username", TEST_USERNAME).hasFieldOrProperty("password").hasFieldOrProperty("enabled").hasFieldOrPropertyWithValue("enabled", false).hasFieldOrProperty("authorities").hasFieldOrPropertyWithValue("authorities", USER_ROLES);

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    void updateUser_ExpectingUsernameConflictException() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, UserDTO.class, port);

        // Get user by username
        ResponseEntity<UserDTO> responseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForEntity("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME);
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        userDTO = responseEntity.getBody();

        // Update user
        Assertions.assertNotNull(userDTO);
        userDTO.username = "new_" + TEST_USERNAME;
        ResponseEntity<String> stringResponseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).exchange("http://localhost:{port}/user/{username}", HttpMethod.PUT, new HttpEntity<>(userDTO), String.class, port, TEST_USERNAME);

        // Verify response
        assertThat(stringResponseEntity.getStatusCode().value()).isEqualTo(400);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Username " + TEST_USERNAME + " cannot be changed");

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    void updateUser_ExpectingNoPasswordException() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        ResponseEntity<UserDTO> responseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, UserDTO.class, port);
        userDTO = responseEntity.getBody();

        // Update user
        Assertions.assertNotNull(userDTO);
        userDTO.password = "";
        ResponseEntity<String> stringResponseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).exchange("http://localhost:{port}/user/{username}", HttpMethod.PUT, new HttpEntity<>(userDTO), String.class, port, TEST_USERNAME);

        // Verify response
        assertThat(stringResponseEntity.getStatusCode().value()).isEqualTo(400);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Password cannot be empty");

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    void updateUser_ExpectingNoAuthoritiesException() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        ResponseEntity<UserDTO> responseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForEntity("http://localhost:{port}/user", userDTO, UserDTO.class, port);
        userDTO = responseEntity.getBody();

        // Update user
        Assertions.assertNotNull(userDTO);
        userDTO.authorities = null;
        ResponseEntity<String> stringResponseEntity = restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).exchange("http://localhost:{port}/user/{username}", HttpMethod.PUT, new HttpEntity<>(userDTO), String.class, port, TEST_USERNAME);

        // Verify response
        assertThat(stringResponseEntity.getStatusCode().value()).isEqualTo(400);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Authorities cannot be empty");

        // Delete test user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);
    }

    @Test
    public void deleteUser() {
        // Install test user
        UserDTO userDTO = new UserDTO(TEST_USERNAME, TEST_PASSWORD, true, USER_ROLES);
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).postForObject("http://localhost:{port}/user", userDTO, UserDTO.class, port);

        // Delete user
        restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).delete("http://localhost:{port}/user/{username}", port, TEST_USERNAME);

        // Verify user data
        try {
            restTemplate.withBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD).getForObject("http://localhost:{port}/user/{username}", UserDTO.class, port, TEST_USERNAME);
        } catch (Exception e) {
            assertThat(e.getLocalizedMessage()).isEqualTo("Error while extracting response for type [class com.example.springwebapi.dtos.UserDTO] and content type [application/json]");
        }
    }
}
