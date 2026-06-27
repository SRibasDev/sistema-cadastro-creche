package com.particaolar.mundo.system.security.controller;

import com.particaolar.mundo.system.security.dto.LoginRequestDTO;
import com.particaolar.mundo.system.security.dto.LoginResponseDTO;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.service.JwtService;
import com.particaolar.mundo.system.security.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );
        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(dto.email());
        String token = jwtService.gerarToken(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome(), usuario.getRole().name()));
    }

    @GetMapping("/hash")
    public String gerarHash(@RequestParam String senha) {
        return new BCryptPasswordEncoder().encode(senha);
    }
}