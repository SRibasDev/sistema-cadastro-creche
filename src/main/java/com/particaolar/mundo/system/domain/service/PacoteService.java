package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Desconto;
import com.particaolar.mundo.system.domain.entity.Pacote;
import com.particaolar.mundo.system.domain.entity.PacoteComprado;
import com.particaolar.mundo.system.domain.entity.PacoteConsumo;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.domain.repository.DescontoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteCompradoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteConsumoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.dto.request.PacoteCompraRequestDTO;
import com.particaolar.mundo.system.dto.request.PacoteRequestDTO;
import com.particaolar.mundo.system.dto.response.PacoteCompradoResponseDTO;
import com.particaolar.mundo.system.dto.response.PacoteResponseDTO;
import com.particaolar.mundo.system.enums.TipoDesconto;
import com.particaolar.mundo.system.exception.PacoteNotFoundException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.SaldoInsuficienteException;
import com.particaolar.mundo.system.mapper.PacoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Pacotes/combos promocionais com saldo e validade.
 * O saldo é debitado automaticamente quando a presença é marcada
 * (PresencaService -> debitarSaldo).
 * Concorrência: lock pessimista na linha do pacote comprado.
 * SaaS: compras futuras serão por (tenant_id, pet).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PacoteService {

    private final PacoteRepository pacoteRepository;
    private final PacoteCompradoRepository pacoteCompradoRepository;
    private final PacoteConsumoRepository consumoRepository;
    private final PetRepository petRepository;
    private final DescontoRepository descontoRepository;
    private final ServicoService servicoService;
    private final PacoteMapper pacoteMapper;
    private final Clock clock;

    @Value("${pacote.debito-automatico:true}")
    private boolean debitoAutomatico;

    @Transactional
    public PacoteResponseDTO salvar(PacoteRequestDTO dto) {
        if (pacoteRepository.existsByNome(dto.nome())) {
            throw new IllegalStateException("Já existe um pacote com o nome '" + dto.nome() + "'");
        }
        Servico servico = servicoService.buscarEntidade(dto.servicoId());
        Desconto desconto = dto.descontoId() != null
                ? descontoRepository.findById(dto.descontoId()).orElse(null)
                : null;

        Pacote pacote = pacoteMapper.toEntity(dto);
        pacote.setServico(servico);
        pacote.setDesconto(desconto);
        Pacote salvo = pacoteRepository.save(pacote);
        log.info("Pacote criado: {} ({}x {} = R$ {})",
                salvo.getNome(), salvo.getQuantidade(), servico.getUnidade(), salvo.getPrecoTotal());
        return pacoteMapper.toResponseDTO(salvo);
    }
    @Transactional(readOnly = true)
    public Page<PacoteResponseDTO> listarTodos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return pacoteRepository.findByAtivoTrue(pageable).map(pacoteMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public PacoteResponseDTO buscarPorId(Long id) {
        Pacote pacote = pacoteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new PacoteNotFoundException(id));
        return pacoteMapper.toResponseDTO(pacote);
    }

    @Transactional
    public PacoteResponseDTO atualizar(Long id, PacoteRequestDTO dto) {
        Pacote pacote = pacoteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new PacoteNotFoundException(id));
        pacoteMapper.updateEntityFromDTO(dto, pacote);
        if (!pacote.getServico().getId().equals(dto.servicoId())) {
            pacote.setServico(servicoService.buscarEntidade(dto.servicoId()));
        }
        log.info("Pacote {} atualizado", id);
        return pacoteMapper.toResponseDTO(pacote);
    }

    @Transactional
    public void deletar(Long id) {
        Pacote pacote = pacoteRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new PacoteNotFoundException(id));
        pacote.setAtivo(false);
        log.info("Pacote {} desativado", id);
    }

    /** Compra de pacote por um pet: gera saldo + validade. */
    @Transactional
    public PacoteCompradoResponseDTO comprar(Long pacoteId, PacoteCompraRequestDTO dto) {
        Pacote pacote = pacoteRepository.findByIdAndAtivoTrue(pacoteId)
                .orElseThrow(() -> new PacoteNotFoundException(pacoteId));
        Pet pet = petRepository.findByIdAndAtivoTrue(dto.petId())
                .orElseThrow(() -> new PetNotFoundException(dto.petId()));

        BigDecimal valorPago = dto.valorPago() != null ? dto.valorPago() : pacote.getPrecoTotal();
        LocalDate validade = LocalDate.now(clock).plusDays(pacote.getValidadeDias());

        PacoteComprado comprado = new PacoteComprado();
        comprado.setPacote(pacote);
        comprado.setPet(pet);
        comprado.setSaldo(pacote.getQuantidade());
        comprado.setValorPago(valorPago);
        comprado.setValidade(validade);

        PacoteComprado salvo = pacoteCompradoRepository.save(comprado);
        log.info("Pacote {} comprado para pet {}: saldo {} até {}",
                pacoteId, pet.getNome(), salvo.getSaldo(), validade);
        return pacoteMapper.toCompradoResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<PacoteCompradoResponseDTO> saldosPorPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return pacoteCompradoRepository.findByPetIdAndAtivoTrue(petId).stream()
                .map(pacoteMapper::toCompradoResponseDTO)
                .toList();
    }
    /**
     * Débito automático de saldo na presença. Escolhe o pacote do pet com
     * validade mais próxima e saldo suficiente. Retorna o id do pacote
     * consumido ou null quando não havia saldo disponível.
     */
    @Transactional
    public Long debitarSaldo(Long petId, Reserva reserva) {
        if (!debitoAutomatico) {
            return null;
        }
        LocalDate hoje = LocalDate.now(clock);
        List<PacoteComprado> disponiveis = pacoteCompradoRepository.buscarComSaldo(petId, hoje);
        if (disponiveis.isEmpty()) {
            log.info("Pet {} sem saldo de pacote disponível", petId);
            return null;
        }

        PacoteComprado escolhido = pacoteCompradoRepository
                .findWithLockById(disponiveis.get(0).getId())
                .orElseThrow(() -> new IllegalStateException("Pacote comprado não encontrado"));

        if (escolhido.getSaldo() < 1) {
            throw new SaldoInsuficienteException(escolhido.getId());
        }

        escolhido.setSaldo(escolhido.getSaldo() - 1);
        if (escolhido.getSaldo() == 0) {
            escolhido.setAtivo(false);
        }

        PacoteConsumo consumo = new PacoteConsumo();
        consumo.setPacoteComprado(escolhido);
        consumo.setReserva(reserva);
        consumo.setQuantidade(1);
        consumoRepository.save(consumo);

        log.info("Saldo debitado: pet={} pacote={} saldo restante={}",
                petId, escolhido.getId(), escolhido.getSaldo());
        return escolhido.getId();
    }

    /** Preço final do pacote aplicando o desconto associado (informativo). */
    public BigDecimal precoComDesconto(Pacote pacote) {
        if (pacote.getDesconto() == null) {
            return pacote.getPrecoTotal();
        }
        Desconto d = pacote.getDesconto();
        if (d.getTipo() == TipoDesconto.PERCENTUAL) {
            BigDecimal fator = BigDecimal.ONE.subtract(
                    d.getValor().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            return pacote.getPrecoTotal().multiply(fator).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal precoFinal = pacote.getPrecoTotal().subtract(d.getValor());
        return precoFinal.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
