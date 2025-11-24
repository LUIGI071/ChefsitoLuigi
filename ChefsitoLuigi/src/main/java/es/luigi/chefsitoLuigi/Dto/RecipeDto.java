package es.luigi.chefsitoLuigi.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {

    private Long id;

    @NotBlank(message = "{recipe.title.notblank}")
    @Size(min = 3, max = 100, message = "{recipe.title.size}")
    private String title;

    @NotBlank(message = "{recipe.instructions.notblank}")
    @Size(min = 10, max = 4000, message = "{recipe.instructions.size}")
    private String instructions;

    @NotNull(message = "{recipe.preparationminutes.notnull}")
    @Min(value = 1, message = "{recipe.preparationminutes.min}")
    private Integer preparationMinutes;

    @NotBlank(message = "{recipe.category.notblank}")
    private String category;

    @NotEmpty(message = "{recipe.ingredientids.notempty}")
    private List<Long> ingredientIds;
}