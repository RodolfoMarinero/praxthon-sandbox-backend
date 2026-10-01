package com.amisimecompila.speisandbox.shared.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class OperacionIdGenerator {

    private static final char[] ALFABETO =
            "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private final SecureRandom random = new SecureRandom();

    public String generar() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        BigInteger value = new BigInteger(1, bytes);
        StringBuilder result = new StringBuilder(26);

        while (result.length() < 26) {
            result.append(ALFABETO[
                    value.and(BigInteger.valueOf(31)).intValue()
            ]);
            value = value.shiftRight(5);
        }

        return "op_" + result.reverse();
    }
}
