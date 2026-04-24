package com.rbac.controller;

import com.rbac.dto.AssignmentResponse;
import com.rbac.dto.UserCreateRequest;
import com.rbac.dto.UserResponse;
import com.rbac.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser() throws Exception {

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("secret123");
        UserResponse response = UserResponse.builder()
                .id(10L)
                .username("newuser")
                .enabled(true)
                .build();
        Mockito.when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(response);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.enabled").value(true));

    }

    @Test
    void assignRoleToUser() throws Exception {

        AssignmentResponse response = AssignmentResponse.builder()
                .message("Role assigned successfully")
                .data("User: testuser -> Role: ADMIN")
                .build();
        Mockito.when(userService.assignRoleToUser(1L, 1L))
                .thenReturn(response);
        mockMvc.perform(post("/users/1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Role assigned successfully"));

    }

}
