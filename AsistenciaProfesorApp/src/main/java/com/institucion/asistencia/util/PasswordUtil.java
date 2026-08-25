package com.institucion.asistencia.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad simple para hashear contraseñas con SHA-256.
 *
 * Nota para el estudiante: SHA-256 "a secas" es mejor que texto plano,
 * pero en un proyecto profesional real se recomienda usar BCrypt o
 * Argon2 (con "salt"). Se deja SHA-256 aquí para no depender de
 * librerías externas que no vienen incluidas en el JDK.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashear(String textoPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible", e);
        }
    }

    public static boolean coincide(String textoPlano, String hashGuardado) {
        return hashear(textoPlano).equalsIgnoreCase(hashGuardado);
    }
}
