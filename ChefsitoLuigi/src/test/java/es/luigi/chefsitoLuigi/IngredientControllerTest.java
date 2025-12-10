package es.luigi.chefsitoLuigi;

import es.luigi.chefsitoLuigi.Controller.IngredientController;
import es.luigi.chefsitoLuigi.Dto.IngredientDto;
import es.luigi.chefsitoLuigi.Service.ImageService;
import es.luigi.chefsitoLuigi.Service.IngredientService;
import es.luigi.chefsitoLuigi.Service.Impl.SmartIngredientSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientControllerTest {

    @Mock
    private IngredientService ingredientService;

    @Mock
    private ImageService imageService;

    @Mock
    private SmartIngredientSearchService searchService;

    @InjectMocks
    private IngredientController ingredientController;

    @Test
    void findAll_ShouldReturnOkWithList() {
        // Given
        IngredientDto tomato = new IngredientDto();
        tomato.setId(1L);
        tomato.setName("Tomato");

        IngredientDto onion = new IngredientDto();
        onion.setId(2L);
        onion.setName("Onion");

        when(ingredientService.findAll()).thenReturn(List.of(tomato, onion));

        // When
        ResponseEntity<List<IngredientDto>> response = ingredientController.findAll();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Tomato", response.getBody().get(0).getName());
        verify(ingredientService, times(1)).findAll();
    }

    @Test
    void create_ShouldReturnCreatedIngredient() {
        // Given
        IngredientDto request = new IngredientDto();
        request.setName("Tomato");

        IngredientDto created = new IngredientDto();
        created.setId(1L);
        created.setName("Tomato");

        when(ingredientService.create(request)).thenReturn(created);

        // When
        ResponseEntity<IngredientDto> response = ingredientController.create(request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Tomato", response.getBody().getName());
        verify(ingredientService, times(1)).create(request);
    }

    @Test
    void smartSearchIngredients_ShouldDelegateToServiceAndReturnOk() {
        // Given
        IngredientDto cabbage = new IngredientDto();
        cabbage.setId(40L);
        cabbage.setName("Cabbage");

        when(searchService.searchIngredients("pollo", "es"))
                .thenReturn(List.of(cabbage));

        // When
        ResponseEntity<List<IngredientDto>> response =
                ingredientController.smartSearchIngredients("pollo", "es");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Cabbage", response.getBody().get(0).getName());
        verify(searchService, times(1)).searchIngredients("pollo", "es");
    }

    @Test
    void voiceSearch_ShouldReturnMapWithIngredientsAndCount() {
        // Given
        String text = "tomate";

        IngredientDto tomato = new IngredientDto();
        tomato.setId(1L);
        tomato.setName("Tomato");

        when(searchService.searchIngredients(text, "es"))
                .thenReturn(List.of(tomato));

        Map<String, String> requestBody = Map.of("text", text);

        // When
        ResponseEntity<?> response =
                ingredientController.voiceSearch(requestBody, "es");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(text, body.get("searchText"));
        assertEquals(1, body.get("count"));

        @SuppressWarnings("unchecked")
        List<IngredientDto> ingredients = (List<IngredientDto>) body.get("ingredients");
        assertEquals(1, ingredients.size());
        assertEquals("Tomato", ingredients.get(0).getName());

        verify(searchService, times(1)).searchIngredients(text, "es");
    }

    @Test
    void uploadImage_ShouldReturnUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        when(imageService.store(any())).thenReturn("https://cdn.chefsito.app/images/image.jpg");

        // When
        ResponseEntity<?> response = ingredientController.uploadImage(file);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("https://cdn.chefsito.app/images/image.jpg", body.get("url"));

        verify(imageService, times(1)).store(any());
    }
}
