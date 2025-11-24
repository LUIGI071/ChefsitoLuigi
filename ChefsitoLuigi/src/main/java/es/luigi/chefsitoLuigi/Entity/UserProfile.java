package es.luigi.chefsitoLuigi.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_allergies", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "allergy")
    private List<String> allergies;

    @ElementCollection
    @CollectionTable(name = "user_intolerances", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "intolerance")
    private List<String> intolerances;

    @ElementCollection
    @CollectionTable(name = "user_disliked_ingredients", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Column(name = "disliked_ingredient")
    private List<String> dislikedIngredients;

    private String dietType; // VEGETARIAN, VEGAN, GLUTEN_FREE, etc.
    private String cookingSkillLevel; // BEGINNER, INTERMEDIATE, ADVANCED
}