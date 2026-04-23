package com.rbac.security;

import com.rbac.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void customUserDetails_AllFields() {

        User user = User.builder()
                .id(42L)
                .username("john")
                .password("pass123")
                .enabled(true)
                .build();
        CustomUserDetails details = new CustomUserDetails(user);
        assertEquals(42L, details.getUserId());
        assertEquals("john", details.getUsername());
        assertEquals("pass123", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertNotNull(details.getAuthorities());
        assertTrue(details.getAuthorities().isEmpty());

    }

    @Test
    void customUserDetails_DisabledUser() {

        User user = User.builder()
                .id(1L)
                .username("disabled")
                .password("pwd")
                .enabled(false)
                .build();
        CustomUserDetails details = new CustomUserDetails(user);
        assertFalse(details.isEnabled());

    }

}
