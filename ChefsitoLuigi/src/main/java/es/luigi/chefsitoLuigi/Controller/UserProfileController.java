package es.luigi.chefsitoLuigi.Controller;

import es.luigi.chefsitoLuigi.Dto.UserProfileDto;
import es.luigi.chefsitoLuigi.Service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    public ResponseEntity<UserProfileDto> createOrUpdate(@RequestBody UserProfileDto dto) {
        UserProfileDto saved = userProfileService.createOrUpdate(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileDto> getByUserId(@PathVariable Long userId) {
        return userProfileService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}