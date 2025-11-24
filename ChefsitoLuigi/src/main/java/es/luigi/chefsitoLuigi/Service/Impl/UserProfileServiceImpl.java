package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Dto.UserProfileDto;
import es.luigi.chefsitoLuigi.Entity.UserProfile;
import es.luigi.chefsitoLuigi.Exception.ResourceNotFoundException;
import es.luigi.chefsitoLuigi.Mapper.UserProfileMapper;
import es.luigi.chefsitoLuigi.Repository.UserProfileRepository;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import es.luigi.chefsitoLuigi.Service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileDto createOrUpdate(UserProfileDto dto) {
        // Verificar que el usuario existe
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResourceNotFoundException("User", "id", dto.getUserId());
        }

        // Buscar si ya existe un perfil para este usuario
        Optional<UserProfile> existingProfile = userProfileRepository.findByUserId(dto.getUserId());

        UserProfile userProfile;
        if (existingProfile.isPresent()) {
            // Actualizar perfil existente
            userProfile = existingProfile.get();
            updateProfileFromDto(userProfile, dto);
        } else {
            // Crear nuevo perfil
            userProfile = userProfileMapper.toEntity(dto);
        }

        UserProfile saved = userProfileRepository.save(userProfile);
        return userProfileMapper.toDto(saved);
    }

    @Override
    public Optional<UserProfileDto> findByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(userProfileMapper::toDto);
    }

    private void updateProfileFromDto(UserProfile profile, UserProfileDto dto) {
        profile.setAllergies(dto.getAllergies());
        profile.setIntolerances(dto.getIntolerances());
        profile.setDislikedIngredients(dto.getDislikedIngredients());
        profile.setDietType(dto.getDietType());
    }
}