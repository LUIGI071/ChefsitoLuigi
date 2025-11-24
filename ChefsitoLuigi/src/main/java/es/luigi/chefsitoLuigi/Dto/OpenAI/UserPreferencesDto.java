package es.luigi.chefsitoLuigi.Dto.OpenAI;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDto {
    private List<String> allergies;
    private List<String> intolerances;
    private List<String> dislikedIngredients;
    private String dietType;
    private String cookingSkillLevel;
}