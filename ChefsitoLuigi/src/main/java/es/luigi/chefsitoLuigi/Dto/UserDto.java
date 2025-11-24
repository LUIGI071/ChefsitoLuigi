package es.luigi.chefsitoLuigi.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @Email(message = "{message.user.email.valid}")
    @NotBlank(message = "{message.user.email.required}")
    private String email;

    @NotBlank(message = "{message.user.password.required}")
    @Size(min = 8, message = "{message.user.password.size}")
    private String password;

    @NotBlank(message = "{message.user.fullName.required}")
    private String fullName;

    private Set<@NotBlank(message = "{message.user.roles.notblank}") String> roles;
}