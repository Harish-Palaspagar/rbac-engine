package com.rbac.exception;

import com.rbac.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rbac.controller.PermissionController;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    void handleNotFound_Returns404() throws Exception {

        Mockito.when(permissionService.getPermissionById(anyLong()))
                .thenThrow(new RbacExceptions.ResourceNotFoundException("Permission not found: 99"));
        mockMvc.perform(get("/permissions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

    }


    @Test
    void handleDuplicate_Returns409() throws Exception {

        Mockito.when(permissionService.createPermission(any()))
                .thenThrow(new RbacExceptions.DuplicateResourceException("Permission already exists: READ"));
        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"read\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

    }

    @Test
    void handleInvalid_Returns400() throws Exception {

        Mockito.when(permissionService.createPermission(any()))
                .thenThrow(new RbacExceptions.InvalidOperationException("Invalid operation"));
        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"something\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

    }

    @Test
    void handleValidation_Returns400WhenNameBlank() throws Exception {

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

    }

    @Test
    void handleGeneric_Returns500() throws Exception {

        Mockito.when(permissionService.getPermissionById(anyLong()))
                .thenThrow(new RuntimeException("Unexpected failure"));
        mockMvc.perform(get("/permissions/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

    }

}
