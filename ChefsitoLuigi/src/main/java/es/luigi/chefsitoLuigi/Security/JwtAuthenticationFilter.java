package es.luigi.chefsitoLuigi.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = tokenProvider.resolveToken(request);
        System.out.println("🔍 JWT Filter - Token encontrado: " + (token != null));

        if (token != null) {
            System.out.println("🔍 Validando token...");
            boolean isValid = tokenProvider.validateToken(token);
            System.out.println("🔍 Token válido: " + isValid);

            if (isValid) {
                var username = tokenProvider.getUsername(token);
                System.out.println("🔍 Usuario del token: " + username);

                try {
                    var userDetails = userDetailsService.loadUserByUsername(username);
                    var auth = tokenProvider.getAuthentication(token, userDetails);

                    System.out.println("🔍 Authentication creada con authorities: " + auth.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("✅ Authentication establecida en SecurityContext");
                } catch (Exception e) {
                    System.out.println("❌ Error cargando userDetails: " + e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}