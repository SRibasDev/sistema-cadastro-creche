package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Pagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByCobrancaId(Long cobrancaId);

    List<Pagamento> findByDataPagamentoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM Pagamento p WHERE p.dataPagamento >= :inicio AND p.dataPagamento <= :fim")
    BigDecimal somarTotalRecebidoNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
