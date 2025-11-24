package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Dto.IngredientDto;
import es.luigi.chefsitoLuigi.Entity.Ingredient;
import es.luigi.chefsitoLuigi.Mapper.IngredientMapper;
import es.luigi.chefsitoLuigi.Repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartIngredientSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SmartIngredientSearchService.class);

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    // Mapa de traducciones comunes para agregar dinámicamente
    private final Map<String, String> commonTranslations = Map.ofEntries(
            Map.entry("tomato", "Tomate"),
            Map.entry("tomate", "Tomato"),
            Map.entry("cebolla", "Onion"),
            Map.entry("onion", "Cebolla"),
            Map.entry("ajo", "Garlic"),
            Map.entry("garlic", "Ajo"),
            Map.entry("pollo", "Chicken"),
            Map.entry("chicken", "Pollo"),
            Map.entry("carne", "Beef"),
            Map.entry("beef", "Carne de Res"),
            Map.entry("pescado", "Fish"),
            Map.entry("fish", "Pescado"),
            Map.entry("arroz", "Rice"),
            Map.entry("rice", "Arroz"),
            Map.entry("pasta", "Pasta"),
            Map.entry("huevo", "Egg"),
            Map.entry("egg", "Huevo"),
            Map.entry("leche", "Milk"),
            Map.entry("milk", "Leche"),
            Map.entry("queso", "Cheese"),
            Map.entry("cheese", "Queso"),
            Map.entry("pan", "Bread"),
            Map.entry("bread", "Pan"),
            Map.entry("papa", "Potato"),
            Map.entry("potato", "Papa"),
            Map.entry("zanahoria", "Carrot"),
            Map.entry("carrot", "Zanahoria"),
            Map.entry("pimiento", "Bell Pepper"),
            Map.entry("bell pepper", "Pimiento"),
            Map.entry("limón", "Lemon"),
            Map.entry("lemon", "Limón"),
            Map.entry("manzana", "Apple"),
            Map.entry("apple", "Manzana"),
            Map.entry("plátano", "Banana"),
            Map.entry("banana", "Plátano")
    );

    public List<IngredientDto> searchIngredients(String query, String language) {
        String cleanQuery = query.toLowerCase().trim();
        logger.debug("🔍 Búsqueda inteligente: '{}' en idioma: {}", cleanQuery, language);

        // Primero buscar en la base de datos existente
        List<IngredientDto> results = ingredientRepository.findAll().stream()
                .filter(ingredient -> matchesIngredient(ingredient, cleanQuery, language))
                .map(ingredientMapper::toDto)
                .collect(Collectors.toList());

        // Si no hay resultados, intentar agregar el ingrediente dinámicamente
        if (results.isEmpty() && canAddIngredientDynamically(cleanQuery)) {
            logger.info("🆕 Ingrediente no encontrado, intentando agregar dinámicamente: {}", cleanQuery);
            Optional<Ingredient> newIngredient = addIngredientDynamically(cleanQuery, language);
            if (newIngredient.isPresent()) {
                results.add(ingredientMapper.toDto(newIngredient.get()));
                logger.info("✅ Ingrediente agregado dinámicamente: {}", cleanQuery);
            }
        }

        logger.info("✅ Encontrados {} ingredientes para: '{}'", results.size(), cleanQuery);
        return results;
    }

    private boolean canAddIngredientDynamically(String query) {
        // Solo agregar dinámicamente si la consulta es un nombre simple (no muy largo)
        return query.length() <= 20 &&
                query.matches("[a-zA-ZáéíóúñüÁÉÍÓÚÑÜ\\s]+") &&
                commonTranslations.containsKey(query.toLowerCase());
    }

    private Optional<Ingredient> addIngredientDynamically(String query, String language) {
        try {
            String normalizedQuery = query.toLowerCase();

            // Determinar nombres en ambos idiomas
            String englishName, spanishName;

            if (language.equalsIgnoreCase("es") || commonTranslations.containsKey(normalizedQuery)) {
                // Si la búsqueda es en español, buscar la traducción al inglés
                spanishName = capitalizeFirstLetter(query);
                englishName = commonTranslations.getOrDefault(normalizedQuery, query);
            } else {
                // Si la búsqueda es en inglés, buscar la traducción al español
                englishName = capitalizeFirstLetter(query);
                spanishName = commonTranslations.getOrDefault(normalizedQuery, query);
            }

            // Verificar si ya existe (por si acaso)
            Optional<Ingredient> existing = ingredientRepository.findByNameIgnoreCase(englishName);
            if (existing.isPresent()) {
                logger.debug("ℹ️ Ingrediente ya existe: {}", englishName);
                return existing;
            }

            // Crear nuevo ingrediente
            Ingredient newIngredient = Ingredient.builder()
                    .name(englishName)
                    .nameEs(spanishName)
                    .unit("units")
                    .quantity(1.0)
                    .imageUrl("https://www.themealdb.com/images/ingredients/" + englishName + ".png")
                    .build();

            Ingredient saved = ingredientRepository.save(newIngredient);
            logger.debug("💾 Nuevo ingrediente guardado: {} (ES: {})", englishName, spanishName);

            return Optional.of(saved);

        } catch (Exception e) {
            logger.error("❌ Error agregando ingrediente dinámicamente '{}': {}", query, e.getMessage());
            return Optional.empty();
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private boolean matchesIngredient(Ingredient ingredient, String query, String language) {
        // Búsqueda exacta en inglés
        boolean matchesEnglish = ingredient.getName().toLowerCase().contains(query);

        // Búsqueda exacta en español
        boolean matchesSpanish = ingredient.getNameEs() != null &&
                ingredient.getNameEs().toLowerCase().contains(query);

        // Búsqueda fonética aproximada
        boolean phoneticMatch = matchesPhonetically(ingredient, query);

        // Búsqueda por similitud (para typos)
        boolean similarityMatch = matchesBySimilarity(ingredient, query);

        boolean result = matchesEnglish || matchesSpanish || phoneticMatch || similarityMatch;

        if (result) {
            logger.debug("🎯 Match: {} (EN: '{}', ES: '{}')",
                    ingredient.getName(), ingredient.getName(), ingredient.getNameEs());
        }

        return result;
    }

    private boolean matchesPhonetically(Ingredient ingredient, String query) {
        String normalizedQuery = normalizeForPhoneticMatch(query);

        String englishName = normalizeForPhoneticMatch(ingredient.getName());
        boolean englishPhonetic = englishName.contains(normalizedQuery);

        boolean spanishPhonetic = false;
        if (ingredient.getNameEs() != null) {
            String spanishName = normalizeForPhoneticMatch(ingredient.getNameEs());
            spanishPhonetic = spanishName.contains(normalizedQuery);
        }

        return englishPhonetic || spanishPhonetic;
    }

    private boolean matchesBySimilarity(Ingredient ingredient, String query) {
        double similarityThreshold = 0.6; // Reducido para ser más permisivo

        boolean englishSimilar = calculateSimilarity(ingredient.getName().toLowerCase(), query) > similarityThreshold;

        boolean spanishSimilar = false;
        if (ingredient.getNameEs() != null) {
            spanishSimilar = calculateSimilarity(ingredient.getNameEs().toLowerCase(), query) > similarityThreshold;
        }

        return englishSimilar || spanishSimilar;
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;

        int minLength = Math.min(s1.length(), s2.length());
        int maxLength = Math.max(s1.length(), s2.length());

        if (minLength == 0) return 0.0;

        int common = 0;
        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                common++;
            }
        }

        return (double) common / maxLength;
    }

    private String normalizeForPhoneticMatch(String text) {
        return text.toLowerCase()
                .replace("tomato", "tomate")
                .replace("potato", "papa")
                .replace("onion", "cebolla")
                .replace("garlic", "ajo")
                .replace("chicken", "pollo")
                .replace("beef", "carne")
                .replace("pork", "cerdo")
                .replace("cheese", "queso")
                .replace("milk", "leche")
                .replace("egg", "huevo")
                .replace("fish", "pescado")
                .replace("rice", "arroz")
                .replace("pasta", "pasta")
                .replace("bread", "pan")
                .replace("flour", "harina")
                .replace("sugar", "azucar")
                .replace("salt", "sal")
                .replace("pepper", "pimienta")
                .replace("oil", "aceite")
                .replace("butter", "mantequilla")
                .replace("lemon", "limon")
                .replace("lime", "lima")
                .replace("apple", "manzana")
                .replace("banana", "platano")
                .replace("orange", "naranja")
                .replaceAll("[^a-z]", "");
    }
}