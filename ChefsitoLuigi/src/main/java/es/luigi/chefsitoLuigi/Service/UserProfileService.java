package es.luigi.chefsitoLuigi.Service;

import es.luigi.chefsitoLuigi.Dto.UserProfileDto;

import java.util.Optional;

public interface UserProfileService {
    UserProfileDto createOrUpdate(UserProfileDto dto);
    Optional<UserProfileDto> findByUserId(Long userId);
}