package ma.karflow.feature.auth.service;

import ma.karflow.feature.auth.dto.AuthResponse;
import ma.karflow.feature.auth.dto.LoginRequest;
import ma.karflow.feature.auth.dto.RegisterRequest;
import ma.karflow.feature.auth.entity.Permission;
import ma.karflow.feature.auth.entity.Role;
import ma.karflow.feature.auth.entity.User;
import ma.karflow.feature.auth.enums.RoleType;
import ma.karflow.feature.auth.repository.PermissionRepository;
import ma.karflow.feature.auth.repository.RoleRepository;
import ma.karflow.feature.auth.repository.UserRepository;
import ma.karflow.feature.auth.security.JwtService;
import ma.karflow.feature.organisation.entity.Organisation;
import ma.karflow.feature.organisation.repository.OrganisationRepository;
import ma.karflow.shared.util.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private OrganisationRepository organisationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role ownerRole;

    @BeforeEach
    void setUp() {
        Permission perm = new Permission("VEHICLE_READ");
        perm.setTenantId(UUID.randomUUID());

        ownerRole = new Role(RoleType.OWNER);
        ownerRole.setTenantId(UUID.randomUUID());
        ownerRole.setPermissions(Set.of(perm));

        testUser = new User();
        testUser.setEmail("test@karflow.ma");
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$12$hashedpassword");
        testUser.setTenantId(UUID.randomUUID());
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(ownerRole));
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "test@karflow.ma", "password123", "0600000000", "TestAgency"
        );

        when(userRepository.existsByEmailAndTenantId(anyString(), any(UUID.class))).thenReturn(false);
        when(permissionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(organisationRepository.save(any(Organisation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setTenantId(u.getTenantId());
            return u;
        });
        when(jwtService.generateAccessToken(anyString(), any(), any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("test@karflow.ma", response.email());
        assertTrue(response.roles().contains("OWNER"));

        verify(userRepository).save(any(User.class));
        verify(roleRepository, times(3)).save(any(Role.class)); // OWNER, ADMIN, AGENT
    }

    @Test
    void login_withValidCredentials_shouldReturnTokens() {
        LoginRequest request = new LoginRequest("test@karflow.ma", "password123");

        when(userRepository.findByEmail("test@karflow.ma")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtService.generateAccessToken(anyString(), any(), any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("test@karflow.ma", response.email());
    }

    @Test
    void login_withInvalidPassword_shouldThrow() {
        LoginRequest request = new LoginRequest("test@karflow.ma", "wrongpassword");

        when(userRepository.findByEmail("test@karflow.ma")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_withNonExistentEmail_shouldThrow() {
        LoginRequest request = new LoginRequest("unknown@karflow.ma", "password123");

        when(userRepository.findByEmail("unknown@karflow.ma")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
