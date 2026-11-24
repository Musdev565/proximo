package cd.ucc.proximo.controller;

import cd.ucc.proximo.domain.entity.Auditoire;
import cd.ucc.proximo.domain.entity.Shop;
import cd.ucc.proximo.domain.entity.User;
import cd.ucc.proximo.domain.enums.UserRole;
import cd.ucc.proximo.dto.AuthRequest;
import cd.ucc.proximo.dto.AuthResponse;
import cd.ucc.proximo.dto.RegisterRequest;
import cd.ucc.proximo.repository.AuditoireRepository;
import cd.ucc.proximo.repository.ShopRepository;
import cd.ucc.proximo.repository.UserRepository;
import cd.ucc.proximo.security.JwtTokenProvider;
import cd.ucc.proximo.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final AuditoireRepository auditoireRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserRepository userRepository,
                          ShopRepository shopRepository,
                          AuditoireRepository auditoireRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.auditoireRepository = auditoireRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cette adresse email.");
        }

        User user = new User(
                req.fullName(),
                req.email().trim().toLowerCase(),
                req.phone(),
                passwordEncoder.encode(req.password()),
                UserRole.STUDENT
        );

        if (req.defaultAuditoireId() != null) {
            Auditoire auditoire = auditoireRepository.findById(req.defaultAuditoireId()).orElse(null);
            user.setDefaultAuditoire(auditoire);
        }

        User saved = userRepository.save(user);
        String token = tokenProvider.generateToken(saved.getEmail(), saved.getId(), saved.getRole().name());

        return ResponseEntity.ok(buildAuthResponse(saved, token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId()).orElseThrow();
        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return ResponseEntity.ok(buildAuthResponse(user, token));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(buildAuthResponse(user, null));
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        Shop shop = shopRepository.findByOwnerId(user.getId()).orElse(null);
        Auditoire defaultAud = user.getDefaultAuditoire();

        return AuthResponse.of(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                defaultAud != null ? defaultAud.getId() : null,
                defaultAud != null ? defaultAud.getCode() : null,
                shop != null ? shop.getId() : null,
                shop != null ? shop.getName() : null
        );
    }
}
