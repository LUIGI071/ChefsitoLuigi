package es.luigi.chefsitoLuigi.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemCreateRequest {
    @NotNull(message = "{pantryitem.ingredientid.notnull}")
    private Long ingredientId;

    @NotNull(message = "{pantryitem.quantity.notnull}")
    @Positive(message = "{pantryitem.quantity.positive}")
    private Double quantity;

    // NO incluir userId - se asigna automáticamente
}