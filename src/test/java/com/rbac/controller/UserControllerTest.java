package com.rbac.controller;

import com.rbac.dto.AssignmentResponse;
import com.rbac.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void assignRoleToUser() throws Exception {

        AssignmentResponse response = AssignmentResponse.builder()
                .message("Role assigned successfully")
                .data("User: testuser -> Role: ADMIN")
                .build();
        Mockito.when(userService.assignRoleToUser(eq(1L), eq(1L))).thenReturn(response);
        mockMvc.perform(post("/users/1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Role assigned successfully"));

    }

}
