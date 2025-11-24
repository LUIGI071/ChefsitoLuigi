package es.luigi.chefsitoLuigi.Dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryItemResponse {
    private Long id;
    private Long userId;
    private Long ingredientId;
    private Double quantity;

    // Opcional: incluir información del ingrediente para facilidad del frontend
    private String ingredientName;
    private String ingredientNameEs;
    private String ingredientImageUrl;
}