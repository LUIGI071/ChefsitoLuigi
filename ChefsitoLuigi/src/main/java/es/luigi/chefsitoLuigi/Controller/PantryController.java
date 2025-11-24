package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.PantryItemCreateRequest;
import es.luigi.chefsitoLuigi.Dto.PantryItemResponse;
import es.luigi.chefsitoLuigi.Security.CustomUserDetailsService;
import es.luigi.chefsitoLuigi.Service.PantryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
@RequiredArgsConstructor
public class PantryController {

    private static final Logger logger = LoggerFactory.getLogger(PantryController.class);

    private final PantryService pantryService;

    @Operation(summary = "Añadir item a la despensa del usuario actual")
    @PostMapping
    public ResponseEntity<PantryItemResponse> addItem(@Valid @RequestBody PantryItemCreateRequest createRequest, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        logger.info("➕ Usuario {} añadiendo item a despensa - Ingredient: {}, Quantity: {}",
                userId, createRequest.getIngredientId(), createRequest.getQuantity());

        PantryItemResponse created = pantryService.addItem(createRequest, userId);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar items del usuario actual")
    @GetMapping
    public ResponseEntity<List<PantryItemResponse>> listByUser(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        logger.info("📋 Solicitando despensa del usuario: {}", userId);
        return ResponseEntity.ok(pantryService.listByUser(userId));
    }

    @Operation(summary = "Listar items por usuario (solo admin)")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PantryItemResponse>> listByUserAdmin(@PathVariable Long userId) {
        logger.info("👑 Admin solicitando despensa del usuario: {}", userId);
        return ResponseEntity.ok(pantryService.listByUser(userId));
    }

    @Operation(summary = "Actualizar cantidad de item")
    @PutMapping("/{id}")
    public ResponseEntity<PantryItemResponse> updateQuantity(@PathVariable Long id, @RequestParam Double quantity, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        logger.info("✏️ Usuario {} actualizando cantidad del item {} a {}", userId, id, quantity);

        PantryItemResponse updated = pantryService.updateQuantity(id, quantity, userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar item de despensa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        logger.info("🗑️ Usuario {} eliminando item {}", userId, id);

        pantryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene el ID del usuario desde el Authentication object
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new SecurityException("No se pudo obtener el ID del usuario autenticado");
    }
}