package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Repository.IngredientRepository;
import es.luigi.chefsitoLuigi.Service.Impl.IngredientTranslationService;
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
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    private final IngredientRepository ingredientRepository;
    private final IngredientTranslationService translationService;

    @GetMapping("/debug/ingredients-status")
    public ResponseEntity<?> getIngredientsStatus() {
        long totalIngredients = ingredientRepository.count();
        long ingredientsWithSpanish = ingredientRepository.findAll().stream()
                .filter(ing -> ing.getNameEs() != null && !ing.getNameEs().isEmpty())
                .count();

        return ResponseEntity.ok(Map.of(
                "totalIngredients", totalIngredients,
                "ingredientsWithSpanish", ingredientsWithSpanish,
                "translationStatus", ingredientsWithSpanish > 0 ? "TRANSLATED" : "NOT_TRANSLATED"
        ));
    }

    @PostMapping("/debug/force-translation")
    public ResponseEntity<?> forceTranslation() {
        try {
            translationService.translateAndSaveIngredients();
            return ResponseEntity.ok("Traducción forzada ejecutada");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/debug/update-translations")
    public ResponseEntity<?> updateExistingTranslations() {
        try {
            long totalIngredients = ingredientRepository.count();
            long ingredientsWithoutSpanish = ingredientRepository.findAll().stream()
                    .filter(ing -> ing.getNameEs() == null || ing.getNameEs().isEmpty())
                    .count();

            logger.info("📊 Estado antes de actualización: {} ingredientes, {} sin traducción",
                    totalIngredients, ingredientsWithoutSpanish);

            // Usar los métodos públicos individualmente
            List<String> englishIngredients = translationService.fetchIngredientsFromTheMealDB();
            Map<String, String> translations = translationService.translateIngredients(englishIngredients);
            translationService.saveTranslatedIngredients(translations);

            long ingredientsWithSpanishAfter = ingredientRepository.findAll().stream()
                    .filter(ing -> ing.getNameEs() != null && !ing.getNameEs().isEmpty())
                    .count();

            return ResponseEntity.ok(Map.of(
                    "message", "Actualización de traducciones completada",
                    "before", Map.of(
                            "total", totalIngredients,
                            "withoutSpanish", ingredientsWithoutSpanish
                    ),
                    "after", Map.of(
                            "total", totalIngredients,
                            "withSpanish", ingredientsWithSpanishAfter
                    ),
                    "updated", ingredientsWithSpanishAfter - (totalIngredients - ingredientsWithoutSpanish)
            ));
        } catch (Exception e) {
            logger.error("❌ Error actualizando traducciones: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}