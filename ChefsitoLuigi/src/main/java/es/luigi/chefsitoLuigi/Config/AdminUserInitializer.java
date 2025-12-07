package es.luigi.chefsitoLuigi.Config;

import es.luigi.chefsitoLuigi.Entity.User;
import es.luigi.chefsitoLuigi.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 Inicializando usuario admin...");

        if (userRepository.findByEmail("admin@chefsito.app").isEmpty()) {
            User admin = User.builder()
                    .email("admin@chefsito.app")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Usuario admin creado: admin@chefsito.app / admin");
            System.out.println("✅ Roles asignados: ROLE_ADMIN, ROLE_USER");
        } else {
            System.out.println("ℹ️ Usuario admin ya existe");
        }

        // Verificar que se creó correctamente
        userRepository.findByEmail("admin@chefsito.app").ifPresent(admin -> {
            System.out.println("🔍 Usuario admin en BD - Roles: " + admin.getRoles());
        });
    }
}