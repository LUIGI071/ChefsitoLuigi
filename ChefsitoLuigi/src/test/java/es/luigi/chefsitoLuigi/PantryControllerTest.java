package es.luigi.chefsitoLuigi;

import es.luigi.chefsitoLuigi.Controller.PantryController;
import es.luigi.chefsitoLuigi.Dto.PantryItemCreateRequest;
import es.luigi.chefsitoLuigi.Dto.PantryItemResponse;
import es.luigi.chefsitoLuigi.Security.CustomUserDetailsService;
import es.luigi.chefsitoLuigi.Service.PantryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryControllerTest {

    @Mock
    private PantryService pantryService;

    @InjectMocks
    private PantryController pantryController;
    
    private Authentication mockAuthWithUserId(Long userId) {
        CustomUserDetailsService.CustomUserDetails userDetails =
                mock(CustomUserDetailsService.CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(userId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        return authentication;
    }

    @Test
    void addItem_ShouldCallServiceWithUserIdAndReturnCreated() {
        // Given
        Long userId = 1L;
        Authentication authentication = mockAuthWithUserId(userId);

        PantryItemCreateRequest request = new PantryItemCreateRequest();
        request.setIngredientId(10L);
        request.setQuantity(2.0);

        PantryItemResponse created = new PantryItemResponse();
        created.setId(100L);
        created.setIngredientId(10L);
        created.setQuantity(2.0);

        when(pantryService.addItem(request, userId)).thenReturn(created);

        // When
        ResponseEntity<PantryItemResponse> response =
                pantryController.addItem(request, authentication);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getId());
        assertEquals(10L, response.getBody().getIngredientId());
        assertEquals(2.0, response.getBody().getQuantity());
        verify(pantryService, times(1)).addItem(request, userId);
    }

    @Test
    void listByUser_ShouldReturnItemsOfCurrentUser() {
        // Given
        Long userId = 1L;
        Authentication authentication = mockAuthWithUserId(userId);

        PantryItemResponse item = new PantryItemResponse();
        item.setId(100L);
        item.setIngredientId(10L);
        item.setQuantity(1.0);

        when(pantryService.listByUser(userId)).thenReturn(List.of(item));

        // When
        ResponseEntity<List<PantryItemResponse>> response =
                pantryController.listByUser(authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).getId());
        verify(pantryService, times(1)).listByUser(userId);
    }

    @Test
    void listByUserAdmin_ShouldReturnItemsForGivenUserId() {
        // Given
        Long userId = 5L;

        PantryItemResponse item = new PantryItemResponse();
        item.setId(200L);
        item.setIngredientId(99L);
        item.setQuantity(3.0);

        when(pantryService.listByUser(userId)).thenReturn(List.of(item));

        // When
        ResponseEntity<List<PantryItemResponse>> response =
                pantryController.listByUserAdmin(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(200L, response.getBody().get(0).getId());
        verify(pantryService, times(1)).listByUser(userId);
    }

    @Test
    void updateQuantity_ShouldCallServiceAndReturnUpdatedItem() {
        // Given
        Long userId = 1L;
        Authentication authentication = mockAuthWithUserId(userId);

        Long itemId = 100L;
        Double newQuantity = 5.0;

        PantryItemResponse updated = new PantryItemResponse();
        updated.setId(itemId);
        updated.setIngredientId(10L);
        updated.setQuantity(newQuantity);

        when(pantryService.updateQuantity(itemId, newQuantity, userId))
                .thenReturn(updated);

        // When
        ResponseEntity<PantryItemResponse> response =
                pantryController.updateQuantity(itemId, newQuantity, authentication);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(itemId, response.getBody().getId());
        assertEquals(newQuantity, response.getBody().getQuantity());
        verify(pantryService, times(1))
                .updateQuantity(itemId, newQuantity, userId);
    }

    @Test
    void delete_ShouldCallServiceAndReturnNoContent() {
        // Given
        Long userId = 1L;
        Authentication authentication = mockAuthWithUserId(userId);
        Long itemId = 100L;

        // When
        ResponseEntity<Void> response =
                pantryController.delete(itemId, authentication);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(pantryService, times(1)).delete(itemId, userId);
    }
}
