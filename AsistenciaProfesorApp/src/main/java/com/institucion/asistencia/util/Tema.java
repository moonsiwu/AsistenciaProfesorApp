package com.institucion.asistencia.util;

import com.institucion.asistencia.model.Asistencia;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Sistema de diseño corporativo moderno (Design System) para el Sistema de Asistencia.
 * Proporciona tipografía de alta legibilidad, alineación armónica y centrada,
 * tarjetas estructuradas, botones interactivos y badges nítidos.
 */
public final class Tema {

    // --- Colores Institucionales Principales ---
    public static final Color AZUL_NAVY        = new Color(0x0F, 0x17, 0x2A); // Slate 900
    public static final Color AZUL_SIDEBAR     = new Color(0x1E, 0x29, 0x3B); // Slate 800
    public static final Color AZUL_HOVER       = new Color(0x33, 0x41, 0x55); // Slate 700
    public static final Color AZUL_ACTIVO      = new Color(0x25, 0x63, 0xEB); // Blue 600
    public static final Color AZUL_PRINCIPAL   = new Color(0x1D, 0x4E, 0xD8); // Blue 700
    public static final Color AZUL_CLARO       = new Color(0x3B, 0x82, 0xF6); // Blue 500
    public static final Color AZUL_SUAVE       = new Color(0xEE, 0xF2, 0xFF); // Indigo 50

    // --- Fondos y Superficies ---
    public static final Color FONDO            = new Color(0xF8, 0xFA, 0xFC); // Slate 50
    public static final Color BLANCO           = Color.WHITE;
    public static final Color CARD_BG          = Color.WHITE;
    public static final Color BORDE            = new Color(0xCB, 0xD5, 0xE1); // Slate 300
    public static final Color BORDE_SUAVE      = new Color(0xE2, 0xE8, 0xF0); // Slate 200

    // --- Tipografía y Textos de Alto Contraste ---
    public static final Color TEXTO_PRINCIPAL  = new Color(0x0F, 0x17, 0x2A); // Slate 900
    public static final Color TEXTO_SECUNDARIO = new Color(0x47, 0x55, 0x69); // Slate 600
    public static final Color TEXTO_MUTED      = new Color(0x64, 0x74, 0x8B); // Slate 500

    // --- Estados de Asistencia (Insignias / Badges con Alto Contraste) ---
    public static final Color VERDE_PRESENTE_BG = new Color(0xDC, 0xFC, 0xE7); // Green 100
    public static final Color VERDE_PRESENTE    = new Color(0x16, 0x65, 0x34); // Green 800

    public static final Color ROJO_AUSENTE_BG   = new Color(0xFE, 0xE2, 0xE2); // Red 100
    public static final Color ROJO_AUSENTE      = new Color(0x99, 0x1B, 0x1B); // Red 800

    public static final Color NARANJA_TARDE_BG  = new Color(0xFE, 0xF3, 0xC7); // Amber 100
    public static final Color NARANJA_TARDE     = new Color(0x92, 0x40, 0x0E); // Amber 800

    public static final Color MORADO_EXCUSA_BG  = new Color(0xF3, 0xE8, 0xFF); // Purple 100
    public static final Color MORADO_EXCUSA     = new Color(0x6B, 0x21, 0xA8); // Purple 800
    public static final Color GRIS_EXCUSA       = MORADO_EXCUSA;

    // --- Fuentes Universales de Alta Legibilidad ---
    public static final Font FUENTE_TITULO     = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FUENTE_SUBTITULO  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_BOLD       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_BOTON      = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_BADGE      = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FUENTE_NUMERO_CARD= new Font("Segoe UI", Font.BOLD, 24);

    private Tema() {
    }

    /**
     * Crea un panel contenedor tipo tarjeta blanca con bordes definidos.
     */
    public static JPanel crearTarjeta() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.setColor(BORDE_SUAVE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        return card;
    }

