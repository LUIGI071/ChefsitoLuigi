package es.luigi.chefsitoLuigi.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.luigi.chefsitoLuigi.Entity.Ingredient;
import es.luigi.chefsitoLuigi.Repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class IngredientTranslationService {

    private static final Logger logger = LoggerFactory.getLogger(IngredientTranslationService.class);

    private final IngredientRepository ingredientRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    public void translateAndSaveIngredients() {
        try {
            logger.info("🌐 Obteniendo ingredientes de TheMealDB...");

            // Obtener ingredientes de TheMealDB
            List<String> englishIngredients = fetchIngredientsFromTheMealDB();
            logger.info("✅ Obtenidos {} ingredientes de TheMealDB", englishIngredients.size());

            if (englishIngredients.isEmpty()) {
                logger.warn("❌ No se obtuvieron ingredientes de TheMealDB");
                loadBasicIngredients();
                return;
            }

            // Traducir al español
            logger.info("🔤 Traduciendo ingredientes al español...");
            Map<String, String> translatedIngredients = translateIngredients(englishIngredients);

            // Guardar en la base de datos
            saveTranslatedIngredients(translatedIngredients);

            logger.info("🎉 Traducción completada exitosamente");

        } catch (Exception e) {
            logger.error("❌ Error en traducción: {}", e.getMessage(), e);
            logger.info("🔄 Cargando ingredientes básicos como fallback...");
            loadBasicIngredients();
        }
    }

    public List<String> fetchIngredientsFromTheMealDB() {
        try {
            String url = "https://www.themealdb.com/api/json/v1/1/list.php?i=list";
            logger.debug("📡 Haciendo request a: {}", url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                logger.error("❌ Respuesta nula de TheMealDB");
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode meals = root.get("meals");

            if (meals == null || !meals.isArray()) {
                logger.error("❌ Formato de respuesta inválido de TheMealDB");
                return Collections.emptyList();
            }

            List<String> ingredients = new ArrayList<>();
            for (JsonNode meal : meals) {
                String name = meal.get("strIngredient").asText();
                if (name != null && !name.trim().isEmpty()) {
                    ingredients.add(name);
                }
            }

            logger.debug("📋 Ingredientes obtenidos: {}", ingredients);
            return ingredients;
        } catch (Exception e) {
            logger.error("❌ Error obteniendo ingredientes de TheMealDB: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Map<String, String> translateIngredients(List<String> ingredients) {
        try {
            // Dividir en lotes más pequeños para evitar límites de tokens
            List<List<String>> batches = createBatches(ingredients, 30);
            Map<String, String> allTranslations = new HashMap<>();

            for (int i = 0; i < batches.size(); i++) {
                logger.info("📦 Procesando lote {}/{}", (i + 1), batches.size());
                List<String> batch = batches.get(i);
                Map<String, String> batchTranslations = translateBatch(batch);
                allTranslations.putAll(batchTranslations);

                // Pequeña pausa entre lotes para evitar rate limiting
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            logger.info("✅ Total de traducciones obtenidas: {}", allTranslations.size());
            return allTranslations;
        } catch (Exception e) {
            logger.error("❌ Error en traducción: {}", e.getMessage(), e);
            return createFallbackTranslations(ingredients);
        }
    }

    private Map<String, String> translateBatch(List<String> ingredients) {
        try {
            // Verificar si OpenAI está configurado
            if (openaiApiKey == null || openaiApiKey.trim().isEmpty() || openaiApiKey.startsWith("${")) {
                logger.warn("⚠️ OpenAI API Key no configurada, usando traducciones de fallback");
                return createFallbackTranslations(ingredients);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            String prompt = buildTranslationPrompt(ingredients);
            logger.debug("📝 Prompt construido para {} ingredientes", ingredients.size());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.3);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            logger.debug("🚀 Enviando request a OpenAI...");
            ResponseEntity<Map> response = restTemplate.exchange(openaiApiUrl, HttpMethod.POST, entity, Map.class);

            String translatedText = extractContentFromResponse(response);
            Map<String, String> translations = parseTranslationResponse(translatedText, ingredients);

            logger.debug("✅ Lote traducido: {} ingredientes", translations.size());
            return translations;

        } catch (Exception e) {
            logger.error("❌ Error traduciendo lote: {}", e.getMessage());
            return createFallbackTranslations(ingredients);
        }
    }

    private String buildTranslationPrompt(List<String> ingredients) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Traduce SOLO los nombres de estos ingredientes culinarios del inglés al español. \n\n");
        prompt.append("IMPORTANTE: Devuelve ÚNICAMENTE un objeto JSON con esta estructura exacta:\n");
        prompt.append("{\n");
        prompt.append("  \"ingredient1_english\": \"traducción_español\",\n");
        prompt.append("  \"ingredient2_english\": \"traducción_español\"\n");
        prompt.append("}\n\n");
        prompt.append("Lista de ingredientes a traducir:\n");

        for (String ingredient : ingredients) {
            prompt.append("- ").append(ingredient).append("\n");
        }

        prompt.append("\nReglas:\n");
        prompt.append("- Mantén términos técnicos (ej: 'worcestershire sauce' -> 'salsa worcestershire')\n");
        prompt.append("- Usa español neutro/latino\n");
        prompt.append("- No añadas texto adicional, solo el JSON\n");
        prompt.append("- Si no conoces una traducción, usa el nombre en inglés\n");

        return prompt.toString();
    }

    private String extractContentFromResponse(ResponseEntity<Map> response) {
        try {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    logger.debug("📨 Respuesta de OpenAI recibida");
                    return content;
                }
            }
            logger.warn("⚠️ Respuesta de OpenAI sin contenido esperado");
        } catch (Exception e) {
            logger.error("❌ Error extrayendo contenido de respuesta: {}", e.getMessage());
        }
        return "{}";
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseTranslationResponse(String response, List<String> originalIngredients) {
        try {
            // Limpiar la respuesta
            String cleanResponse = response.replace("```json", "").replace("```", "").trim();
            logger.debug("🧹 Respuesta limpiada: {}", cleanResponse.substring(0, Math.min(100, cleanResponse.length())) + "...");

            Map<String, String> translations = objectMapper.readValue(cleanResponse, Map.class);
            logger.info("✅ {} traducciones parseadas correctamente", translations.size());

            // Verificar que todas las traducciones estén presentes
            for (String ingredient : originalIngredients) {
                if (!translations.containsKey(ingredient)) {
                    logger.warn("⚠️ Ingrediente sin traducción: {}", ingredient);
                    translations.put(ingredient, ingredient); // Usar el original como fallback
                }
            }

            return translations;
        } catch (Exception e) {
            logger.error("❌ Error parseando traducciones JSON: {}", e.getMessage());
            logger.debug("Respuesta original: {}", response);
            return createFallbackTranslations(originalIngredients);
        }
    }

    private Map<String, String> createFallbackTranslations(List<String> ingredients) {
        logger.info("🔄 Usando traducciones de fallback para {} ingredientes", ingredients.size());

        Map<String, String> fallback = new HashMap<>();
        Map<String, String> commonTranslations = getCommonTranslations();

        for (String ingredient : ingredients) {
            String lowerIngredient = ingredient.toLowerCase();
            String translation = commonTranslations.getOrDefault(lowerIngredient, ingredient);
            fallback.put(ingredient, translation);
            logger.debug("🔤 Fallback: {} -> {}", ingredient, translation);
        }

        return fallback;
    }

    private Map<String, String> getCommonTranslations() {
        Map<String, String> translations = new HashMap<>();
        // Ingredientes comunes
        translations.put("chicken", "Pollo");
        translations.put("beef", "Carne de Res");
        translations.put("pork", "Cerdo");
        translations.put("fish", "Pescado");
        translations.put("salmon", "Salmón");
        translations.put("tuna", "Atún");
        translations.put("tomato", "Tomate");
        translations.put("onion", "Cebolla");
        translations.put("garlic", "Ajo");
        translations.put("potato", "Papa");
        translations.put("carrot", "Zanahoria");
        translations.put("bell pepper", "Pimiento");
        translations.put("rice", "Arroz");
        translations.put("pasta", "Pasta");
        translations.put("egg", "Huevo");
        translations.put("milk", "Leche");
        translations.put("cheese", "Queso");
        translations.put("bread", "Pan");
        translations.put("flour", "Harina");
        translations.put("sugar", "Azúcar");
        translations.put("salt", "Sal");
        translations.put("pepper", "Pimienta");
        translations.put("oil", "Aceite");
        translations.put("butter", "Mantequilla");
        translations.put("lemon", "Limón");
        translations.put("lime", "Lima");
        translations.put("apple", "Manzana");
        translations.put("banana", "Plátano");
        translations.put("orange", "Naranja");
        // Añade más según necesites

        return translations;
    }

    public void saveTranslatedIngredients(Map<String, String> translations) {
        try {
            List<Ingredient> ingredientsToSave = new ArrayList<>();
            int savedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;

            for (Map.Entry<String, String> entry : translations.entrySet()) {
                String englishName = entry.getKey();
                String spanishName = entry.getValue();

                // Buscar por nombre (case insensitive)
                Optional<Ingredient> existingOpt = ingredientRepository.findByNameIgnoreCase(englishName);

                if (existingOpt.isPresent()) {
                    // ACTUALIZAR ingrediente existente
                    Ingredient existing = existingOpt.get();
                    if (existing.getNameEs() == null || !existing.getNameEs().equals(spanishName)) {
                        existing.setNameEs(spanishName);
                        ingredientsToSave.add(existing);
                        updatedCount++;
                        logger.debug("🔄 Actualizando: {} -> {}", englishName, spanishName);
                    } else {
                        skippedCount++;
                    }
                } else {
                    // CREAR nuevo ingrediente
                    Ingredient ingredient = Ingredient.builder()
                            .name(englishName)
                            .nameEs(spanishName)
                            .unit("units")
                            .quantity(1.0)
                            .imageUrl("https://www.themealdb.com/images/ingredients/" + englishName + ".png")
                            .build();
                    ingredientsToSave.add(ingredient);
                    savedCount++;
                    logger.debug("➕ Nuevo: {} -> {}", englishName, spanishName);
                }
            }

            if (!ingredientsToSave.isEmpty()) {
                ingredientRepository.saveAll(ingredientsToSave);
                logger.info("💾 Guardados: {} nuevos, {} actualizados, {} sin cambios",
                        savedCount, updatedCount, skippedCount);
            } else {
                logger.info("ℹ️ No hay cambios para guardar");
            }

        } catch (Exception e) {
            logger.error("❌ Error guardando ingredientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error guardando ingredientes traducidos", e);
        }
    }

    private void loadBasicIngredients() {
        try {
            logger.info("🔄 Cargando ingredientes básicos de respaldo...");

            List<Object[]> basicIngredients = Arrays.asList(
                    new Object[]{"Chicken", "Pollo"},
                    new Object[]{"Beef", "Carne de Res"},
                    new Object[]{"Tomato", "Tomate"},
                    new Object[]{"Onion", "Cebolla"},
                    new Object[]{"Garlic", "Ajo"},
                    new Object[]{"Potato", "Papa"},
                    new Object[]{"Rice", "Arroz"},
                    new Object[]{"Pasta", "Pasta"},
                    new Object[]{"Egg", "Huevo"},
                    new Object[]{"Milk", "Leche"},
                    new Object[]{"Cheese", "Queso"},
                    new Object[]{"Bread", "Pan"},
                    new Object[]{"Salmon", "Salmón"},
                    new Object[]{"Carrot", "Zanahoria"},
                    new Object[]{"Bell Pepper", "Pimiento"},
                    new Object[]{"Lemon", "Limón"},
                    new Object[]{"Apple", "Manzana"},
                    new Object[]{"Banana", "Plátano"}
            );

            for (Object[] ingredientData : basicIngredients) {
                String englishName = (String) ingredientData[0];
                String spanishName = (String) ingredientData[1];

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

            logger.info("✅ {} ingredientes básicos cargados", basicIngredients.size());
        } catch (Exception e) {
            logger.error("❌ Error cargando ingredientes básicos: {}", e.getMessage(), e);
        }
    }

    private <T> List<List<T>> createBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }
}