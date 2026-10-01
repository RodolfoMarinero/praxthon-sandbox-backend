package com.amisimecompila.speisandbox.validacion;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ClabeValidatorTest {
    private final ClabeValidator validator = new ClabeValidator();

    @Test
    void validaVectoresOficiales() {
        assertThat(validator.tieneDigitoVerificadorValido(
                "032180000118359719"
        )).isTrue();
        assertThat(validator.tieneDigitoVerificadorValido(
                "103150124152345786"
        )).isTrue();
    }
}
