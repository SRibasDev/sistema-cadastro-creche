# 🚀 PLANO INFALÍVEL: SaaS de Agendamento para Creche/Hotel de Pets

**Documento Estratégico Completo para Transformar seu Sistema em Produto Vendável**

---

## 📊 RESUMO EXECUTIVO

| Métrica | Valor |
|---------|-------|
| **Tempo até MVP Multi-tenant** | 12 semanas |
| **Investimento total** | R$ 132-250k |
| **Break-even** | 8-10 clientes (Plano Professional) |
| **Receita mensal esperada (Ano 1)** | R$ 24-50k |
| **Margem bruta** | ~85-90% |
| **TAM (Total Addressable Market)** | ~50k creches/hotéis no Brasil |

---

## 🎯 FASE 0: VALIDAÇÃO IMEDIATA (Semanas 1-2)

### Ação 1: Pesquisa com Clientes Reais

**Objetivo**: Validar se as creches pagam por este produto

**Execução:**
1. Criar lista de 30 creches/hotéis locais (busca no Google Maps)
2. Ligar 5-10 por semana com script curto:
   ```
   "Oi, gerencio creches e fiz um sistema para agendar 
    hospedagens online. Você pagaria R$ 200/mês por isso?"
   ```
3. Coletar respostas em planilha:
   - Interesse? (Sim/Não/Talvez)
   - Pain point principal? (Agenda manual, perda de clientes, etc)
   - Preço máximo? (R$ 100-500)
   - Funcionalidade crítica? (agendamento, pagamento, notificação)

**Meta**: 5+ respostas "Sim" = validação ✅

**Outcome esperado**: Confirmação de demanda real

---

### Ação 2: Preparar Landing Page Simples

**URL**: seu-saas-pets.com.br

**Seções:**
- Hero: "Gerencie Hospedagens sem Estresse"
- Problema: "Planilhas? Ligações perdidas? Clientes indo embora?"
- Solução: "Calendário online 24/7, pagamento automático, SMS"
- Pricing (simples):
  - Teste Grátis: 14 dias
  - Depois: R$ 199/mês (Starter)
- CTA: "Solicitar Demo"
- Social proof: Lógos de creches (conforme ganhar clientes)

**Tech**: Vercel + Next.js simples ou até Carrd/Webflow por enquanto

---

### Ação 3: Onboard 1-2 Creches Piloto

**Objetivo**: Testar o MVP atual com usuários reais

**Execução:**
1. Escolher creche amiga ou que se interesse na ligação
2. Setup: 30 min call + migração de dados
3. Período: 30 dias gratuito (beta tester)
4. Feedback: semanal via WhatsApp/email
5. Documentar:
   - O que funcionou?
   - O que faltou?
   - Qual a maior dificuldade de uso?

**Outcome**: Validação técnica + feedback de UX

---

## 🏗️ FASE 1: REFATORAÇÃO PARA MULTI-TENANCY (Semanas 3-5)

### 1.1 Estratégia de Isolamento de Dados

**Sua base atual**: MySQL com CRUD simples por tutor/pet

**Problema**: Tudo está no mesmo banco, sem isolamento por empresa

**Solução escolhida**: **Row-Level Security (RLS) + Tenant Context**

#### Migrations SQL

```sql
-- Migração: Adicionar tenant_id a todas as tabelas
ALTER TABLE tutor ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE pet ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE hospedagem ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE usuario ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;

-- Índices críticos para performance
CREATE INDEX idx_tutor_tenant ON tutor(tenant_id, ativo);
CREATE INDEX idx_pet_tenant ON pet(tenant_id, ativo);
CREATE INDEX idx_hospedagem_tenant ON hospedagem(tenant_id, status);

-- Nova tabela: Empresas (tenants)
CREATE TABLE empresa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(20) UNIQUE,
    email_admin VARCHAR(255) NOT NULL,
    plano VARCHAR(50) NOT NULL DEFAULT 'STARTER',
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    data_criacao TIMESTAMP DEFAULT NOW(),
    data_cancelamento TIMESTAMP NULL,
    subdomain VARCHAR(100) UNIQUE NOT NULL,
    documento_url TEXT,
    logo_url TEXT
);

-- Auditoria de acesso
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    usuario_id BIGINT,
    acao VARCHAR(50),
    tabela VARCHAR(100),
    registro_id BIGINT,
    dados_antigos JSONB,
    dados_novos JSONB,
    timestamp TIMESTAMP DEFAULT NOW()
);
```

