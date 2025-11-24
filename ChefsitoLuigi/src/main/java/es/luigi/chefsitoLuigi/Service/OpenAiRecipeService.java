package es.luigi.chefsitoLuigi.Service;

import es.luigi.chefsitoLuigi.Dto.OpenAI.OpenAiRecipeRequest;
import es.luigi.chefsitoLuigi.Dto.OpenAI.OpenAiRecipeResponse;

import java.util.List;

public interface OpenAiRecipeService {
    List<OpenAiRecipeResponse> getRecipeRecommendations(OpenAiRecipeRequest request);
    List<OpenAiRecipeResponse> getRecipeRecommendationsForUser(Long userId);
}