package es.luigi.chefsitoLuigi.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {

    private Long id;

    @NotBlank(message = "{message.ingredient.name.required}")
    @Size(min = 2, max = 100, message = "{message.ingredient.name.size}")
    private String name;

    // NUEVO: Nombre en español
    private String nameEs;

    @NotBlank(message = "{message.ingredient.unit.required}")
    @Size(max = 20, message = "{message.ingredient.unit.size}")
    private String unit;

    @NotNull(message = "{message.ingredient.quantity.required}")
    @Positive(message = "{message.ingredient.quantity.positive}")
    private Double quantity;

    @Pattern(
            regexp = "^(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|gif|png|jpeg)?$",
            message = "{message.ingredient.imageUrl.pattern}"
    )
    private String imageUrl;

    @FutureOrPresent(message = "{message.ingredient.expiryDate.future}")
    private LocalDate expiryDate;
}