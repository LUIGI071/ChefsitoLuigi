package es.luigi.chefsitoLuigi.Security;

import es.luigi.chefsitoLuigi.Entity.User;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        System.out.println("🔐 Cargando usuario: " + email + " con roles: " + user.getRoles());

        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    System.out.println("🏷️ Procesando rol: " + role);
                    return new SimpleGrantedAuthority(role);
                })
                .collect(Collectors.toList());

        System.out.println("✅ Authorities finales: " + authorities);

        // UserDetails personalizado que incluye el ID del usuario
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    // Clase interna para UserDetails personalizado
    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User {
        private final Long id;

        public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
            super(username, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}