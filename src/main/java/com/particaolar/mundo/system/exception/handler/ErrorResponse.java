package com.particaolar.mundo.system.exception.handler;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp
) {}