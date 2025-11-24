package es.luigi.chefsitoLuigi.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemDto {
    private Long id;
    @NotNull(message = "{pantryitem.userid.notnull}")
    private Long userId;
    @NotNull(message = "{pantryitem.ingredientid.notnull}")
    private Long ingredientId;
    @NotNull(message = "{pantryitem.quantity.notnull}")
    @Positive(message = "{pantryitem.quantity.positive}")
    private Double quantity;
}