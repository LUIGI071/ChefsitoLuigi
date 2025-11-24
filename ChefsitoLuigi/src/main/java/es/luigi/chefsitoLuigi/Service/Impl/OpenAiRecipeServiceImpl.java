package es.luigi.chefsitoLuigi.Service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.luigi.chefsitoLuigi.Dto.OpenAI.OpenAiRecipeRequest;
import es.luigi.chefsitoLuigi.Dto.OpenAI.OpenAiRecipeResponse;
import es.luigi.chefsitoLuigi.Dto.OpenAI.UserPreferencesDto;
import es.luigi.chefsitoLuigi.Dto.UserProfileDto;
import es.luigi.chefsitoLuigi.Entity.Ingredient;
import es.luigi.chefsitoLuigi.Entity.PantryItem;
import es.luigi.chefsitoLuigi.Entity.Recipe;
import es.luigi.chefsitoLuigi.Entity.User;
import es.luigi.chefsitoLuigi.Repository.PantryItemRepository;
import es.luigi.chefsitoLuigi.Repository.RecipeRepository;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import es.luigi.chefsitoLuigi.Service.OpenAiRecipeService;
import es.luigi.chefsitoLuigi.Service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenAiRecipeServiceImpl implements OpenAiRecipeService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiRecipeServiceImpl.class);

    private final PantryItemRepository pantryItemRepository;
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final RecipeRepository recipeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache simple en memoria para evitar llamadas repetidas a OpenAI
    private final Map<String, List<OpenAiRecipeResponse>> recipeCache = new HashMap<>();
    private final Map<Long, Long> userLastRequestTime = new HashMap<>();
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutos

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Override
    public List<OpenAiRecipeResponse> getRecipeRecommendations(OpenAiRecipeRequest request) {
        try {
            // Verificar si ya tenemos recetas en caché para estos ingredientes
            String cacheKey = generateCacheKey(request);
            List<OpenAiRecipeResponse> cachedRecipes = getCachedRecipes(cacheKey, request.getUserId());

            if (cachedRecipes != null && !cachedRecipes.isEmpty()) {
                logger.info("✅ Devolviendo recetas desde caché para usuario: {}", request.getUserId());
                return cachedRecipes;
            }

            logger.debug("Construyendo prompt para OpenAI...");
            String prompt = buildRecipePrompt(request);
            String aiResponse = callOpenAiApi(prompt);
            List<OpenAiRecipeResponse> recipes = parseAiResponse(aiResponse);

            // Limitar a máximo 5 recetas
            if (recipes.size() > 5) {
                recipes = recipes.subList(0, 5);
                logger.debug("Recetas limitadas a 5 de {}", recipes.size());
            }

            // Guardar en caché
            cacheRecipes(cacheKey, recipes, request.getUserId());

            // Guardar recetas en historial si tenemos userId
            if (request.getUserId() != null) {
                saveRecipesToHistory(recipes, request.getUserId());
            }

            logger.info("✅ {} recetas generadas exitosamente para usuario {}", recipes.size(), request.getUserId());
            return recipes;
        } catch (Exception e) {
            logger.error("❌ Error generando recomendaciones de recetas: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<OpenAiRecipeResponse> getRecipeRecommendationsForUser(Long userId) {
        try {
            // Verificar si el usuario hizo una solicitud reciente
            if (isRecentRequest(userId)) {
                logger.debug("⏰ Usuario {} ya hizo una solicitud reciente, verificando caché...", userId);
            }

            // Obtener ingredientes del usuario CON INFORMACIÓN MEJORADA
            List<PantryItem> pantryItems = pantryItemRepository.findByUserId(userId);
            List<EnhancedIngredientInfo> enhancedIngredients = pantryItems.stream()
                    .map(this::mapToEnhancedIngredientInfo)
                    .collect(Collectors.toList());

            List<String> availableIngredientsForPrompt = enhancedIngredients.stream()
                    .map(this::formatIngredientForPrompt)
                    .collect(Collectors.toList());

            // DEBUG: Mostrar ingredientes del usuario
            logger.debug("🧪 Usuario {} tiene ingredientes mejorados: {}", userId, availableIngredientsForPrompt);
            enhancedIngredients.forEach(ingredient ->
                    logger.debug("  - EN: '{}', ES: '{}', Cantidad: {} {}",
                            ingredient.getEnglishName(),
                            ingredient.getSpanishName(),
                            ingredient.getQuantity(),
                            ingredient.getUnit())
            );

            if (enhancedIngredients.isEmpty()) {
                logger.warn("❌ Usuario {} no tiene ingredientes en la despensa", userId);
                return Collections.emptyList();
            }

            // Obtener preferencias del usuario
            UserProfileDto userProfileDto = userProfileService.findByUserId(userId)
                    .orElse(UserProfileDto.builder().build());

            UserPreferencesDto preferences = UserPreferencesDto.builder()
                    .allergies(userProfileDto.getAllergies() != null ? userProfileDto.getAllergies() : Collections.emptyList())
                    .intolerances(userProfileDto.getIntolerances() != null ? userProfileDto.getIntolerances() : Collections.emptyList())
                    .dislikedIngredients(userProfileDto.getDislikedIngredients() != null ? userProfileDto.getDislikedIngredients() : Collections.emptyList())
                    .dietType(userProfileDto.getDietType())
                    .build();

            // Verificar si ya existen recetas recomendadas recientemente para este usuario
            List<Recipe> recentRecipes = recipeRepository.findByRecommendedToUsersId(userId);
            if (!recentRecipes.isEmpty()) {
                logger.debug("📚 Usuario {} ya tiene {} recetas en historial", userId, recentRecipes.size());
            }

            OpenAiRecipeRequest request = OpenAiRecipeRequest.builder()
                    .userId(userId)
                    .availableIngredients(availableIngredientsForPrompt)
                    .preferences(preferences)
                    .maxRecipes(5)
                    .build();

            List<OpenAiRecipeResponse> recipes = getRecipeRecommendations(request);

            // Actualizar tiempo de última solicitud
            userLastRequestTime.put(userId, System.currentTimeMillis());

            return recipes;

        } catch (Exception e) {
            logger.error("❌ Error obteniendo recomendaciones para usuario {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private EnhancedIngredientInfo mapToEnhancedIngredientInfo(PantryItem pantryItem) {
        Ingredient ingredient = pantryItem.getIngredient();
        return EnhancedIngredientInfo.builder()
                .englishName(ingredient.getName())
                .spanishName(ingredient.getNameEs())
                .unit(ingredient.getUnit())
                .quantity(pantryItem.getQuantity())
                .imageUrl(ingredient.getImageUrl())
                .build();
    }

    private String formatIngredientForPrompt(EnhancedIngredientInfo ingredient) {
        StringBuilder formatted = new StringBuilder();
        formatted.append(ingredient.getEnglishName());

        // Agregar nombre en español si está disponible
        if (ingredient.getSpanishName() != null && !ingredient.getSpanishName().isEmpty()) {
            formatted.append(" (").append(ingredient.getSpanishName()).append(")");
        }

        // Agregar cantidad si está disponible
        if (ingredient.getQuantity() != null && ingredient.getUnit() != null) {
            formatted.append(" - ").append(ingredient.getQuantity()).append(" ").append(ingredient.getUnit());
        }

        return formatted.toString();
    }

    private String generateCacheKey(OpenAiRecipeRequest request) {
        String ingredientsKey = String.join(",", request.getAvailableIngredients());
        String preferencesKey = "";

        if (request.getPreferences() != null) {
            UserPreferencesDto prefs = request.getPreferences();
            preferencesKey = String.join(",",
                    prefs.getAllergies() != null ? prefs.getAllergies() : Collections.emptyList()) +
                    String.join(",", prefs.getIntolerances() != null ? prefs.getIntolerances() : Collections.emptyList()) +
                    String.join(",", prefs.getDislikedIngredients() != null ? prefs.getDislikedIngredients() : Collections.emptyList()) +
                    (prefs.getDietType() != null ? prefs.getDietType() : "");
        }

        return (ingredientsKey + preferencesKey).hashCode() + "_" + request.getUserId();
    }

    private List<OpenAiRecipeResponse> getCachedRecipes(String cacheKey, Long userId) {
        Long lastRequestTime = userLastRequestTime.get(userId);
        if (lastRequestTime != null && (System.currentTimeMillis() - lastRequestTime) < CACHE_DURATION_MS) {
            return recipeCache.get(cacheKey);
        }
        return null;
    }

    private void cacheRecipes(String cacheKey, List<OpenAiRecipeResponse> recipes, Long userId) {
        recipeCache.put(cacheKey, recipes);
        userLastRequestTime.put(userId, System.currentTimeMillis());
        logger.debug("💾 Recetas guardadas en caché para usuario: {}", userId);
    }

    private boolean isRecentRequest(Long userId) {
        Long lastRequest = userLastRequestTime.get(userId);
        return lastRequest != null && (System.currentTimeMillis() - lastRequest) < CACHE_DURATION_MS;
    }

    private String buildRecipePrompt(OpenAiRecipeRequest request) {
        logger.debug("Construyendo prompt de recetas MEJORADO...");
        StringBuilder prompt = new StringBuilder();

        prompt.append("Como chef experto, genera ").append(request.getMaxRecipes())
                .append(" recetas de cocina prácticas, creativas y deliciosas usando principalmente estos ingredientes:\n\n");

        // Lista detallada de ingredientes
        for (String ingredient : request.getAvailableIngredients()) {
            prompt.append("• ").append(ingredient).append("\n");
        }

        prompt.append("\nINFORMACIÓN DE INGREDIENTES:\n");
        prompt.append("- Los ingredientes se muestran en formato: 'NombreInglés (NombreEspañol) - Cantidad Unidad'\n");
        prompt.append("- Usa ambos nombres (inglés y español) como referencia\n");
        prompt.append("- Considera las cantidades disponibles para las proporciones\n\n");

        if (request.getPreferences() != null) {
            UserPreferencesDto prefs = request.getPreferences();

            if (prefs.getAllergies() != null && !prefs.getAllergies().isEmpty()) {
                prompt.append("🚫 EXCLUIR ABSOLUTAMENTE estos alimentos por alergias: ")
                        .append(String.join(", ", prefs.getAllergies())).append("\n");
            }
            if (prefs.getIntolerances() != null && !prefs.getIntolerances().isEmpty()) {
                prompt.append("🚫 EXCLUIR estos ingredientes por intolerancias: ")
                        .append(String.join(", ", prefs.getIntolerances())).append("\n");
            }
            if (prefs.getDislikedIngredients() != null && !prefs.getDislikedIngredients().isEmpty()) {
                prompt.append("👎 Evitar estos ingredientes que no le gustan al usuario: ")
                        .append(String.join(", ", prefs.getDislikedIngredients())).append("\n");
            }
            if (prefs.getDietType() != null && !prefs.getDietType().isEmpty()) {
                prompt.append("🥗 Respetar estrictamente dieta: ").append(prefs.getDietType()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("IMPORTANTE CRÍTICO: Las recetas deben ser ESPECÍFICAS y DETALLADAS. No uses descripciones genéricas.");
        prompt.append("\n\nINSTRUCCIONES ESPECÍFICAS PARA LAS RECETAS:");
        prompt.append("\n- description: Debe ser detallada, mencionar sabores, texturas, origen culinario, y por qué es especial");
        prompt.append("\n- instructions: Deben ser PASOS ESPECÍFICOS con temperaturas exactas, tiempos de cocción, técnicas culinarias");
        prompt.append("\n- ingredients: Incluir cantidades aproximadas y preparaciones específicas (ej: 'cebolla picada finamente', 'ajo machacado')");
        prompt.append("\n- preparationTime: Tiempo real considerando preparación y cocción");

        prompt.append("\n\nFORMATO EXACTO REQUERIDO - Devuelve SOLO un array JSON válido:");
        prompt.append("\n- title: string (nombre creativo y específico EN ESPAÑOL)");
        prompt.append("\n- description: string (descripción detallada de sabores, texturas, origen EN ESPAÑOL)");
        prompt.append("\n- ingredients: array de strings (con cantidades y preparaciones EN ESPAÑOL: '2 cebollas picadas', '3 dientes de ajo machacados')");
        prompt.append("\n- instructions: array de strings (pasos numerados ESPECÍFICOS con tiempos y temperaturas EN ESPAÑOL)");
        prompt.append("\n- preparationTime: number (tiempo total real en minutos)");
        prompt.append("\n- difficulty: string (fácil, medio, difícil)");
        prompt.append("\n- category: string (desayuno, almuerzo, cena, postre, etc.)");

        prompt.append("\n\nEJEMPLO DE FORMATO CORRECTO (MUY ESPECÍFICO):\n");
        prompt.append("""
                [
                  {
                    "title": "Risotto Cremoso de Champiñones Silvestres",
                    "description": "Risotto italiano cremoso con una mezcla de champiñones silvestres salteados en mantequilla y ajo, terminado con queso parmesano rallado y perejil fresco. La textura es cremosa y los champiñones aportan un sabor terroso y umami.",
                    "ingredients": [
                      "1 taza de arroz arbóreo",
                      "300g de mezcla de champiñones silvestres (portobello, shiitake, cremini)",
                      "1 cebolla picada finamente",
                      "3 dientes de ajo machacados",
                      "1/2 taza de vino blanco seco",
                      "4 tazas de caldo de vegetales caliente",
                      "1/2 taza de queso parmesano rallado",
                      "3 cucharadas de mantequilla",
                      "2 cucharadas de aceite de oliva",
                      "Sal y pimienta negra al gusto",
                      "Perejil fresco picado para decorar"
                    ],
                    "instructions": [
                      "1. Calentar el caldo de vegetales en una olla y mantenerlo caliente a fuego bajo",
                      "2. En una cazuela grande, calentar el aceite de oliva y 1 cucharada de mantequilla a fuego medio",
                      "3. Saltear la cebolla picada por 5 minutos hasta que esté transparente, añadir el ajo y cocinar 1 minuto más",
                      "4. Añadir los champiñones laminados y saltear por 8-10 minutos hasta que estén dorados",
                      "5. Incorporar el arroz arbóreo y tostar durante 2 minutos, revolviendo constantemente",
                      "6. Verter el vino blanco y cocinar hasta que se evapore completamente",
                      "7. Añadir el caldo caliente, un cucharón a la vez, revolviendo constantemente hasta que el líquido sea absorbido antes de añadir más (proceso de 18-20 minutos)",
                      "8. Cuando el arroz esté al dente y cremoso, retirar del fuego y añadir el parmesano y la mantequilla restante",
                      "9. Tapar y dejar reposar 2 minutos antes de servir con perejil fresco"
                    ],
                    "preparationTime": 45,
                    "difficulty": "medio",
                    "category": "almuerzo"
                  }
                ]
                """);

        return prompt.toString();
    }

    private String callOpenAiApi(String prompt) {
        try {
            logger.debug("Llamando a API de OpenAI...");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", 4000);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(openaiApiUrl, HttpMethod.POST, entity, Map.class);
            logger.debug("✅ Respuesta recibida de OpenAI");

            return extractContentFromResponse(response);
        } catch (Exception e) {
            logger.error("❌ Error llamando a API de OpenAI: {}", e.getMessage(), e);
            return "[]";
        }
    }

    private String extractContentFromResponse(ResponseEntity<Map> response) {
        try {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            logger.error("Error extrayendo contenido de respuesta OpenAI: {}", e.getMessage());
        }
        return "[]";
    }

    private List<OpenAiRecipeResponse> parseAiResponse(String aiResponse) {
        try {
            String cleanResponse = aiResponse.replace("```json", "").replace("```", "").trim();
            List<OpenAiRecipeResponse> recipes = objectMapper.readValue(
                    cleanResponse,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OpenAiRecipeResponse.class)
            );
            logger.debug("✅ {} recetas parseadas exitosamente", recipes.size());
            return recipes;
        } catch (JsonProcessingException e) {
            logger.error("❌ Error parseando respuesta OpenAI: {}", e.getMessage());
            logger.debug("Raw response: {}", aiResponse);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("❌ Error inesperado parseando respuesta: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void saveRecipesToHistory(List<OpenAiRecipeResponse> aiRecipes, Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("❌ Usuario no encontrado para ID: {}", userId);
                return;
            }

            for (OpenAiRecipeResponse aiRecipe : aiRecipes) {
                String openAiId = generateOpenAiId(aiRecipe);
                logger.debug("🔑 Generated OpenAiId: {} for recipe: {}", openAiId, aiRecipe.getTitle());

                Optional<Recipe> existingRecipeOpt = recipeRepository.findByOpenAiId(openAiId);
                Recipe existingRecipe;

                if (existingRecipeOpt.isEmpty()) {
                    Recipe newRecipe = Recipe.builder()
                            .title(aiRecipe.getTitle())
                            .description(aiRecipe.getDescription())
                            .instructions(String.join("\n", aiRecipe.getInstructions()))
                            .preparationTime(aiRecipe.getPreparationTime())
                            .category(aiRecipe.getCategory())
                            .difficulty(aiRecipe.getDifficulty())
                            .source("openai")
                            .openAiId(openAiId)
                            .ingredientNames(aiRecipe.getIngredients())
                            .recommendedToUsers(new ArrayList<>())
                            .build();
                    existingRecipe = recipeRepository.save(newRecipe);
                    logger.debug("✅ Nueva receta guardada: {} con ID: {}", aiRecipe.getTitle(), openAiId);
                } else {
                    existingRecipe = existingRecipeOpt.get();
                    logger.debug("✅ Receta ya existente: {} con ID: {}", aiRecipe.getTitle(), openAiId);
                }

                if (existingRecipe.getRecommendedToUsers() == null) {
                    existingRecipe.setRecommendedToUsers(new ArrayList<>());
                }

                boolean userAlreadyAssociated = existingRecipe.getRecommendedToUsers()
                        .stream()
                        .anyMatch(u -> u.getId().equals(userId));

                if (!userAlreadyAssociated) {
                    existingRecipe.getRecommendedToUsers().add(user);
                    recipeRepository.save(existingRecipe);
                    logger.debug("✅ Receta asociada al usuario: {} → Usuario {}", aiRecipe.getTitle(), userId);
                }
            }
            logger.info("💾 Historial de recetas guardado para usuario {}", userId);
        } catch (Exception e) {
            logger.error("❌ Error guardando recetas en historial: {}", e.getMessage(), e);
        }
    }

    private String generateOpenAiId(OpenAiRecipeResponse recipe) {
        try {
            String base = recipe.getTitle() + "|" +
                    String.join(",", recipe.getIngredients()) + "|" +
                    System.currentTimeMillis();

            byte[] encodedBytes = Base64.getEncoder().encode(base.getBytes());
            String encoded = new String(encodedBytes);

            return encoded.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(32, encoded.length()));
        } catch (Exception e) {
            String fallback = "recipe_" + recipe.getTitle().hashCode() + "_" + UUID.randomUUID().toString();
            return fallback.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(32, fallback.length()));
        }
    }
}