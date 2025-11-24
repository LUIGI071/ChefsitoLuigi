package es.luigi.chefsitoLuigi.Dto.OpenAI;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiRecipeResponse {
    private String openAiId; // ID único generado para la receta
    private String title;
    private String description;
    private List<String> ingredients;
    private List<String> instructions;
    private Integer preparationTime;
    private String difficulty;
    private String category;
}