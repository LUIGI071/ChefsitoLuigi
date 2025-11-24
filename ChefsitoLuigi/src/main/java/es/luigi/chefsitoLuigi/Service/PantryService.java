package es.luigi.chefsitoLuigi.Service;

import es.luigi.chefsitoLuigi.Dto.PantryItemCreateRequest;
import es.luigi.chefsitoLuigi.Dto.PantryItemResponse;
import java.util.List;

public interface PantryService {
    PantryItemResponse addItem(PantryItemCreateRequest createRequest, Long userId);
    List<PantryItemResponse> listByUser(Long userId);
    PantryItemResponse updateQuantity(Long id, Double quantity, Long userId);
    void delete(Long id, Long userId);
}