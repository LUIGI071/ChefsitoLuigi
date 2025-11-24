package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Dto.UserDto;
import es.luigi.chefsitoLuigi.Entity.User;
import es.luigi.chefsitoLuigi.Exception.ResourceNotFoundException;
import es.luigi.chefsitoLuigi.Mapper.UserMapper;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import es.luigi.chefsitoLuigi.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto updateUserRoles(Long userId, List<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setRoles(Set.copyOf(roles));
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }
}