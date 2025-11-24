package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Dto.PantryItemCreateRequest;
import es.luigi.chefsitoLuigi.Dto.PantryItemResponse;
import es.luigi.chefsitoLuigi.Entity.*;
import es.luigi.chefsitoLuigi.Exception.ResourceNotFoundException;
import es.luigi.chefsitoLuigi.Repository.*;
import es.luigi.chefsitoLuigi.Service.PantryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PantryServiceImpl implements PantryService {

    private static final Logger logger = LoggerFactory.getLogger(PantryServiceImpl.class);

    private final PantryItemRepository pantryItemRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    public PantryItemResponse addItem(PantryItemCreateRequest createRequest, Long userId) {
        logger.debug("Agregando item a despensa para usuario: {}", userId);

        // validar existencia user e ingredient
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Ingredient ingredient = ingredientRepository.findById(createRequest.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", createRequest.getIngredientId()));

        PantryItem item = PantryItem.builder()
                .user(user)
                .ingredient(ingredient)
                .quantity(createRequest.getQuantity())
                .build();

        PantryItem saved = pantryItemRepository.save(item);
        logger.info("✅ Item agregado a despensa: usuario {}, ingrediente {}, cantidad {}",
                user.getId(), ingredient.getName(), createRequest.getQuantity());

        return mapToResponse(saved);
    }

    @Override
    public List<PantryItemResponse> listByUser(Long userId) {
        logger.debug("Listando despensa para usuario: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<PantryItemResponse> items = pantryItemRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        logger.info("✅ Despensa listada: {} items para usuario {}", items.size(), userId);
        return items;
    }

    @Override
    public PantryItemResponse updateQuantity(Long id, Double quantity, Long userId) {
        logger.debug("Actualizando cantidad del item {} para usuario {}", id, userId);

        PantryItem item = pantryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PantryItem", "id", id));

        // Verificar que el item pertenece al usuario
        if (!item.getUser().getId().equals(userId)) {
            logger.warn("❌ Intento de modificar item no propio: usuario {} intentó modificar item {}", userId, id);
            throw new SecurityException("No tienes permisos para modificar este item");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity debe ser mayor que 0");
        }

        item.setQuantity(quantity);
        PantryItem saved = pantryItemRepository.save(item);

        logger.info("✅ Cantidad actualizada: item {}, nueva cantidad {}", id, quantity);

        return mapToResponse(saved);
    }

    @Override
    public void delete(Long id, Long userId) {
        logger.debug("Eliminando item {} para usuario {}", id, userId);

        PantryItem item = pantryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PantryItem", "id", id));

        // Verificar que el item pertenece al usuario
        if (!item.getUser().getId().equals(userId)) {
            logger.warn("❌ Intento de eliminar item no propio: usuario {} intentó eliminar item {}", userId, id);
            throw new SecurityException("No tienes permisos para eliminar este item");
        }

        pantryItemRepository.delete(item);
        logger.info("✅ Item eliminado: {}", id);
    }

    /**
     * Mapea PantryItem a PantryItemResponse
     */
    private PantryItemResponse mapToResponse(PantryItem pantryItem) {
        Ingredient ingredient = pantryItem.getIngredient();

        return PantryItemResponse.builder()
                .id(pantryItem.getId())
                .userId(pantryItem.getUser().getId())
                .ingredientId(ingredient.getId())
                .quantity(pantryItem.getQuantity())
                .ingredientName(ingredient.getName())
                .ingredientNameEs(ingredient.getNameEs())
                .ingredientImageUrl(ingredient.getImageUrl())
                .build();
    }
}