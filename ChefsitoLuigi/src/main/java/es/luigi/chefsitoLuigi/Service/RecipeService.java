package es.luigi.chefsitoLuigi.Service;

import es.luigi.chefsitoLuigi.Entity.Recipe;
import java.util.List;
import java.util.Optional;

public interface RecipeService {
    List<Recipe> getUserRecipeHistory(Long userId);
    List<Recipe> findByCategory(String category);
    List<Recipe> findByDifficulty(String difficulty);
    List<Recipe> findByMaxTime(Integer maxTime);
    List<Recipe> searchByTitle(String title);
    Optional<Recipe> findById(Long id);
    List<Recipe> findAll(); // Método agregado
}