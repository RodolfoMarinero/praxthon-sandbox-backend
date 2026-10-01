package com.amisimecompila.speisandbox.operacion.api.dto.response;

import java.math.BigDecimal;

public record ImporteResponse(
        BigDecimal valor,
        String divisa
) {
}
