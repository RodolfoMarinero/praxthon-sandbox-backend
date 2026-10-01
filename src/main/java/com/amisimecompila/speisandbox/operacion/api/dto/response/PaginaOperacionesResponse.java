package com.amisimecompila.speisandbox.operacion.api.dto.response;

import java.util.List;

public record PaginaOperacionesResponse(
        List<OperacionResponse> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
}
