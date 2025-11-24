package es.luigi.chefsitoLuigi.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    // NUEVO: Nombre en español
    private String nameEs;

    private String unit; // e.g. g, ml, unidad

    private Double quantity;

    private String imageUrl;

    private LocalDate expiryDate;
}