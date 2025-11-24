package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Service.Impl.IngredientTranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TranslationController {

    private final IngredientTranslationService translationService;

    @PostMapping("/translate-ingredients")
    public ResponseEntity<?> translateIngredients() {
        try {
            System.out.println("🚀 Iniciando traducción automática de ingredientes...");
            translationService.translateAndSaveIngredients();

            return ResponseEntity.ok(Map.of(
                    "message", "Traducción completada exitosamente",
                    "status", "success"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error en la traducción",
                    "message", e.getMessage()
            ));
        }
    }
}