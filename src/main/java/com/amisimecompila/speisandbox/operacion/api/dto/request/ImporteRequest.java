package com.amisimecompila.speisandbox.operacion.api.dto.request;

import java.math.BigDecimal;

public record ImporteRequest(
        BigDecimal valor,
        String divisa
) {
}
