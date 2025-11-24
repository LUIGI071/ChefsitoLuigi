package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.OpenAI.OpenAiRecipeRequest;
import es.luigi.chefsitoLuigi.Dto.OpenAI.OpenAiRecipeResponse;
import es.luigi.chefsitoLuigi.Service.OpenAiRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class OpenAiRecipeController {

    private final OpenAiRecipeService openAiRecipeService;

    @PostMapping("/by-ingredients")
    @PreAuthorize("#request.userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<OpenAiRecipeResponse>> getRecipesByIngredients(@RequestBody OpenAiRecipeRequest request) {
        System.out.println("🧠 Usuario " + request.getUserId() + " solicitando recetas por ingredientes");
        List<OpenAiRecipeResponse> recipes = openAiRecipeService.getRecipeRecommendations(request);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/for-user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<List<OpenAiRecipeResponse>> getRecipesForUser(@PathVariable Long userId) {
        System.out.println("🧠 Usuario " + userId + " solicitando sus recetas recomendadas");
        List<OpenAiRecipeResponse> recipes = openAiRecipeService.getRecipeRecommendationsForUser(userId);
        return ResponseEntity.ok(recipes);
    }
}