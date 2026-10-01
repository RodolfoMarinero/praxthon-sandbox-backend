package com.amisimecompila.speisandbox.validacion;

import org.springframework.stereotype.Component;

@Component
public class ClabeValidator {

    private static final int[] PESOS = {3, 7, 1};

    public boolean tieneFormatoValido(String clabe) {
        return clabe != null && clabe.matches("^[0-9]{18}$");
    }

    public boolean tieneDigitoVerificadorValido(String clabe) {
        if (!tieneFormatoValido(clabe)) {
            return false;
        }

        int suma = 0;
        for (int index = 0; index < 17; index++) {
            int digito = Character.digit(clabe.charAt(index), 10);
            suma += (digito * PESOS[index % PESOS.length]) % 10;
        }

        int esperado = (10 - (suma % 10)) % 10;
        return esperado == Character.digit(clabe.charAt(17), 10);
    }

    public boolean perteneceAInstitucion(
            String clabe,
            String institucion
    ) {
        return tieneFormatoValido(clabe)
                && institucion != null
                && clabe.startsWith(institucion);
    }
}
