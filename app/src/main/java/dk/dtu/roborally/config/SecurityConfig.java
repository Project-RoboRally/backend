package dk.dtu.roborally.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			SecurityContextRepository securityContextRepository)
			throws Exception {
		http.cors(cors -> {
		}).securityContext(context -> context
				.securityContextRepository(securityContextRepository))
				.csrf(csrf -> csrf
						.csrfTokenRepository(
								CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(
								new CsrfTokenRequestAttributeHandler())
						// SockJS uses POST transport requests; REST mutations
						// remain CSRF-protected.
						.ignoringRequestMatchers("/ws/**"))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response,
								exception) -> response.setStatus(
										HttpStatus.UNAUTHORIZED.value())))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/health", "/api/csrf",
								"/api/login", "/api/register")
						.permitAll().requestMatchers("/api/session")
						.authenticated()
						.requestMatchers(HttpMethod.GET, "/api/**").permitAll()
						.requestMatchers("/ws/**").authenticated()
						.requestMatchers("/api/**").authenticated().anyRequest()
						.permitAll())
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(logout -> logout.logoutUrl("/api/logout")
						.logoutSuccessHandler((request, response,
								authentication) -> response.setStatus(
										HttpStatus.NO_CONTENT.value())));
		return http.build();
	}
}
