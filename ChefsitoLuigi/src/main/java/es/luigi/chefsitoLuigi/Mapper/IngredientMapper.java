package es.luigi.chefsitoLuigi.Mapper;

import es.luigi.chefsitoLuigi.Dto.IngredientDto;
import es.luigi.chefsitoLuigi.Entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mapping(source = "nameEs", target = "nameEs")
    IngredientDto toDto(Ingredient entity);

    Ingredient toEntity(IngredientDto dto);
}