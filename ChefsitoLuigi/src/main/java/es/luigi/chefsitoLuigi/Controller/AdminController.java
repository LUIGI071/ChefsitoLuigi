package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.UserDto;
import es.luigi.chefsitoLuigi.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        logger.info(" Admin solicitando lista de usuarios");
        List<UserDto> users = userService.getAllUsers();
        logger.info(" Lista de usuarios obtenida: {} usuarios", users.size());
        return users;
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        logger.info("🗑 Admin eliminando usuario ID: {}", userId);
        userService.deleteUser(userId);
        logger.info(" Usuario {} eliminado exitosamente", userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<UserDto> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody Map<String, List<String>> request) {
        logger.info(" Admin actualizando roles para usuario ID: {}", userId);
        List<String> roles = request.get("roles");
        logger.debug("Nuevos roles a asignar: {}", roles);
        UserDto updatedUser = userService.updateUserRoles(userId, roles);
        logger.info(" Roles actualizados para usuario {}: {}", userId, updatedUser.getRoles());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        logger.info(" Admin solicitando estadísticas");
        long userCount = userService.getAllUsers().size();
        logger.info("Estadísticas calculadas: {} usuarios totales", userCount);
        return ResponseEntity.ok(Map.of("totalUsers", userCount));
    }
}