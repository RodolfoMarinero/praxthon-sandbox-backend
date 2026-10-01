package com.amisimecompila.speisandbox.catalogo.domain;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CatalogoInstituciones {

    private static final Set<String> CODIGOS = Set.of(
            "801",
            "802",
            "803",
            "804",
            "805"
    );

    public boolean existe(String codigo) {

        return CODIGOS.contains(codigo);

    }
    public boolean permiteEmision(String codigo) {

        return existe(codigo) && !"804".equals(codigo);

    }
}
