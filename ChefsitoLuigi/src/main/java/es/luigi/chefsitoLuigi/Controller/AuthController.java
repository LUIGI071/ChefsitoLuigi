package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.UserDto;
import es.luigi.chefsitoLuigi.Entity.User;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import es.luigi.chefsitoLuigi.Security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto userDto) {
        try {
            logger.info("👤 Intento de registro para: {}", userDto.getEmail());

            // Validaciones manuales adicionales
            if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
                logger.warn("Validación fallida: email vacío");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "VALIDATION_ERROR",
                        "message", "El email es obligatorio"
                ));
            }

            if (userDto.getPassword() == null || userDto.getPassword().length() < 8) {
                logger.warn("Validación fallida: contraseña demasiado corta para {}", userDto.getEmail());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "VALIDATION_ERROR",
                        "message", "La contraseña debe tener al menos 8 caracteres"
                ));
            }

            // Verificar si el usuario ya existe
            if (userRepository.findByEmail(userDto.getEmail().trim().toLowerCase()).isPresent()) {
                logger.warn("❌ Email ya registrado: {}", userDto.getEmail());
                return ResponseEntity.status(409).body(Map.of(
                        "error", "EMAIL_ALREADY_EXISTS",
                        "message", "El correo electrónico ya está registrado"
                ));
            }

            // Crear nuevo usuario
            User user = User.builder()
                    .email(userDto.getEmail().trim().toLowerCase())
                    .password(passwordEncoder.encode(userDto.getPassword()))
                    .fullName(userDto.getFullName())
                    .roles(userDto.getRoles() != null ? userDto.getRoles() : Set.of("ROLE_USER"))
                    .build();

            User savedUser = userRepository.save(user);
            logger.info("✅ Usuario registrado exitosamente: {}", savedUser.getEmail());

            // Generar token automáticamente después del registro
            String token = jwtTokenProvider.createToken(savedUser.getEmail(), savedUser.getRoles());

            return ResponseEntity.status(201).body(Map.of(
                    "token", token,
                    "email", savedUser.getEmail(),
                    "roles", savedUser.getRoles(),
                    "id", savedUser.getId(),
                    "fullName", savedUser.getFullName(),
                    "message", "Usuario registrado exitosamente"
            ));

        } catch (DataIntegrityViolationException e) {
            logger.error("❌ Error de integridad de datos durante registro: {}", e.getMessage());
            return ResponseEntity.status(409).body(Map.of(
                    "error", "DUPLICATE_EMAIL",
                    "message", "El correo electrónico ya está registrado"
            ));
        } catch (Exception e) {
            logger.error("❌ Error en registro para {}: {}", userDto.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(400).body(Map.of(
                    "error", "REGISTRATION_ERROR",
                    "message", "Error en el registro: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Map<String, String> creds) {
        String email = creds.get("email");
        try {
            logger.info("🔐 Intento de login para: {}", email);

            // Validaciones básicas
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Intento de login sin email");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "MISSING_EMAIL",
                        "message", "El email es obligatorio"
                ));
            }

            if (creds.get("password") == null || creds.get("password").isEmpty()) {
                logger.warn("Intento de login sin contraseña para: {}", email);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "MISSING_PASSWORD",
                        "message", "La contraseña es obligatoria"
                ));
            }

            var user = userRepository.findByEmail(email.trim().toLowerCase())
                    .orElseThrow(() -> {
                        logger.warn("❌ Usuario no encontrado: {}", email);
                        return new RuntimeException("Usuario no encontrado");
                    });

            logger.debug("✅ Usuario encontrado, verificando contraseña...");

            if (!passwordEncoder.matches(creds.get("password"), user.getPassword())) {
                logger.warn("❌ Contraseña incorrecta para: {}", email);
                return ResponseEntity.status(401).body(Map.of(
                        "error", "INVALID_CREDENTIALS",
                        "message", "Credenciales incorrectas"
                ));
            }

            logger.debug("✅ Contraseña correcta, creando token con roles: {}", user.getRoles());

            String token = jwtTokenProvider.createToken(user.getEmail(), user.getRoles());

            logger.info("✅ Login exitoso para: {}", email);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "email", user.getEmail(),
                    "roles", user.getRoles(),
                    "id", user.getId(),
                    "fullName", user.getFullName(),
                    "message", "Login exitoso"
            ));
        } catch (RuntimeException e) {
            logger.error("❌ Error de autenticación para {}: {}", email, e.getMessage());
            return ResponseEntity.status(401).body(Map.of(
                    "error", "AUTHENTICATION_FAILED",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("❌ Error inesperado en login para {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(401).body(Map.of(
                    "error", "LOGIN_ERROR",
                    "message", "Error en autenticación: " + e.getMessage()
            ));
        }
    }
}