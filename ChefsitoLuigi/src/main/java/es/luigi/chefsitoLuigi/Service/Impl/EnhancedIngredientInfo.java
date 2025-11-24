package es.luigi.chefsitoLuigi.Service.Impl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnhancedIngredientInfo {
    private String englishName;
    private String spanishName;
    private String unit;
    private Double quantity;
    private String imageUrl;
}