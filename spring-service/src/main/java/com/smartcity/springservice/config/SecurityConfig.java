package com.smartcity.springservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final String clerkIssuerUrl;
	private final String clerkJwksUrl;
	private final String clerkAudience;

	public SecurityConfig(
		@Value("${app.auth.clerk.issuer-url:}") String clerkIssuerUrl,
		@Value("${app.auth.clerk.jwks-url:}") String clerkJwksUrl,
		@Value("${app.auth.clerk.audience:}") String clerkAudience
	) {
		this.clerkIssuerUrl = clerkIssuerUrl;
		this.clerkJwksUrl = clerkJwksUrl;
		this.clerkAudience = clerkAudience;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		boolean clerkConfigured = isClerkConfigured();

		HttpSecurity configuredHttp = http
			.cors(Customizer.withDefaults())
			.csrf((csrf) -> csrf.disable())
			.authorizeHttpRequests((auth) -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/api/health/**").permitAll()
				.requestMatchers("/api/auth/**").access((authentication, context) ->
					new org.springframework.security.authorization.AuthorizationDecision(clerkConfigured
						&& isAuthenticated(authentication.get()))
				)
				.anyRequest().permitAll()
			);

		if (clerkConfigured) {
			configuredHttp.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
		}

		return http.build();
	}

	@Bean
	@ConditionalOnExpression(
		"'${app.auth.clerk.issuer-url:}'.length() > 0 and '${app.auth.clerk.jwks-url:}'.length() > 0"
	)
	public JwtDecoder jwtDecoder() {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(clerkJwksUrl).build();

		OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(
			clerkIssuerUrl
		);

		if (clerkAudience == null || clerkAudience.isBlank()) {
			decoder.setJwtValidator(issuerValidator);
			return decoder;
		}

		OAuth2TokenValidator<Jwt> audienceValidator = (jwt) -> {
			if (jwt.getAudience().contains(clerkAudience)) {
				return OAuth2TokenValidatorResult.success();
			}

			return OAuth2TokenValidatorResult.failure(
				new OAuth2Error("invalid_token", "Token audience is invalid", null)
			);
		};

		decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
			issuerValidator,
			audienceValidator
		));

		return decoder;
	}

	private boolean isClerkConfigured() {
		return clerkIssuerUrl != null
			&& !clerkIssuerUrl.isBlank()
			&& clerkJwksUrl != null
			&& !clerkJwksUrl.isBlank();
	}

	private boolean isAuthenticated(Authentication authentication) {
		return authentication != null
			&& authentication.isAuthenticated()
			&& !(authentication instanceof AnonymousAuthenticationToken);
	}
}
