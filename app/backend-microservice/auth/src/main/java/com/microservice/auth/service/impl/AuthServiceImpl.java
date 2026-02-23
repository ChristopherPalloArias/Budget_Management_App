package com.microservice.auth.service.impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.microservice.auth.dto.AuthMapper;
import com.microservice.auth.dto.AuthResponse;
import com.microservice.auth.dto.LoginRequest;
import com.microservice.auth.dto.RegisterRequest;
import com.microservice.auth.dto.UserResponse;
import com.microservice.auth.exception.AuthException;
import com.microservice.auth.exception.EmailAlreadyExistsException;
import com.microservice.auth.model.User;
import com.microservice.auth.repository.UserRepository;
import com.microservice.auth.security.JwtTokenProvider;
import com.microservice.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio de autenticación.
 *
 * <p>Maneja el registro de nuevos usuarios, autenticación con email/password
 * y consulta del usuario autenticado actual. Las contraseñas se almacenan
 * como hashes BCrypt y nunca en texto plano.</p>
 *
 * <h3>Flujo de Registro</h3>
 * <ol>
 *   <li>Validar que el email no esté registrado.</li>
 *   <li>Generar UUID como userId.</li>
 *   <li>Hashear la contraseña con BCrypt.</li>
 *   <li>Persistir el usuario.</li>
 *   <li>Generar token JWT y retornar AuthResponse.</li>
 * </ol>
 *
 * <h3>Flujo de Login</h3>
 * <ol>
 *   <li>Buscar usuario por email.</li>
 *   <li>Verificar la contraseña contra el hash almacenado.</li>
 *   <li>Generar token JWT y retornar AuthResponse.</li>
 * </ol>
 *
 * @see AuthService Contrato (interfaz) que esta clase implementa
 * @see JwtTokenProvider Generación y validación de tokens JWT
 */
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request datos de registro validados desde el controller
     * @return respuesta con datos del usuario creado y token JWT
     * @throws EmailAlreadyExistsException si el email ya está registrado
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "El correo electrónico '" + request.email() + "' ya está registrado");
        }

        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getUserId(), saved.getEmail());

        return AuthMapper.toAuthResponse(saved, token);
    }

    /**
     * Autentica un usuario existente con email y contraseña.
     *
     * @param request credenciales de login validadas desde el controller
     * @return respuesta con datos del usuario y token JWT
     * @throws AuthException si el email no existe o la contraseña es incorrecta
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException("Credenciales incorrectas"));

        if (!user.getEnabled()) {
            throw new AuthException("Esta cuenta ha sido deshabilitada");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Credenciales incorrectas");
        }

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail());

        return AuthMapper.toAuthResponse(user, token);
    }

    /**
     * Obtiene los datos del usuario autenticado actual.
     *
     * @param userId el ID del usuario extraído del token JWT
     * @return respuesta con los datos del usuario (sin token)
     * @throws AuthException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        return AuthMapper.toUserResponse(user);
    }
}
