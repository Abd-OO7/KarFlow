package ma.karflow.feature.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.auth.dto.*;
import ma.karflow.feature.auth.security.JwtService;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/client-auth")
@RequiredArgsConstructor
@Tag(name = "Client Authentication", description = "Authentification des clients")
public class ClientAuthController {

    private final ClientRepository clientRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un client")
    public ResponseEntity<ApiResponse<ClientAuthResponse>> register(@Valid @RequestBody ClientRegisterRequest request) {
        // Check if email already registered with password
        if (clientRepository.existsByEmailWithPassword(request.email())) {
            throw new DuplicateResourceException("Client", "email", request.email());
        }

        // Find existing client by email (may have been created via booking) or create new
        Client client = clientRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    Client c = new Client();
                    c.setEmail(request.email());
                    // Clients registering directly don't have a tenantId yet;
                    // we use a nil UUID as placeholder for global clients
                    c.setTenantId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"));
                    return c;
                });

        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setPhone(request.phone());
        client.setCin(request.cin());
        client.setPassword(passwordEncoder.encode(request.password()));
        client = clientRepository.save(client);

        String accessToken = jwtService.generateClientAccessToken(client.getEmail(), client.getId());
        String refreshToken = jwtService.generateRefreshToken(client.getEmail());

        ClientAuthResponse response = new ClientAuthResponse(
                accessToken,
                refreshToken,
                client.getId(),
                client.getEmail(),
                client.getFirstName(),
                client.getLastName(),
                "CLIENT"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Inscription réussie"));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un client")
    public ResponseEntity<ApiResponse<ClientAuthResponse>> login(@Valid @RequestBody ClientLoginRequest request) {
        Client client = clientRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        if (client.getPassword() == null || !passwordEncoder.matches(request.password(), client.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        String accessToken = jwtService.generateClientAccessToken(client.getEmail(), client.getId());
        String refreshToken = jwtService.generateRefreshToken(client.getEmail());

        ClientAuthResponse response = new ClientAuthResponse(
                accessToken,
                refreshToken,
                client.getId(),
                client.getEmail(),
                client.getFirstName(),
                client.getLastName(),
                "CLIENT"
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Connexion réussie"));
    }

    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil du client connecté")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> me(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtService.isTokenValid(token) || !jwtService.isClientToken(token)) {
            throw new UnauthorizedException("Token client invalide");
        }

        java.util.UUID clientId = jwtService.extractClientId(token);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new UnauthorizedException("Client introuvable"));

        ClientProfileResponse profile = new ClientProfileResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCin(),
                client.getAddress(),
                client.getLicenseNumber(),
                client.getLicenseExpiry(),
                client.getDateOfBirth(),
                client.getPhotoUrl()
        );

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // Try cookie
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("kf_access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
