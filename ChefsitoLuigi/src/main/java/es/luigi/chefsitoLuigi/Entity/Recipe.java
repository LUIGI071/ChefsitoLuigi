package es.luigi.chefsitoLuigi.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "recipes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(length = 4000)
    private String instructions;

    private Integer preparationTime; // Cambiado de preparationMinutes
    private String category;
    private String difficulty;
    private String source = "openai";
    private String openAiId;

    // AÑADIR ESTOS CAMPOS FALTANTES:
    @ElementCollection
    @CollectionTable(name = "recipe_diet_tags", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "diet_tag")
    private Set<String> dietTags;

    @ManyToMany
    @JoinTable(name = "recipe_ingredients_rel",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id"))
    private List<Ingredient> ingredients;

    @ElementCollection
    @CollectionTable(name = "recipe_ingredients_names", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient_name")
    private List<String> ingredientNames;

    @ManyToMany
    @JoinTable(name = "user_recipe_history",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> recommendedToUsers;
}