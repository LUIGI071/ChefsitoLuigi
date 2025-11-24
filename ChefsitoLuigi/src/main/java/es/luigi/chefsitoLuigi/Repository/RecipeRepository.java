package es.luigi.chefsitoLuigi.Repository;

import es.luigi.chefsitoLuigi.Entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByOpenAiId(String openAiId);
    List<Recipe> findByRecommendedToUsersId(Long userId); // Historial por usuario
    List<Recipe> findByCategory(String category);
    List<Recipe> findByDifficulty(String difficulty);
    List<Recipe> findByPreparationTimeLessThanEqual(Integer maxTime);
}