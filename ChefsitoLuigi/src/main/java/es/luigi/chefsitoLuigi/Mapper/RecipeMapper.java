package es.luigi.chefsitoLuigi.Mapper;

import es.luigi.chefsitoLuigi.Dto.RecipeDto;
import es.luigi.chefsitoLuigi.Entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @Mapping(source = "preparationTime", target = "preparationMinutes")
    RecipeDto toDto(Recipe recipe);

    @Mapping(source = "preparationMinutes", target = "preparationTime")
    Recipe toEntity(RecipeDto dto);
}