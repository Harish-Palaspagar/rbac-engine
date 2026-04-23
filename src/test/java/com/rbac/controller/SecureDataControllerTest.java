package com.rbac.controller;

import com.rbac.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rbac.repository.RolePermissionRepo;
import com.rbac.repository.UserRoleRepo;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class SecureDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRoleRepo userRoleRepo;

    @MockitoBean
    private RolePermissionRepo rolePermissionRepo;

    @Test
    void getSecureData() throws Exception {

        CustomUserDetails userDetails = new CustomUserDetails(com.rbac.entity.User.builder().id(1L).username("admin").password("pass").build());
        Mockito.when(userRoleRepo.findRoleIdsByUserId(1L)).thenReturn(List.of(1L));
        Mockito.when(rolePermissionRepo.findPermissionNamesByRoleIds(List.of(1L))).thenReturn(List.of("ACCESS_SECURE_DATA"));
        mockMvc.perform(get("/secure-data")
                .with(user(userDetails)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessedBy").value("admin"));

    }

}
