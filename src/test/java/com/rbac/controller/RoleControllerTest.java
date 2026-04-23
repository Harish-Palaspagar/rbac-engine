package com.rbac.controller;

import com.rbac.dto.AssignmentResponse;
import com.rbac.dto.RoleRequest;
import com.rbac.dto.RoleResponse;
import com.rbac.service.RoleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRole() throws Exception {

        RoleRequest request = new RoleRequest();
        request.setName("admin");
        RoleResponse response = RoleResponse.builder().id(1L).name("ADMIN").build();
        Mockito.when(roleService.createRole(any(RoleRequest.class))).thenReturn(response);
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("ADMIN"));

    }

    @Test
    void assignPermissionToRole() throws Exception {

        AssignmentResponse response = AssignmentResponse.builder()
                .message("Permission assigned successfully")
                .data("Role: ADMIN -> Permission: MANAGE_USERS")
                .build();
        Mockito.when(roleService.assignPermissionToRole(eq(1L), eq(1L))).thenReturn(response);
        mockMvc.perform(post("/roles/1/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Permission assigned successfully"));

    }

    @Test
    void getAllRoles() throws Exception {

        RoleResponse response = RoleResponse.builder().id(1L).name("ADMIN").build();
        Mockito.when(roleService.getAllRoles()).thenReturn(List.of(response));
        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));

    }

    @Test
    void getRoleById() throws Exception {

        RoleResponse response = RoleResponse.builder().id(1L).name("ADMIN").build();
        Mockito.when(roleService.getRoleById(1L)).thenReturn(response);
        mockMvc.perform(get("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN"));

    }

}
