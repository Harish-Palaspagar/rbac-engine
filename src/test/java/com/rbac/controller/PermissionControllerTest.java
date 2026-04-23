package com.rbac.controller;

import com.rbac.dto.PermissionRequest;
import com.rbac.dto.PermissionResponse;
import com.rbac.service.PermissionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPermission() throws Exception {

        PermissionRequest request = new PermissionRequest();
        request.setName("manage_users");
        PermissionResponse response = PermissionResponse.builder().id(1L).name("MANAGE_USERS").build();
        Mockito.when(permissionService.createPermission(any(PermissionRequest.class))).thenReturn(response);
        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MANAGE_USERS"));

    }

    @Test
    void getAllPermissions() throws Exception {

        PermissionResponse response = PermissionResponse.builder().id(1L).name("MANAGE_USERS").build();
        Mockito.when(permissionService.getAllPermissions()).thenReturn(List.of(response));
        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("MANAGE_USERS"));

    }

    @Test
    void getPermissionById() throws Exception {

        PermissionResponse response = PermissionResponse.builder().id(1L).name("MANAGE_USERS").build();
        Mockito.when(permissionService.getPermissionById(1L)).thenReturn(response);
        mockMvc.perform(get("/permissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MANAGE_USERS"));

    }

}
