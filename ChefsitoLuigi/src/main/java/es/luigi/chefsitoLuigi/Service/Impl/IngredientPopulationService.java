package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Entity.Ingredient;
import es.luigi.chefsitoLuigi.Repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientPopulationService {

    private static final Logger logger = LoggerFactory.getLogger(IngredientPopulationService.class);

    private final IngredientRepository ingredientRepository;
    private final IngredientTranslationService translationService;

    @EventListener(ApplicationReadyEvent.class)
    public void populateIngredientsOnStartup() {
        if (ingredientRepository.count() == 0) {
            logger.info("🏗️ Iniciando poblado automático de ingredientes...");

            try {
                // Opción 1: Traducir automáticamente desde TheMealDB
                translationService.translateAndSaveIngredients();
                logger.info("✅ Poblado automático completado");
            } catch (Exception e) {
                logger.error("❌ Error en poblado automático: {}", e.getMessage(), e);
                // Opción de fallback: cargar ingredientes básicos
                loadBasicIngredients();
            }

        } else {
            logger.info("ℹ️ Ya existen {} ingredientes en la BD", ingredientRepository.count());
            // Verificar si necesitan traducción
            long untranslatedCount = ingredientRepository.findAll().stream()
                    .filter(ing -> ing.getNameEs() == null || ing.getNameEs().isEmpty())
                    .count();

            if (untranslatedCount > 0) {
                logger.info("🔄 {} ingredientes necesitan traducción, ejecutando actualización...", untranslatedCount);
                try {
                    translationService.translateAndSaveIngredients();
                } catch (Exception e) {
                    logger.error("❌ Error actualizando traducciones: {}", e.getMessage());
                }
            }
        }
    }

    // Método para población manual (mantener compatibilidad)
    public void populateIngredientsFromMealDB() {
        if (ingredientRepository.count() == 0) {
            translationService.translateAndSaveIngredients();
        } else {
            logger.info("⚠️ Ya existen ingredientes, no se necesita poblar");
        }
    }

    // Fallback: ingredientes básicos si falla la traducción
    private void loadBasicIngredients() {
        try {
            logger.info("🔄 Cargando ingredientes básicos de respaldo...");

            // Lista básica de ingredientes en ambos idiomas
            var basicIngredients = java.util.List.of(
                    new Object[]{"Chicken", "Pollo", "Meat"},
                    new Object[]{"Beef", "Carne de Res", "Meat"},
                    new Object[]{"Tomato", "Tomate", "Vegetables"},
                    new Object[]{"Onion", "Cebolla", "Vegetables"},
                    new Object[]{"Garlic", "Ajo", "Vegetables"},
                    new Object[]{"Potato", "Papa", "Vegetables"},
                    new Object[]{"Rice", "Arroz", "Grains"},
                    new Object[]{"Pasta", "Pasta", "Grains"},
                    new Object[]{"Egg", "Huevo", "Dairy"},
                    new Object[]{"Milk", "Leche", "Dairy"},
                    new Object[]{"Cheese", "Queso", "Dairy"},
                    new Object[]{"Bread", "Pan", "Grains"}
            );

            for (Object[] ingredientData : basicIngredients) {
                String englishName = (String) ingredientData[0];
                String spanishName = (String) ingredientData[1];
                String category = (String) ingredientData[2];

                if (ingredientRepository.findByNameIgnoreCase(englishName).isEmpty()) {
                    Ingredient ingredient = Ingredient.builder()
                            .name(englishName)
                            .nameEs(spanishName)
                            .unit("units")
                            .quantity(1.0)
                            .imageUrl("https://www.themealdb.com/images/ingredients/" + englishName + ".png")
                            .build();

                    ingredientRepository.save(ingredient);
                }
            }

            logger.info("✅ Ingredientes básicos cargados");
        } catch (Exception e) {
            logger.error("❌ Error cargando ingredientes básicos: {}", e.getMessage(), e);
        }
    }
}