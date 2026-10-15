package com.particaolar.mundo.system.security;

import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.exception.UsuarioNotFoundException;
import com.particaolar.mundo.system.security.dto.TrocarSenhaRequestDTO;
import com.particaolar.mundo.system.security.dto.UsuarioResponseDTO;
import com.particaolar.mundo.system.security.dto.UsuarioUpdateRequestDTO;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.security.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario admin;
    private Usuario funcionario;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(1L);
        admin.setNome("Administrador");
        admin.setEmail("admin@creche.com");
        admin.setSenha("$2a$12$hashedPasswordAdmin");
        admin.setRole(Role.ADMIN);
        admin.setAtivo(true);

        funcionario = new Usuario();
        funcionario.setId(2L);
        funcionario.setNome("Funcionario 1");
        funcionario.setEmail("func@creche.com");
        funcionario.setSenha("$2a$12$hashedPasswordFunc");
        funcionario.setRole(Role.FUNCIONARIO);
        funcionario.setAtivo(true);
    }

    @Test
    @DisplayName("Deve listar todos os usuários com paginação")
    void deveListarTodos() {
        when(usuarioRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(admin, funcionario)));

        Page<UsuarioResponseDTO> resultado = usuarioService.listarTodos(PageRequest.of(0, 10));

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements());
        assertEquals("admin@creche.com", resultado.getContent().get(0).email());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void deveBuscarPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        UsuarioResponseDTO dto = usuarioService.buscarPorId(1L);

        assertEquals(1L, dto.id());
        assertEquals("admin@creche.com", dto.email());
        assertEquals(Role.ADMIN, dto.role());
    }

    @Test
    @DisplayName("Deve lançar UsuarioNotFoundException ao buscar ID inexistente")
    void deveLancarExceptionIdInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> usuarioService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve atualizar dados do funcionário com sucesso")
    void deveAtualizarUsuario() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(usuarioRepository.existsByEmailAndIdNot("func.novo@creche.com", 2L)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioUpdateRequestDTO dto = new UsuarioUpdateRequestDTO("Funcionario Atualizado", "func.novo@creche.com", Role.FUNCIONARIO);
        UsuarioResponseDTO res = usuarioService.atualizar(2L, dto);

        assertEquals("Funcionario Atualizado", res.nome());
        assertEquals("func.novo@creche.com", res.email());
    }

    @Test
    @DisplayName("Deve impedir atualização com e-mail duplicado")
    void deveImpedirEmailDuplicado() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(usuarioRepository.existsByEmailAndIdNot("admin@creche.com", 2L)).thenReturn(true);

        UsuarioUpdateRequestDTO dto = new UsuarioUpdateRequestDTO("Func", "admin@creche.com", Role.FUNCIONARIO);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizar(2L, dto));
    }

    @Test
    @DisplayName("Deve impedir rebaixamento do único ADMIN ativo")
    void deveImpedirRebaixarUnicoAdmin() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.existsByEmailAndIdNot("admin@creche.com", 1L)).thenReturn(false);
        when(usuarioRepository.countByRoleAndAtivoTrue(Role.ADMIN)).thenReturn(1L);

        UsuarioUpdateRequestDTO dto = new UsuarioUpdateRequestDTO("Admin", "admin@creche.com", Role.FUNCIONARIO);

        assertThrows(IllegalStateException.class, () -> usuarioService.atualizar(1L, dto));
    }

    @Test
    @DisplayName("Deve desativar funcionário com sucesso")
    void deveDesativarFuncionario() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponseDTO res = usuarioService.alterarStatus(2L, false, "admin@creche.com");

        assertFalse(res.ativo());
    }

    @Test
    @DisplayName("ADMIN não pode desativar sua própria conta")
    void adminNaoPodeDesativarPropriaConta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalStateException.class, () ->
                usuarioService.alterarStatus(1L, false, "admin@creche.com"));
    }

    @Test
    @DisplayName("ADMIN não pode desativar o único ADMIN ativo do sistema")
    void naoPodeDesativarUnicoAdmin() {
        Usuario outroAdmin = new Usuario();
        outroAdmin.setId(3L);
        outroAdmin.setEmail("outro.admin@creche.com");
        outroAdmin.setRole(Role.ADMIN);
        outroAdmin.setAtivo(true);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(outroAdmin));
        when(usuarioRepository.countByRoleAndAtivoTrue(Role.ADMIN)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () ->
                usuarioService.alterarStatus(3L, false, "admin@creche.com"));
    }

    @Test
    @DisplayName("Usuário pode alterar a própria senha validando a atual")
    void deveAlterarPropriaSenha() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(passwordEncoder.matches("Func@12345", funcionario.getSenha())).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("$2a$12$newHashed");

        TrocarSenhaRequestDTO dto = new TrocarSenhaRequestDTO("Func@12345", "NovaSenha@123");
        usuarioService.alterarSenha(2L, dto, "func@creche.com", false);

        verify(usuarioRepository).save(funcionario);
        assertEquals("$2a$12$newHashed", funcionario.getSenha());
    }

    @Test
    @DisplayName("Deve rejeitar alteração de senha se senha atual estiver errada")
    void deveRejeitarSenhaAtualIncorreta() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(passwordEncoder.matches("SenhaErrada", funcionario.getSenha())).thenReturn(false);

        TrocarSenhaRequestDTO dto = new TrocarSenhaRequestDTO("SenhaErrada", "NovaSenha@123");

        assertThrows(IllegalArgumentException.class, () ->
                usuarioService.alterarSenha(2L, dto, "func@creche.com", false));
    }

    @Test
    @DisplayName("ADMIN pode redefinir senha de outro usuário sem senha atual")
    void adminPodeRedefinirSenhaOutroUsuario() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(funcionario));
        when(passwordEncoder.encode("Resetada@123")).thenReturn("$2a$12$resetHashed");

        TrocarSenhaRequestDTO dto = new TrocarSenhaRequestDTO(null, "Resetada@123");
        usuarioService.alterarSenha(2L, dto, "admin@creche.com", true);

        verify(usuarioRepository).save(funcionario);
        assertEquals("$2a$12$resetHashed", funcionario.getSenha());
    }

    @Test
    @DisplayName("Usuario desativado tem isEnabled() = false")
    void usuarioDesativadoIsEnabledFalse() {
        funcionario.setAtivo(false);
        assertFalse(funcionario.isEnabled());
    }
}
