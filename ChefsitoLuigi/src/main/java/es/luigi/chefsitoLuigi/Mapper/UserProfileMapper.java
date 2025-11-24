package es.luigi.chefsitoLuigi.Mapper;

import es.luigi.chefsitoLuigi.Dto.UserProfileDto;
import es.luigi.chefsitoLuigi.Entity.User;
import es.luigi.chefsitoLuigi.Entity.UserProfile;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {

    @Autowired
    private UserRepository userRepository;

    @Mapping(source = "user.id", target = "userId")
    public abstract UserProfileDto toDto(UserProfile entity);

    @Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
    public abstract UserProfile toEntity(UserProfileDto dto);

    @Named("userIdToUser")
    protected User userIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }
}