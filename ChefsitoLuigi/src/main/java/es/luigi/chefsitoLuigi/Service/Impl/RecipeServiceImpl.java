package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Entity.Recipe;
import es.luigi.chefsitoLuigi.Repository.RecipeRepository;
import es.luigi.chefsitoLuigi.Service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;

    @Override
    public List<Recipe> getUserRecipeHistory(Long userId) {
        return recipeRepository.findByRecommendedToUsersId(userId);
    }

    @Override
    public List<Recipe> findByCategory(String category) {
        return recipeRepository.findByCategory(category);
    }

    @Override
    public List<Recipe> findByDifficulty(String difficulty) {
        return recipeRepository.findByDifficulty(difficulty);
    }

    @Override
    public List<Recipe> findByMaxTime(Integer maxTime) {
        return recipeRepository.findByPreparationTimeLessThanEqual(maxTime);
    }

    @Override
    public List<Recipe> searchByTitle(String title) {
        return recipeRepository.findAll().stream()
                .filter(recipe -> recipe.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Recipe> findById(Long id) {
        return recipeRepository.findById(id);
    }

    @Override
    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }
}