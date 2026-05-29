package ma.karflow.feature.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.config.JwtConfig;
import ma.karflow.feature.auth.dto.*;
import ma.karflow.feature.auth.service.AuthService;
import ma.karflow.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints d'authentification et de gestion de compte")
public class AuthController {

    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @PostMapping("/register")
    @Operation(summary = "Inscription d'une nouvelle agence (tenant)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                               HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        setTokenCookies(response, authResponse);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse, "Inscription réussie"));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setTokenCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Connexion réussie"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                              HttpServletResponse response) {
        AuthResponse authResponse = authService.refreshToken(request);
        setTokenCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Demander la réinitialisation du mot de passe")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser le mot de passe avec un token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mot de passe réinitialisé avec succès"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion (suppression des cookies)")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        clearTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.success(null, "Déconnexion réussie"));
    }

    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        UserResponse userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    private void setTokenCookies(HttpServletResponse response, AuthResponse auth) {
        Cookie accessCookie = new Cookie("kf_access_token", auth.accessToken());
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(jwtConfig.isCookieSecure());
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (jwtConfig.getAccessTokenExpiration() / 1000));
        accessCookie.setAttribute("SameSite", "Strict");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("kf_refresh_token", auth.refreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(jwtConfig.isCookieSecure());
        refreshCookie.setPath("/api/v1/auth/refresh");
        refreshCookie.setMaxAge((int) (jwtConfig.getRefreshTokenExpiration() / 1000));
        refreshCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("kf_access_token", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(jwtConfig.isCookieSecure());
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("kf_refresh_token", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(jwtConfig.isCookieSecure());
        refreshCookie.setPath("/api/v1/auth/refresh");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }
}
