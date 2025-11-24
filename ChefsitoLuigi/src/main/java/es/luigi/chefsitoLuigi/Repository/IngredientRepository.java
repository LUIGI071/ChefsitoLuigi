package es.luigi.chefsitoLuigi.Repository;

import es.luigi.chefsitoLuigi.Entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("SELECT i FROM Ingredient i WHERE LOWER(i.name) = LOWER(:name)")
    Optional<Ingredient> findByNameIgnoreCase(@Param("name") String name);

    // Método adicional para búsqueda flexible
    @Query("SELECT i FROM Ingredient i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(i.nameEs) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Ingredient> findByNameContainingIgnoreCase(@Param("name") String name);
}