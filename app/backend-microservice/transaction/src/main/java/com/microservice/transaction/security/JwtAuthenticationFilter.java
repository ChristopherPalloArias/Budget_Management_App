package com.microservice.transaction.security;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro JWT para autenticación en el microservicio de transacciones.
 * 
 * Intercepta cada petición, extrae el token del header Authorization,
 * lo valida, y establece el contexto de seguridad con el userId.
 * 
 * El userId se utiliza posteriormente en los controladores y servicios
 * para filtrar y validar que el usuario solo acceda a sus propias
 * transacciones.
 * 
 * @see JwtTokenProvider
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info(">>> [TRANSACTION-FILTER] {} {} — processing request", method, requestURI);

        String token = extractTokenFromRequest(request);

        if (token == null) {
            log.warn(">>> [TRANSACTION-FILTER] No JWT token found in Authorization header for {} {}", method, requestURI);
        } else {
            log.info(">>> [TRANSACTION-FILTER] JWT token found (length={}), validating...", token.length());
            if (jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                log.info(">>> [TRANSACTION-FILTER] Token VALID — userId={}", userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = securityContextHolderStrategy.createEmptyContext();
                context.setAuthentication(authentication);
                securityContextHolderStrategy.setContext(context);
            } else {
                log.error(">>> [TRANSACTION-FILTER] Token INVALID for {} {} — will return 403", method, requestURI);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        if (StringUtils.hasText(bearerToken)) {
            log.warn(">>> [TRANSACTION-FILTER] Authorization header present but does NOT start with 'Bearer ': '{}'",
                    bearerToken.substring(0, Math.min(bearerToken.length(), 20)) + "...");
        }
        return null;
    }
}
