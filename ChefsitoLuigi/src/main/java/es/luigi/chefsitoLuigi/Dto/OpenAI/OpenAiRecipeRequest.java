package es.luigi.chefsitoLuigi.Dto.OpenAI;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiRecipeRequest {
    private Long userId;
    private List<String> availableIngredients;
    private UserPreferencesDto preferences;
    private int maxRecipes;
}