package app.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(ex -> ex
                // Sin token, o token inválido/vencido -> 401
                .authenticationEntryPoint((request, response, e) ->
                    escribirError(response, HttpStatus.UNAUTHORIZED,
                        "No autenticado: token ausente, inválido o vencido"))
                // Autenticado pero sin permisos -> 403
                .accessDeniedHandler((request, response, e) ->
                    escribirError(response, HttpStatus.FORBIDDEN, "Acceso denegado"))
            )
            .authorizeHttpRequests(auth -> auth
                // Públicos: solo los métodos de lectura / login / registro
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                // /propiedades/mias va antes que /{id}: si no, ese patrón la volvería pública (issue #55)
                .requestMatchers(HttpMethod.GET, "/propiedades/mias").authenticated()
                .requestMatchers(HttpMethod.GET, "/propiedades", "/propiedades/{id}").permitAll()
                // Link para compartir con vista previa: lo pide el bot de WhatsApp, sin token (issue #151)
                .requestMatchers(HttpMethod.GET, "/compartir/propiedades/{id}").permitAll()
                // /error: el contenedor redirige ahí cuando un controller devuelve 403/404/409...
                // Si estuviera protegido, esos errores se verían como 401.
                .requestMatchers("/error").permitAll()
                // Solo ADMIN: verificar inmobiliarias (issue #61)
                .requestMatchers(HttpMethod.PATCH, "/inmobiliarias/{id}/verificacion").hasRole("ADMIN")
                // Solo ADMIN: importación desde fuentes externas
                .requestMatchers("/fuentes-externas/**").hasRole("ADMIN")
                // Todo lo demás (incluido POST /propiedades) requiere JWT válido
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void escribirError(HttpServletResponse response, HttpStatus status, String mensaje)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
            "{\"status\":" + status.value() + ",\"mensaje\":\"" + mensaje + "\",\"data\":null}");
    }
}
