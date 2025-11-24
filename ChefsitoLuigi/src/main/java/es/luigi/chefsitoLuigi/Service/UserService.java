package es.luigi.chefsitoLuigi.Service;

import es.luigi.chefsitoLuigi.Dto.UserDto;
import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    void deleteUser(Long userId);
    UserDto updateUserRoles(Long userId, List<String> roles);
}