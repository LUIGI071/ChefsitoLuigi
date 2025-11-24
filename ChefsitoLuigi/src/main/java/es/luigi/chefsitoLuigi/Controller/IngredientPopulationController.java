package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Service.Impl.IngredientPopulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class IngredientPopulationController {

    private final IngredientPopulationService populationService;

    @PostMapping("/populate-ingredients")
    public ResponseEntity<?> populateIngredients() {
        try {
            populationService.populateIngredientsFromMealDB();
            return ResponseEntity.ok("Ingredients populated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error populating ingredients: " + e.getMessage());
        }
    }
}