package com.amisimecompila.speisandbox.operacion.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudOperacion;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudT2TRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SolicitudOperacionJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializaT2TPorDiscriminador() throws Exception {
        String json = """
                {
                  "tipoOperacion": "T2T",
                  "referenciaSeguimiento": "R1",
                  "importe": {"valor": 1, "divisa": "MXN"},
                  "emisor": {
                    "institucion": "801",
                    "cuenta": "801180000118359717",
                    "nombre": "A"
                  },
                  "receptor": {
                    "institucion": "802",
                    "cuenta": "802180000200030011",
                    "nombre": "B"
                  },
                  "concepto": "Pago",
                  "folioNumerico": 1
                }
                """;

        SolicitudOperacion result = mapper.readValue(
                json,
                SolicitudOperacion.class
        );

        assertThat(result).isInstanceOf(SolicitudT2TRequest.class);
    }
}
