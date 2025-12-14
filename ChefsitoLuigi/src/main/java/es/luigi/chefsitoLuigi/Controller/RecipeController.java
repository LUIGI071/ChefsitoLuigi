package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.RecipeDto;
import es.luigi.chefsitoLuigi.Entity.Recipe;
import es.luigi.chefsitoLuigi.Exception.ResourceNotFoundException;
import es.luigi.chefsitoLuigi.Mapper.RecipeMapper;
import es.luigi.chefsitoLuigi.Service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    // Operaciones con Entity (para OpenAI e historial)
    @Operation(summary = "Obtener historial de recetas recomendadas para un usuario")
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Recipe>> getRecipeHistory(@PathVariable Long userId) {
        List<Recipe> history = recipeService.getUserRecipeHistory(userId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Buscar recetas por categoría")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Recipe>> getRecipesByCategory(@PathVariable String category) {
        List<Recipe> recipes = recipeService.findByCategory(category);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Buscar recetas por dificultad")
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<Recipe>> getRecipesByDifficulty(@PathVariable String difficulty) {
        List<Recipe> recipes = recipeService.findByDifficulty(difficulty);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Buscar recetas por tiempo máximo de preparación")
    @GetMapping("/time/{maxTime}")
    public ResponseEntity<List<Recipe>> getRecipesByMaxTime(@PathVariable Integer maxTime) {
        List<Recipe> recipes = recipeService.findByMaxTime(maxTime);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Buscar recetas por título")
    @GetMapping("/search")
    public ResponseEntity<List<Recipe>> searchRecipes(@RequestParam String title) {
        List<Recipe> recipes = recipeService.searchByTitle(title);
        return ResponseEntity.ok(recipes);
    }

    @Operation(summary = "Obtener receta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable Long id) {
        Recipe recipe = recipeService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", id));
        return ResponseEntity.ok(recipe);
    }

    // Operaciones con DTO
    @Operation(summary = "Obtener todas las recetas como DTO")
    @GetMapping("/dto")
    public ResponseEntity<List<RecipeDto>> getAllRecipesAsDto() {
        List<Recipe> recipes = recipeService.findAll();
        List<RecipeDto> recipeDtos = recipes.stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(recipeDtos);
    }

    @Operation(summary = "Obtener receta por ID como DTO")
    @GetMapping("/dto/{id}")
    public ResponseEntity<RecipeDto> getRecipeAsDto(@PathVariable Long id) {
        Recipe recipe = recipeService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", "id", id));
        return ResponseEntity.ok(recipeMapper.toDto(recipe));
    }
}