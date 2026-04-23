package com.rbac.security;

import com.rbac.entity.User;
import com.rbac.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepo userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_Success() {

        User user = User.builder().id(1L).username("admin").password("secret").enabled(true).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        UserDetails details = userDetailsService.loadUserByUsername("admin");
        assertNotNull(details);
        assertEquals("admin", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertTrue(details.isEnabled());
        verify(userRepository, times(1)).findByUsername("admin");

    }

    @Test
    void loadUserByUsername_NotFound() {

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown"));
        verify(userRepository, times(1)).findByUsername("unknown");

    }

}
