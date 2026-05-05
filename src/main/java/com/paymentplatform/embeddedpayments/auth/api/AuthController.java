package com.paymentplatform.embeddedpayments.auth.api;

import com.paymentplatform.embeddedpayments.auth.application.GetCurrentMerchantUseCase;
import com.paymentplatform.embeddedpayments.auth.application.IssueMerchantTokenUseCase;
import com.paymentplatform.embeddedpayments.auth.application.LoginMerchantUseCase;
import com.paymentplatform.embeddedpayments.auth.application.LogoutMerchantUseCase;
import com.paymentplatform.embeddedpayments.auth.application.RegisterUserUseCase;
import com.paymentplatform.embeddedpayments.auth.application.RefreshTokenUseCase;
import com.paymentplatform.embeddedpayments.shared.exception.DomainException;
import com.paymentplatform.embeddedpayments.shared.security.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IssueMerchantTokenUseCase issueMerchantTokenUseCase;
    private final LoginMerchantUseCase loginMerchantUseCase;
    private final LogoutMerchantUseCase logoutMerchantUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final GetCurrentMerchantUseCase getCurrentMerchantUseCase;
    private final AuthenticationService authenticationService;

    public AuthController(IssueMerchantTokenUseCase issueMerchantTokenUseCase,
                          LoginMerchantUseCase loginMerchantUseCase,
                          LogoutMerchantUseCase logoutMerchantUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          RegisterUserUseCase registerUserUseCase,
                          GetCurrentMerchantUseCase getCurrentMerchantUseCase,
                          AuthenticationService authenticationService) {
        this.issueMerchantTokenUseCase = issueMerchantTokenUseCase;
        this.loginMerchantUseCase = loginMerchantUseCase;
        this.logoutMerchantUseCase = logoutMerchantUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.getCurrentMerchantUseCase = getCurrentMerchantUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginMerchantUseCase.LoginResponse loginResponse = loginMerchantUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new TokenResponse(loginResponse.token(), loginResponse.expiresAt()));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserUseCase.RegisteredUser registeredUser = registerUserUseCase.execute(
                request.email(),
                request.password(),
                request.role(),
                request.merchantName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(
                        registeredUser.id(),
                        registeredUser.email(),
                        registeredUser.status(),
                        registeredUser.role(),
                        registeredUser.merchantId(),
                        registeredUser.apiKey()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        logoutMerchantUseCase.execute();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshRequest request) {
        RefreshTokenUseCase.RefreshTokenResponse response = refreshTokenUseCase.execute(request.refreshToken());
        return ResponseEntity.ok(new TokenResponse(response.token(), response.expiresAt()));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        UUID currentUserId = authenticationService.getCurrentUserId();
        String role = authenticationService.getCurrentUserRole();
        UUID merchantId = authenticationService.getCurrentMerchantId();

        if (currentUserId == null || role == null) {
            throw new DomainException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "User is not authenticated",
                    List.of()
            );
        }

        GetCurrentMerchantUseCase.CurrentMerchantResponse currentUser =
                getCurrentMerchantUseCase.execute(currentUserId, role, merchantId);
        return ResponseEntity.ok(new CurrentUserResponse(
                currentUser.id(),
                currentUser.email(),
                currentUser.status(),
                currentUser.role(),
                currentUser.merchantId()
        ));
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> issueToken(@Valid @RequestBody TokenRequest request) {
        IssueMerchantTokenUseCase.IssuedToken token = issueMerchantTokenUseCase.execute(request.merchantId());
        return ResponseEntity.ok(new TokenResponse(token.token(), token.expiresAt()));
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email should be valid")
            String email,
            @NotBlank(message = "Password is required")
            String password) {
    }

    public record RegisterRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email should be valid")
            String email,
            @NotBlank(message = "Password is required")
            String password,
            String role,
            String merchantName) {
    }

    public record RefreshRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken) {
    }

    public record TokenRequest(@NotNull UUID merchantId) {
    }

    public record TokenResponse(String token, Instant expiresAt) {
    }

    public record RegisterResponse(UUID id, String email, String status, String role, UUID merchantId, String apiKey) {
    }

    public record CurrentUserResponse(UUID id, String email, String status, String role, UUID merchantId) {
    }
}
