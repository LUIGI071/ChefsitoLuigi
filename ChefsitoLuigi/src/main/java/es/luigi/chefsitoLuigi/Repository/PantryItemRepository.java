package es.luigi.chefsitoLuigi.Repository;

import es.luigi.chefsitoLuigi.Entity.PantryItem;
import es.luigi.chefsitoLuigi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {
    List<PantryItem> findByUser(User user);
    List<PantryItem> findByUserId(Long userId);

    // Nuevo método para verificar propiedad
    @Query("SELECT p FROM PantryItem p WHERE p.id = :id AND p.user.id = :userId")
    Optional<PantryItem> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}