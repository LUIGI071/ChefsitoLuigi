package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.IngredientDto;
import es.luigi.chefsitoLuigi.Service.IngredientService;
import es.luigi.chefsitoLuigi.Service.ImageService;
import es.luigi.chefsitoLuigi.Service.Impl.SmartIngredientSearchService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final ImageService imageService;
    private final SmartIngredientSearchService searchService;

    // CRUD Operations
    @Operation(summary = "Obtener todos los ingredientes (sin paginación)")
    @GetMapping("/all")
    public ResponseEntity<List<IngredientDto>> findAll() {
        System.out.println("📋 Solicitando todos los ingredientes");

        List<IngredientDto> allIngredients = ingredientService.findAll();

        System.out.println("✅ Devolviendo " + allIngredients.size() + " ingredientes");
        return ResponseEntity.ok(allIngredients);
    }

    @Operation(summary = "Crear ingrediente")
    @PostMapping
    public ResponseEntity<IngredientDto> create(@Valid @RequestBody IngredientDto dto) {
        IngredientDto created = ingredientService.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Subir imagen y devolver URL")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = imageService.store(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Operation(summary = "Obtener ingrediente por id")
    @GetMapping("/{id}")
    public ResponseEntity<IngredientDto> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.findById(id));
    }

    // Búsquedas Mejoradas
    @Operation(summary = "Buscar ingredientes por nombre")
    @GetMapping("/search")
    public ResponseEntity<List<IngredientDto>> searchIngredients(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {

        System.out.println("🔍 Búsqueda de ingredientes: " + query);

        List<IngredientDto> results = ingredientService.findAll().stream()
                .filter(ingredient ->
                        ingredient.getName().toLowerCase().contains(query.toLowerCase()))
                .limit(limit)
                .collect(Collectors.toList());

        System.out.println("✅ Encontrados " + results.size() + " ingredientes para: " + query);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Buscar ingredientes de forma inteligente (inglés/español)")
    @GetMapping("/smart-search")
    public ResponseEntity<List<IngredientDto>> smartSearchIngredients(
            @RequestParam String query,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        System.out.println("🔍 Búsqueda inteligente: '" + query + "' en idioma: " + language);

        List<IngredientDto> results = searchService.searchIngredients(query, language);

        System.out.println("✅ Encontrados " + results.size() + " ingredientes para: " + query);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Buscar ingredientes por texto de voz")
    @PostMapping("/voice-search")
    public ResponseEntity<?> voiceSearch(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String language) {

        String text = request.get("text");
        System.out.println("🎤 Búsqueda por voz simulada: '" + text + "' en idioma: " + language);

        List<IngredientDto> ingredients = searchService.searchIngredients(text, language);

        return ResponseEntity.ok(Map.of(
                "searchText", text,
                "ingredients", ingredients,
                "count", ingredients.size()
        ));
    }

    @Operation(summary = "Obtener ingredientes populares")
    @GetMapping("/popular")
    public ResponseEntity<List<IngredientDto>> getPopularIngredients(
            @RequestParam(defaultValue = "20") int limit) {

        System.out.println("📊 Solicitando " + limit + " ingredientes populares");

        List<IngredientDto> popular = ingredientService.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());

        System.out.println("✅ Devolviendo " + popular.size() + " ingredientes populares");

        return ResponseEntity.ok(popular);
    }

    @Operation(summary = "Listar ingredientes con paginación")
    @GetMapping
    public ResponseEntity<List<IngredientDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        System.out.println("📋 Solicitando ingredientes - página: " + page + ", tamaño: " + size);

        List<IngredientDto> allIngredients = ingredientService.findAll();
        int start = Math.min(page * size, allIngredients.size());
        int end = Math.min((page + 1) * size, allIngredients.size());

        List<IngredientDto> pagedIngredients = allIngredients.subList(start, end);

        System.out.println("✅ Devolviendo " + pagedIngredients.size() + " ingredientes");
        return ResponseEntity.ok(pagedIngredients);
    }

    @Operation(summary = "Actualizar ingrediente")
    @PutMapping("/{id}")
    public ResponseEntity<IngredientDto> update(@PathVariable Long id, @Valid @RequestBody IngredientDto dto) {
        return ResponseEntity.ok(ingredientService.update(id, dto));
    }

    @Operation(summary = "Eliminar ingrediente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}