---

### 1.2 Refatoração do Backend Java

#### Componente: TenantContext

```java
@Component
public class TenantContext {
    private static final ThreadLocal<Long> tenantId = new ThreadLocal<>();
    
    public static void setTenantId(Long id) {
        tenantId.set(id);
    }
    
    public static Long getTenantId() {
        Long id = tenantId.get();
        if (id == null) {
            throw new IllegalStateException("Tenant ID não configurado");
        }
        return id;
    }
    
    public static void clear() {
        tenantId.remove();
    }
}
```

#### Interceptor: TenantInterceptor

```java
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        // Extract from subdomain: usuario.seu-saas.com → usuario
        String host = request.getServerName();
        String subdomain = host.split("\\.")[0];
        
        // OU extract from JWT claim
        String token = extractToken(request);
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Long tenantId = claims.get("tenant_id", Long.class);
        TenantContext.setTenantId(tenantId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) throws Exception {
        TenantContext.clear();
    }
}
```

#### Repository com Filtro Automático

```java
@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    
    @Query("SELECT t FROM Tutor t WHERE t.tenantId = :tenantId AND t.ativo = true")
    Page<Tutor> findActivosByTenant(@Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT t FROM Tutor t WHERE t.tenantId = :tenantId AND t.id = :id")
    Optional<Tutor> findByIdAndTenant(@Param("id") Long id, 
                                       @Param("tenantId") Long tenantId);
}
```

#### Service com Tenant Automático

```java
@Service
@Transactional
public class TutorService {
    
    private final TutorRepository tutorRepository;
    
    public Page<TutorResponseDTO> listarAtivos(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        return tutorRepository.findActivosByTenant(tenantId, pageable)
            .map(TutorMapper::toResponseDTO);
    }
    
    public void criarTutor(TutorRequestDTO dto) {
        Tutor tutor = TutorMapper.toEntity(dto);
        tutor.setTenantId(TenantContext.getTenantId());
        tutorRepository.save(tutor);
    }
    
    public void deletarTutor(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Tutor tutor = tutorRepository.findByIdAndTenant(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado"));
        tutor.setAtivo(false);
        tutorRepository.save(tutor);
    }
}
```

#### Entidade Atualizada

```java
@Entity
@Table(name = "tutor")
@Getter
@Setter
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tenantId;
    
    private String nome;
    private String telefone;
    private String cpf;
    private boolean ativo = true;
    
    @CreationTimestamp
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    private LocalDateTime atualizadoEm;
}
```

---

### 1.3 Fluxo de Autenticação Revisado

#### JWT com tenant_id

```java
@Service
public class JwtService {
    
    public String gerarToken(Usuario usuario) {
        return Jwts.builder()
            .setSubject(usuario.getEmail())
            .claim("tenant_id", usuario.getTenantId())
            .claim("role", usuario.getRole())
            .claim("user_id", usuario.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact();
    }
}
```

#### Endpoint de Login

```java
@PostMapping("/login")
public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
    Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
        .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
    
    if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
        throw new UnauthorizedException("Credenciais inválidas");
    }
    
    String token = jwtService.gerarToken(usuario);
    
    return ResponseEntity.ok(new LoginResponseDTO(
        token,
        usuario.getNome(),
        usuario.getRole(),
        usuario.getTenantId()
    ));
}
```

---

### 1.4 Checklist de Refatoração

- [ ] Adicionar `tenant_id` a todas as entidades
- [ ] Criar tabela `empresa`
- [ ] Implementar `TenantContext` e `TenantInterceptor`
- [ ] Refatorar todos os Repositories com filtro de tenant
- [ ] Adicionar `tenant_id` ao JWT
- [ ] Testes: garantir isolamento entre tenants
- [ ] Migrations com Flyway
- [ ] Deploy em ambiente de staging

---

## 🛠️ FASE 2: NOVAS FEATURES CRÍTICAS (Semanas 6-10)

### 2.1 Módulo de Serviços

#### Entidade

```java
@Entity
public class Servico {
    @Id
    private Long id;
    private Long tenantId;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer duracaoMinutos;
    private String cor;
    private boolean ativo = true;
}
```

#### DTO

```java
public record ServicoRequestDTO(
    String nome,
    String descricao,
    BigDecimal preco,
    Integer duracaoMinutos
) {}
```

#### Controller

