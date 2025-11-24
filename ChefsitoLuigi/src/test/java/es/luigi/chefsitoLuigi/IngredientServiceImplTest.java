package es.luigi.chefsitoLuigi;

import es.luigi.chefsitoLuigi.Dto.IngredientDto;
import es.luigi.chefsitoLuigi.Entity.Ingredient;
import es.luigi.chefsitoLuigi.Exception.ResourceNotFoundException;
import es.luigi.chefsitoLuigi.Mapper.IngredientMapper;
import es.luigi.chefsitoLuigi.Repository.IngredientRepository;
import es.luigi.chefsitoLuigi.Service.Impl.IngredientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceImplTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientServiceImpl ingredientService;

    @Test
    void create_ShouldReturnIngredientDto() {
        // Given
        IngredientDto dto = new IngredientDto();
        dto.setName("Tomato");
        Ingredient entity = new Ingredient();
        entity.setName("Tomato");
        when(ingredientMapper.toEntity(dto)).thenReturn(entity);
        when(ingredientRepository.save(entity)).thenReturn(entity);
        when(ingredientMapper.toDto(entity)).thenReturn(dto);

        // When
        IngredientDto result = ingredientService.create(dto);

        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        verify(ingredientRepository, times(1)).save(entity);
    }

    @Test
    void findById_WhenIngredientExists_ShouldReturnIngredientDto() {
        // Given
        Long id = 1L;
        Ingredient entity = new Ingredient();
        entity.setId(id);
        entity.setName("Tomato");
        IngredientDto dto = new IngredientDto();
        dto.setId(id);
        dto.setName("Tomato");
        when(ingredientRepository.findById(id)).thenReturn(Optional.of(entity));
        when(ingredientMapper.toDto(entity)).thenReturn(dto);

        // When
        IngredientDto result = ingredientService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Tomato", result.getName());
    }

    @Test
    void findById_WhenIngredientNotExists_ShouldThrowException() {
        // Given
        Long id = 1L;
        when(ingredientRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> ingredientService.findById(id));
    }
}
