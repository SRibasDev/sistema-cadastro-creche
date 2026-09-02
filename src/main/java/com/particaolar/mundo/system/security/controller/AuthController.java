package com.particaolar.mundo.system.security.controller;

import com.particaolar.mundo.system.security.dto.LoginRequestDTO;
import com.particaolar.mundo.system.security.dto.LoginResponseDTO;
import com.particaolar.mundo.system.security.dto.RegisterRequestDTO;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.security.service.JwtService;
import com.particaolar.mundo.system.security.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
            );
        } catch (AuthenticationException e) {
            // Generic message to prevent user enumeration
            throw new BadCredentialsException("Credenciais inválidas");
        }

        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(dto.email());
        String token = jwtService.gerarToken(usuario);
        log.info("Login bem-sucedido para usuário: {}", usuario.getEmail());
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome(), usuario.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setRole(dto.role());
        usuarioRepository.save(usuario);
        log.info("Novo usuário registrado: {}", usuario.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}