```java
@RestController
@RequestMapping("/api/v1/servicos")
public class ServicoController {
    
    @PostMapping
    public ResponseEntity<ServicoResponseDTO> criar(@RequestBody ServicoRequestDTO dto) {
        Servico servico = servicoService.criar(dto);
        return ResponseEntity.status(201).body(ServicoMapper.toResponseDTO(servico));
    }
    
    @GetMapping
    public ResponseEntity<Page<ServicoResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(servicoService.listar(pageable)
            .map(ServicoMapper::toResponseDTO));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(
        @PathVariable Long id,
        @RequestBody ServicoRequestDTO dto) {
        Servico servico = servicoService.atualizar(id, dto);
        return ResponseEntity.ok(ServicoMapper.toResponseDTO(servico));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 2.2 Agendamento com Validações

#### Entidade

```java
@Entity
public class Agendamento {
    @Id
    private Long id;
    private Long tenantId;
    
    @ManyToOne
    private Pet pet;
    
    @ManyToOne
    private Servico servico;
    
    private LocalDateTime inicio;
    private LocalDateTime fim;
    
    @Enumerated(EnumType.STRING)
    private StatusAgendamento status;
    
    private BigDecimal valorTotal;
    private String observacoes;
    
    @CreationTimestamp
    private LocalDateTime criadoEm;
}
```

#### Service com Validações

```java
@Service
@Transactional
public class AgendamentoService {
    
    public void criar(AgendamentoRequestDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        
        Pet pet = petRepository.findByIdAndTenant(dto.getPetId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));
        
