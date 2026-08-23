package com.institucion.asistencia.util;

import java.awt.*;

/** Paleta de colores y fuentes centralizada, para que toda la app se vea consistente. */
public final class Tema {

    public static final Color AZUL_PRINCIPAL   = new Color(0x1E, 0x40, 0x6E);
    public static final Color AZUL_CLARO       = new Color(0x2F, 0x6F, 0xB0);
    public static final Color FONDO            = new Color(0xF4, 0xF6, 0xF9);
    public static final Color BLANCO           = Color.WHITE;
    public static final Color TEXTO_PRINCIPAL  = new Color(0x22, 0x28, 0x32);
    public static final Color TEXTO_SECUNDARIO = new Color(0x6B, 0x74, 0x80);
    public static final Color BORDE            = new Color(0xDD, 0xE2, 0xE8);

    public static final Color VERDE_PRESENTE   = new Color(0x1E, 0x8E, 0x3E);
    public static final Color ROJO_AUSENTE     = new Color(0xC6, 0x28, 0x28);
    public static final Color NARANJA_TARDE    = new Color(0xE6, 0x8A, 0x00);
    public static final Color GRIS_EXCUSA      = new Color(0x60, 0x6B, 0x78);

    public static final Font FUENTE_TITULO   = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL   = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FUENTE_BOLD     = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_BOTON    = new Font("SansSerif", Font.BOLD, 14);

    private Tema() {
    }
}
