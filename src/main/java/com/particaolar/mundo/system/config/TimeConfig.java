package com.particaolar.mundo.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Fonte única de tempo da aplicação. Evita LocalDateTime.now() espalhado
 * (testabilidade) e resolve fuso horário (o servidor pode rodar em UTC).
 * SaaS: cada empresa terá seu ZoneId configurável.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(@Value("${app.fuso:America/Sao_Paulo}") String fuso) {
        return Clock.system(ZoneId.of(fuso));
    }
}
