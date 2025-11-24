package es.luigi.chefsitoLuigi.Dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private Long userId;
    private List<String> allergies;
    private List<String> intolerances;
    private List<String> dislikedIngredients;
    private String dietType;
}