    /**
     * Crea una tarjeta métrica con título y valor numérico centrados armónicamente.
     */
    public static JPanel crearTarjetaMetrica(String titulo, String valorInicial, Color colorAcento) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.setColor(BORDE_SUAVE);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));

                // Barra superior de acento
                g2.setColor(colorAcento);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, 4, 10, 10));
                g2.fillRect(0, 3, getWidth() - 1, 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(8, 4));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setPreferredSize(new Dimension(150, 75));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(TEXTO_SECUNDARIO);

        JLabel lblValor = new JLabel(valorInicial, SwingConstants.CENTER);
        lblValor.setFont(FUENTE_NUMERO_CARD);
        lblValor.setForeground(colorAcento);
        lblValor.setName("valor");

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    /**
     * Crea un botón de acción principal (azul institucional).
     */
    public static JButton crearBotonPrimario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0x1E, 0x3A, 0x8A));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x25, 0x63, 0xEB));
                } else {
                    g2.setColor(AZUL_PRINCIPAL);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 20, 9, 20));
        return btn;
    }

    /**
     * Crea un botón secundario con borde y fondo blanco.
     */
    public static JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0xE2, 0xE8, 0xF0));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0xF1, 0xF5, 0xF9));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.setColor(BORDE);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setForeground(TEXTO_PRINCIPAL);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    /**
     * Crea un botón para acciones de eliminación o baja.
     */
    public static JButton crearBotonPeligro(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0xFE, 0xCD, 0xCD));
                } else if (getModel().isRollover()) {
                    g2.setColor(ROJO_AUSENTE_BG);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.setColor(new Color(0xF8, 0x71, 0x71));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FUENTE_BOTON);
        btn.setForeground(ROJO_AUSENTE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    /**
     * Aplica el estilo moderno Dashboard a cualquier JTable con cabeceras y celdas centradas.
     */
    public static void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(38);
        tabla.setFont(FUENTE_NORMAL);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(0xF1, 0xF5, 0xF9));
        tabla.setSelectionBackground(new Color(0xEE, 0xF2, 0xFF));
        tabla.setSelectionForeground(TEXTO_PRINCIPAL);
        tabla.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0xF8, 0xFA, 0xFC));
        header.setForeground(TEXTO_PRINCIPAL);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE_SUAVE));

        // Centrar las cabeceras de columna
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Renderizador de celdas centrado
        DefaultTableCellRenderer cellRendererCentrado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFC));
                    setForeground(TEXTO_PRINCIPAL);
                }
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, cellRendererCentrado);
        tabla.setDefaultRenderer(String.class, cellRendererCentrado);

        // Renderizador de Insignias para Estados (centrado)
        tabla.setDefaultRenderer(Asistencia.Estado.class, new EstadoBadgeRenderer());
    }

    /**
     * Renderizador de insignias (badges) para estados de asistencia, perfectamente centrado.
     */
    public static class EstadoBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(!isSelected);
            if (isSelected) {
                panel.setBackground(new Color(0xEE, 0xF2, 0xFF));
            } else {
                panel.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFC));
            }

            if (value != null) {
                String estadoStr = value.toString();
                Color bgColor = new Color(0xF1, 0xF5, 0xF9);
                Color textColor = TEXTO_SECUNDARIO;

                if ("PRESENTE".equalsIgnoreCase(estadoStr)) {
                    bgColor = VERDE_PRESENTE_BG;
                    textColor = VERDE_PRESENTE;
                } else if ("AUSENTE".equalsIgnoreCase(estadoStr)) {
                    bgColor = ROJO_AUSENTE_BG;
                    textColor = ROJO_AUSENTE;
                } else if ("TARDE".equalsIgnoreCase(estadoStr)) {
                    bgColor = NARANJA_TARDE_BG;
                    textColor = NARANJA_TARDE;
                } else if ("EXCUSA".equalsIgnoreCase(estadoStr)) {
                    bgColor = MORADO_EXCUSA_BG;
                    textColor = MORADO_EXCUSA;
                }

                final Color finalBg = bgColor;
                final Color finalTxt = textColor;
                final String textoBadge = estadoStr;

                JLabel badge = new JLabel(textoBadge, SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(finalBg);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setFont(FUENTE_BADGE);
                badge.setForeground(finalTxt);
                badge.setOpaque(false);
                badge.setBorder(new EmptyBorder(4, 14, 4, 14));

                panel.add(badge);
            }
            return panel;
        }
    }

    /**
     * Aplica estilo nítido a campos de texto con padding equilibrado.
     */
    public static void estilizarCampoTexto(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBackground(Color.WHITE);
        campo.setForeground(TEXTO_PRINCIPAL);
        campo.setCaretColor(AZUL_PRINCIPAL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }
}