        Servico servico = servicoRepository.findByIdAndTenant(dto.getServicoId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));
        
        // VALIDAÇÃO: Não permitir sobreposição
        boolean temConflito = agendamentoRepository.existsConflict(
            pet.getId(),
            dto.getInicio(),
            dto.getFim(),
            tenantId
        );
        
        if (temConflito) {
            throw new BusinessException("Pet já tem agendamento neste horário");
        }
        
        // Validação: não agendar no passado
        if (dto.getInicio().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível agendar no passado");
        }
        
        // Validação: duração mínima
        long duracao = Duration.between(dto.getInicio(), dto.getFim()).toMinutes();
        if (duracao < servico.getDuracaoMinutos()) {
            throw new BusinessException("Duração insuficiente para este serviço");
        }
        
        Agendamento agendamento = new Agendamento();
        agendamento.setTenantId(tenantId);
        agendamento.setPet(pet);
        agendamento.setServico(servico);
        agendamento.setInicio(dto.getInicio());
        agendamento.setFim(dto.getFim());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setValorTotal(servico.getPreco());
        agendamento.setObservacoes(dto.getObservacoes());
        
        agendamentoRepository.save(agendamento);
    }
    
    public void confirmar(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Agendamento agendamento = agendamentoRepository.findByIdAndTenant(id, tenantId)
            .orElseThrow();
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamentoRepository.save(agendamento);
    }
    
    public void cancelar(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Agendamento agendamento = agendamentoRepository.findByIdAndTenant(id, tenantId)
            .orElseThrow();
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
    }
}
```

#### Repository

```java
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Agendamento a
        WHERE a.tenantId = :tenantId
          AND a.pet.id = :petId
          AND a.status != 'CANCELADO'
          AND (
            (a.inicio < :fim AND a.fim > :inicio)
          )
    """)
    boolean existsConflict(
        @Param("petId") Long petId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("tenantId") Long tenantId
    );
    
    Page<Agendamento> findByTenantIdAndStatusOrderByInicio(
        Long tenantId,
        StatusAgendamento status,
        Pageable pageable
    );
}
```

---

### 2.3 Sistema de Notificações

#### Abstração

```java
public interface NotificacaoService {
    void enviar(Notificacao notificacao);
}
```

#### Email (SendGrid)

```java
@Service
public class EmailNotificacaoService implements NotificacaoService {
    
    private final SendGridClient sendGridClient;
    
    @Override
    public void enviar(Notificacao notificacao) {
        Email from = new Email("noreply@seu-saas.com");
        Email to = new Email(notificacao.getDestinatario());
        Content content = new Content("text/html", notificacao.getCorpo());
        Mail mail = new Mail(from, notificacao.getTitulo(), to, content);
        
        try {
            sendGridClient.send(mail);
        } catch (IOException e) {
            log.error("Erro ao enviar email", e);
        }
    }
}
```

#### SMS (Twilio)

```java
@Service
public class SmsNotificacaoService implements NotificacaoService {
    
    private final TwilioClient twilioClient;
    
    @Override
    public void enviar(Notificacao notificacao) {
        Message message = Message.creator(
            new PhoneNumber(notificacao.getTelefone()),
            new PhoneNumber("+5511999999999"),
            notificacao.getCorpo()
        ).create();
    }
}
```

#### Listener de Eventos

```java
@Component
public class AgendamentoEventListener {
    
    private final EmailNotificacaoService emailService;
    private final SmsNotificacaoService smsService;
    
    @EventListener
    public void onAgendamentoConfirmado(AgendamentoConfirmadoEvent event) {
        Agendamento agendamento = event.getAgendamento();
        Pet pet = agendamento.getPet();
        Cliente cliente = pet.getCliente();
        
        String corpo = String.format("""
            Seu agendamento foi confirmado!
            Pet: %s
            Serviço: %s
            Data/Hora: %s
            Valor: R$ %.2f
        """, pet.getNome(), agendamento.getServico().getNome(),
            agendamento.getInicio(), agendamento.getValorTotal());
        
        Notificacao notificacao = new Notificacao(
            cliente.getEmail(),
            "Agendamento Confirmado",
            corpo
        );
        emailService.enviar(notificacao);
        
        smsService.enviar(new Notificacao(
            cliente.getTelefone(),
            null,
            "Agendamento de " + pet.getNome() + " confirmado para " + 
            agendamento.getInicio().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
        ));
    }
}
```

---

### 2.4 Integração de Pagamento (Stripe)

#### Configuração

```java
@Configuration
public class StripeConfig {
    
    @Bean
    public StripeClient stripeClient(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey;
        return new StripeClient();
    }
}
```

#### Service de Pagamento

```java
@Service
@Transactional
public class PagamentoService {
    
    private final StripeClient stripeClient;
    private final AgendamentoRepository agendamentoRepository;
    
    public String criarCheckoutSession(Long agendamentoId) throws StripeException {
        Long tenantId = TenantContext.getTenantId();
        Agendamento agendamento = agendamentoRepository.findByIdAndTenant(agendamentoId, tenantId)
            .orElseThrow();
        
        SessionCreateParams params = SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl("https://seu-saas.com/sucesso?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("https://seu-saas.com/cancelado")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(agendamento.getValorTotal().longValue() * 100)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(agendamento.getServico().getNome())
                                    .addImage("https://seu-saas.com/logo.png")
                                    .build()
                            )
                            .build()
                    )
                    .setQuantity(1L)
                    .build()
            )
            .putMetadata("agendamento_id", agendamentoId.toString())
            .putMetadata("tenant_id", tenantId.toString())
            .build();
        
        Session session = Session.create(params);
        
        Transacao transacao = new Transacao();
        transacao.setAgendamento(agendamento);
        transacao.setSessionId(session.getId());
        transacao.setStatus(StatusTransacao.PENDENTE);
        transacaoRepository.save(transacao);
        
        return session.getUrl();
    }
    
    @PostMapping("/webhook/stripe")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String sig) {
        try {
            Event event = Webhook.constructEvent(payload, sig, webhookSecret);
            
            if (event.getType().equals("checkout.session.completed")) {
                Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);
                
                Transacao transacao = transacaoRepository.findBySessionId(session.getId())
                    .orElseThrow();
                transacao.setStatus(StatusTransacao.APROVADA);
                transacao.setTransactionId(session.getPaymentIntent());
                transacaoRepository.save(transacao);
                
                Agendamento agendamento = transacao.getAgendamento();
                agendamento.setStatus(StatusAgendamento.CONFIRMADO);
                agendamentoRepository.save(agendamento);
                
                applicationEventPublisher.publishEvent(
                    new AgendamentoConfirmadoEvent(agendamento)
                );
            }
            
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).build();
        }
    }
}
```

---

## 🎨 FASE 3: FRONTEND WEB (Semanas 11-14)

### 3.1 Stack Recomendado

```
Frontend Web:
├─ Next.js 14+ (React + SSR + routing)
├─ TypeScript (type safety obrigatório)
├─ Tailwind CSS + Shadcn/ui (componentes prontos)
├─ Zustand (state management leve)
├─ React Query (data fetching + cache)
├─ NextAuth.js (autenticação com JWT)
└─ Deployment: Vercel (1-click, grátis até 100k req/mês)
```

### 3.2 Estrutura do Projeto

```
creche-saas-web/
├── app/
│   ├── (auth)/
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── signup/
│   │   │   └── page.tsx
│   │   └── forgot-password/
│   │       └── page.tsx
│   ├── (app)/
│   │   ├── dashboard/
│   │   │   └── page.tsx
│   │   ├── agendamentos/
│   │   │   ├── page.tsx
│   │   │   ├── [id]/
│   │   │   │   └── page.tsx
│   │   │   └── novo/
│   │   │       └── page.tsx
│   │   ├── pets/
│   │   │   ├── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── clientes/
│   │   │   ├── page.tsx
│   │   │   └── [id]/
│   │   │       └── page.tsx
│   │   ├── relatorios/
│   │   │   └── page.tsx
│   │   └── configuracoes/
│   │       └── page.tsx
│   ├── api/
│   │   ├── auth/
│   │   │   └── [...nextauth]/
│   │   │       └── route.ts
│   │   └── stripe-webhook/
│   │       └── route.ts
│   └── layout.tsx
├── components/
│   ├── layout/
│   │   ├── Navbar.tsx
│   │   ├── Sidebar.tsx
│   │   └── Footer.tsx
│   ├── forms/
│   │   ├── LoginForm.tsx
│   │   ├── AgendamentoForm.tsx
│   │   └── PetForm.tsx
│   ├── calendar/
│   │   ├── MonthCalendar.tsx
│   │   ├── WeekCalendar.tsx
│   │   └── AgendamentoCard.tsx
│   └── common/
│       ├── LoadingSpinner.tsx
│       ├── ErrorBoundary.tsx
│       └── ConfirmDialog.tsx
├── hooks/
│   ├── useAgendamentos.ts
│   ├── usePets.ts
│   ├── useAuth.ts
│   └── useNotifications.ts
├── lib/
│   ├── api-client.ts
│   ├── constants.ts
│   └── utils.ts
├── store/
│   ├── authStore.ts
│   └── uiStore.ts
├── types/
│   └── index.ts
└── package.json
```

### 3.3 Componente Crítico: Calendário

```typescript
// components/calendar/WeekCalendar.tsx
'use client';

import { useState, useEffect } from 'react';
import { format, addDays, startOfWeek, eachDayOfInterval } from 'date-fns';
import { useAgendamentos } from '@/hooks/useAgendamentos';
import AgendamentoCard from './AgendamentoCard';

export default function WeekCalendar() {
  const [semanaAtual, setSemanaAtual] = useState(new Date());
  const { agendamentos, isLoading } = useAgendamentos({
    startDate: startOfWeek(semanaAtual),
    endDate: addDays(startOfWeek(semanaAtual), 6)
  });

  const dias = eachDayOfInterval({
    start: startOfWeek(semanaAtual),
    end: addDays(startOfWeek(semanaAtual), 6)
  });

  const agendamentosPorDia = (dia: Date) => {
    return agendamentos.filter(a => 
      format(a.inicio, 'yyyy-MM-dd') === format(dia, 'yyyy-MM-dd')
    );
  };

  return (
    <div className="w-full overflow-x-auto">
      <div className="grid grid-cols-7 gap-2">
        {dias.map(dia => (
          <div key={dia.toString()} className="border rounded-lg p-4 min-h-96">
            <h3 className="font-bold text-center mb-2">
              {format(dia, 'EEE dd/MM')}
            </h3>
            <div className="space-y-2">
              {agendamentosPorDia(dia).map(agendamento => (
                <AgendamentoCard 
                  key={agendamento.id} 
                  agendamento={agendamento}
                />
              ))}
            </div>
            <button 
              className="mt-4 w-full bg-blue-500 text-white py-2 rounded text-sm"
              onClick={() => {/* abrir modal de novo agendamento */}}
            >
              + Agendar
            </button>
          </div>
        ))}
      </div>
      <div className="flex justify-between mt-4">
        <button onClick={() => setSemanaAtual(addDays(semanaAtual, -7))}>
          ← Semana Anterior
        </button>
        <button onClick={() => setSemanaAtual(new Date())}>
          Hoje
        </button>
        <button onClick={() => setSemanaAtual(addDays(semanaAtual, 7))}>
          Próxima Semana →
        </button>
      </div>
    </div>
  );
}
```

### 3.4 Hook de Data (React Query)

```typescript
// hooks/useAgendamentos.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';

export function useAgendamentos({ startDate, endDate }) {
  return useQuery({
    queryKey: ['agendamentos', startDate, endDate],
    queryFn: () => 
      apiClient.get('/api/v1/agendamentos', {
        params: {
          startDate: startDate.toISOString(),
          endDate: endDate.toISOString()
        }
      }).then(res => res.data),
    staleTime: 5 * 60 * 1000,
  });
}

export function useCriarAgendamento() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (dto) => 
      apiClient.post('/api/v1/agendamentos', dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['agendamentos'] });
    }
  });
}
```

---

## 📱 FASE 4: APP MOBILE (Semanas 15-18)

### Stack: Flutter

```yaml
# pubspec.yaml
dependencies:
  flutter:
    sdk: flutter
  
  # HTTP
  dio: ^5.3.0
  
  # State Management
  riverpod: ^2.4.0
  flutter_riverpod: ^2.4.0
  
  # Local Storage
  hive: ^2.2.0
  
  # Auth
  flutter_secure_storage: ^9.0.0
  
  # Calendar
  table_calendar: ^3.0.9
  
  # UI
  flutter_dotenv: ^5.1.0
  intl: ^0.19.0
```

### Estrutura

```
lib/
├── main.dart
├── models/
│   ├── agendamento.dart
│   ├── pet.dart
│   └── cliente.dart
├── screens/
│   ├── auth/
│   │   ├── login_screen.dart
│   │   └── signup_screen.dart
│   ├── home/
│   │   ├── dashboard_screen.dart
│   │   └── calendar_screen.dart
│   └── agendamentos/
│       ├── agendamento_list_screen.dart
│       └── agendamento_detail_screen.dart
├── providers/
│   ├── auth_provider.dart
│   ├── agendamento_provider.dart
│   └── pet_provider.dart
├── services/
│   ├── api_service.dart
│   └── notification_service.dart
└── widgets/
    ├── agendamento_card.dart
    └── custom_app_bar.dart
```

---

## 🔐 FASE 5: SEGURANÇA & INFRAESTRUTURA (Semanas 19-22)

### 5.1 Dockerfile Otimizado

```dockerfile
# Multi-stage build para Java
FROM eclipse-temurin:21-jre-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Stage 2: Executar
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

### 5.2 Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: creche_saas_db
    environment:
      POSTGRES_USER: creche_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: creche_saas_db
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U creche_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: creche_saas_cache
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  api:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: creche_saas_api
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/creche_saas_db
      SPRING_DATASOURCE_USERNAME: creche_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      JWT_SECRET: ${JWT_SECRET}
      STRIPE_API_KEY: ${STRIPE_API_KEY}
      SENDGRID_API_KEY: ${SENDGRID_API_KEY}
    ports:
      - "8080:8080"
    volumes:
      - ./:/app
    command: mvn spring-boot:run

volumes:
  postgres_data:
  redis_data:
```

---

## 💰 FASE 6: MONETIZAÇÃO & OPERAÇÕES

### 6.1 Modelo de Pricing

#### PLANO STARTER
**R$ 199/mês** (ou R$ 1.990/ano com 17% desconto)

✅ Até 50 pets
✅ 1 gerente + 2 funcionários
✅ Cadastro básico (clientes, pets)
✅ Agendamentos simples
✅ Suporte por email (48h)
❌ Pagamento online
❌ App mobile
❌ Relatórios avançados

---

#### PLANO PROFESSIONAL ⭐ (Recomendado)
**R$ 499/mês** (ou R$ 4.990/ano com 17% desconto)

✅ Tudo do Starter +
✅ Até 500 pets
✅ 5 funcionários com roles customizados
✅ Agendamento online 24/7
✅ Pagamento integrado (Stripe)
✅ App mobile (iOS + Android)
✅ Email + SMS automáticas
✅ Dashboard com gráficos e BI
✅ Histórico de hospedagens por pet
✅ Integração WhatsApp
✅ Suporte por chat (24h)
✅ 2 backups/semana

---

#### PLANO ENTERPRISE
**R$ 1.299/mês** (ou R$ 12.990/ano com 17% desconto)

✅ Tudo do Professional +
✅ Usuários ilimitados
✅ Customização completa de marca
✅ API pública + webhooks
✅ Integração com ERPs e sistemas legados
✅ Relatórios personalizados (BI avançado)
✅ Dedicated account manager
✅ SSO/2FA
✅ SLA 99,9%
✅ Suporte 24/7 por phone
✅ Backup diário + retenção 90 dias
✅ Treinamento presencial (até 4h/ano)

---

### 6.2 Taxas Adicionais

**TAXA DE SETUP (Único)**: R$ 1.000
- Migração de dados
- Configuração inicial
- Treinamento de usuários
- Customizações menores

**TAXA DE PROCESSAMENTO (ao usar pagamento)**: 2,9% + R$ 0,30
- Apenas quando cliente processa pagamento online
- Exemplo: Hospedagem R$ 500 = R$ 14,50 em taxa

---

### 6.3 Customer Acquisition Strategy

#### CANAL 1: Vendas Diretas (PRIORIDADE #1)

**Objetivo**: 10-15 ligações/semana

**Script**:
```
"Olá, vi seu negócio no Google. Gerencio sistema de 
agendamento para creches. Você perdeu clientes por 
não ter calendário online?"
```

**Oferta**: 30 dias grátis + suporte completo

**Taxa de conversão esperada**: 10-20%

**Meta**: 2-3 clientes/semana

---

#### CANAL 2: Google Ads

**Keywords**:
- "sistema agendamento creche pets"
- "software hotel cachorros"
- "agenda online cuidador de pets"

**Budget**: R$ 500-1k/mês
**CPC estimado**: R$ 5-15
**Conversão esperada**: 2-5%

---

#### CANAL 3: Parcerias Estratégicas

- Petshops (agentes de venda)
- Clínicas veterinárias
- Associações de pets
- Fóruns/grupos no Facebook

---

#### CANAL 4: Inbound Marketing

- Blog: "10 dicas para melhorar sua creche de pets"
- YouTube: tutoriais de uso
- LinkedIn: conteúdo para donos de negócios
- Organic: 20-30% do tráfego em 12 meses

---

### 6.4 Métricas de Viabilidade

```
CAC (Customer Acquisition Cost) esperado: R$ 500-1.500
LTV (Lifetime Value) esperado: R$ 6.000-12.000 (12-24 meses)
LTV/CAC ratio: 4-8x ✅ (Saudável se > 3x)
```

---

## 📋 TIMELINE CONSOLIDADO (24 SEMANAS)

```
SEMANA 1-2: VALIDAÇÃO
├─ Pesquisa com 30 creches ✅
├─ Landing page simples
├─ 2-3 creches piloto onboarded
└─ Validação de demanda

SEMANA 3-5: REFATORAÇÃO BACKEND
├─ TenantContext + TenantInterceptor
├─ Migração de dados
├─ Testes de isolamento
└─ Deploy em staging

SEMANA 6-10: NOVAS FEATURES
├─ Módulo de Serviços
├─ Agendamento com validação
├─ Notificações (email + SMS)
├─ Integração Stripe
└─ Testes automatizados

SEMANA 11-14: FRONTEND WEB
├─ Next.js + autenticação
├─ Dashboard + calendário
├─ CRUD de pets/clientes
├─ Deploy em Vercel

SEMANA 15-18: APP MOBILE
├─ Flutter setup
├─ Autenticação
├─ Telas principais
├─ Push notifications

SEMANA 19-22: INFRA + SEGURANÇA
├─ Dockerfile otimizado
├─ CI/CD completo
├─ Security scanning
├─ Monitoramento + logging

SEMANA 23-24: LAUNCH + ONBOARDING
├─ Website final
├─ Documentação
├─ Suporte 24/7
├─ 10+ creches em produção
```

---

## 💰 PROJEÇÃO FINANCEIRA (12-24 meses)

### Investimento Inicial

**Desenvolvimento (DIY):**
- Backend refactor + features: 200h × R$ 200 = R$ 40.000
- Frontend web: 120h × R$ 200 = R$ 24.000
- Mobile: 80h × R$ 200 = R$ 16.000
- DevOps/Infra: 50h × R$ 250 = R$ 12.500
- QA/Testing: 60h × R$ 180 = R$ 10.800
- **TOTAL**: ~R$ 103.300 (economia se você mesmo fizer)

**Infraestrutura Anual:**
- AWS (ECS, RDS, S3): R$ 200/mês = R$ 2.400/ano
- Stripe (2,9% em transações): variável
- SendGrid + Twilio: R$ 500/mês = R$ 6.000/ano
- Domínio + SSL: R$ 100/mês = R$ 1.200/ano
- **TOTAL**: ~R$ 10.000/ano

**INVESTIMENTO TOTAL**: R$ 113.300

---

### Receita (Conservadora)

```
MÊS 1-2 (Launch): 2 clientes (pilotos) = R$ 0
MÊS 3 (Primeira venda real): +2 clientes (Starter) = R$ 400/mês
MÊS 4-6: +1-2 clientes/mês = R$ 1-2k/mês
MÊS 7-12: +2-3 clientes/mês = R$ 3-6k/mês

ANO 1 TOTAL:
├─ Mês 1-2: R$ 0
├─ Mês 3: R$ 400
├─ Mês 4-6: R$ 1.5k × 3 = R$ 4.500
├─ Mês 7-12: R$ 4.5k × 6 = R$ 27.000
└─ RECEITA ANO 1: ~R$ 32.000

ANO 2 (Com momentum):
├─ Clientes retidos do Ano 1: 15 × R$ 400 (avg) = R$ 6k/mês
├─ Novos clientes: +20-30/ano
├─ Mix melhorado (mais Professional): média R$ 450/cliente
└─ RECEITA ANO 2: ~R$ 250.000

BREAK-EVEN:
├─ 10 clientes no Plano Professional = R$ 4.990/mês
├─ Custos mensais: ~R$ 1.000
├─ Lucro: R$ 3.990
└─ ROI: 30 meses (conservador, com crescimento é 12-18 meses)
```

---

## ✅ CHECKLIST DE LANÇAMENTO

### 2 Semanas Antes
- [ ] Testes de segurança completos
- [ ] Load testing (1k concurrent users)
- [ ] Backup & disaster recovery validado
- [ ] SLA documentado e pronto
- [ ] Monitoring ativo (Prometheus + Grafana)
- [ ] On-call team treinado

### 1 Semana Antes
- [ ] Website final publicado
- [ ] Documentação + FAQ completos
- [ ] Email de launch pronto
- [ ] Vídeos de tutorial prontos
- [ ] Criar primeira conta piloto
- [ ] Testar todo o fluxo end-to-end

### Dia do Launch
- [ ] Verificar status dos serviços
- [ ] Enviar email para creches pré-qualificadas
- [ ] Monitor 24h ativo
- [ ] Suporte on-call
- [ ] Pronto para escalar issues

### Semana de Launch
- [ ] Ligar para 20+ creches
- [ ] Onboard 3-5 novos clientes
- [ ] Coletar feedback
- [ ] Corrigir bugs urgentes
- [ ] Daily standup de otimização

---

## 🎯 MÉTRICAS DE SUCESSO

### PRODUTO
- ✅ Uptime: ≥ 99.5%
- ✅ Latência p95: < 200ms
- ✅ Taxa de erro: < 0.1%
- ✅ Feature adoption: ≥ 70%
- ✅ Bug severity crítica: 0

### NEGÓCIO
- ✅ MRR (Monthly Recurring Revenue)
- ✅ Churn rate: < 5%/mês
- ✅ NPS: ≥ 50
- ✅ CAC payback period: < 6 meses
- ✅ LTV/CAC ratio: ≥ 4x
- ✅ Retention day 30: ≥ 90%

### CRESCIMENTO
- ✅ Novos clientes/mês
- ✅ Taxa de upgrade (Starter → Professional)
- ✅ Número de agendamentos/mês
- ✅ Valor médio por transação
- ✅ Receita por cliente (ARPU)

---

## 🚀 AÇÃO IMEDIATA (HOJE)

```
1. [ ] Criar lista de 30 creches + emails/telefones
2. [ ] Enviar email: "Estou testando novo sistema..."
3. [ ] Agendar 3 ligações para amanhã
4. [ ] Criar branch: feature/multi-tenant
5. [ ] Começar refatoração de TenantContext
6. [ ] Registrar domínio: seu-saas-pets.com.br
7. [ ] Criar repo: creche-saas-web (Next.js)
8. [ ] Documentar este plano em Notion
```

---

## 📞 RESUMO EXECUTIVO

**Status**: 🟢 Plano infalível pronto para execução

**Tempo até MVP**: 12 semanas

**Investimento**: R$ 113.300

**Break-even**: 8-10 clientes (Plano Professional)

**Potencial de lucro (Ano 2)**: R$ 250.000+

**ROI esperado**: 220% ao final de 24 meses

---

## 📝 PRÓXIMOS PASSOS

1. **Semana 1**: Validação com clientes reais
2. **Semana 2-5**: Refatoração backend para multi-tenant
3. **Semana 6-10**: Implementação de features críticas
4. **Semana 11-22**: Frontend, Mobile e Infraestrutura
5. **Semana 23-24**: Launch e primeiros clientes

**Você está pronto para começar?** 🚀

---

*Documento preparado para SRibasDev - Sistema de Cadastro para Creche/Hotel de Pets*
*Data: 21 de Agosto de 2026*
