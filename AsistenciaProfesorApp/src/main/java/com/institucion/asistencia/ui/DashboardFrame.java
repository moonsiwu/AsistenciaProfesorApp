package com.institucion.asistencia.ui;

import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal con barra lateral centrada y armónica estilo Dashboard SaaS institucional.
 */
public class DashboardFrame extends JFrame {

    private final Profesor profesor;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);

    private static final String TARJETA_ASISTENCIA = "asistencia";
    private static final String TARJETA_ESTUDIANTES = "estudiantes";
    private static final String TARJETA_REPORTES = "reportes";

    private final List<JButton> botonesMenu = new ArrayList<>();
    private String tarjetaActiva = TARJETA_ASISTENCIA;

    public DashboardFrame(Profesor profesor) {
        super("Sistema Institucional de Asistencia — " + profesor.getNombreCompleto());
        this.profesor = profesor;
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(940, 620));
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);

        raiz.add(construirBarraLateral(), BorderLayout.WEST);

        panelContenido.setBackground(Tema.FONDO);
        panelContenido.add(new PanelTomarAsistencia(profesor), TARJETA_ASISTENCIA);
        panelContenido.add(new PanelEstudiantes(profesor), TARJETA_ESTUDIANTES);
        panelContenido.add(new PanelReportes(profesor), TARJETA_REPORTES);

        raiz.add(panelContenido, BorderLayout.CENTER);
        setContentPane(raiz);
        mostrarTarjeta(TARJETA_ASISTENCIA);
    }

    private JPanel construirBarraLateral() {
        JPanel barra = new JPanel();
        barra.setBackground(Tema.AZUL_SIDEBAR);
        barra.setPreferredSize(new Dimension(240, 0));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(new EmptyBorder(24, 16, 20, 16));

        // --- Encabezado centrado de la barra lateral ---
        JLabel logoTexto = new JLabel("EduAsistencia", SwingConstants.CENTER);
        logoTexto.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoTexto.setForeground(Color.WHITE);
        logoTexto.setAlignmentX(Component.CENTER_ALIGNMENT);

        barra.add(logoTexto);
        barra.add(Box.createVerticalStrut(20));

        // --- Tarjeta de Perfil Docente Centrada ---
        JPanel cardPerfil = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x33, 0x41, 0x55)); // Slate 700
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        cardPerfil.setOpaque(false);
        cardPerfil.setLayout(new BorderLayout(10, 0));
        cardPerfil.setBorder(new EmptyBorder(10, 12, 10, 12));
        cardPerfil.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        cardPerfil.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inicial circular
        String inicial = profesor.getNombreCompleto() != null && !profesor.getNombreCompleto().isEmpty()
                ? profesor.getNombreCompleto().substring(0, 1).toUpperCase() : "D";
        JLabel avatar = new JLabel(inicial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.AZUL_PRINCIPAL);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setForeground(Color.WHITE);

        JPanel panelInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        panelInfo.setOpaque(false);
        JLabel lblNombre = new JLabel(profesor.getNombreCompleto());
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNombre.setForeground(Color.WHITE);
        JLabel lblRol = new JLabel("Docente Titular");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRol.setForeground(new Color(0x94, 0xA3, 0xB8)); // Slate 400
        panelInfo.add(lblNombre);
        panelInfo.add(lblRol);

        cardPerfil.add(avatar, BorderLayout.WEST);
        cardPerfil.add(panelInfo, BorderLayout.CENTER);
        barra.add(cardPerfil);
        barra.add(Box.createVerticalStrut(24));

        // --- Título de Sección Centrado ---
        JLabel lblMenu = new JLabel("GESTIÓN Y CONTROL", SwingConstants.CENTER);
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenu.setForeground(new Color(0x94, 0xA3, 0xB8));
        lblMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        barra.add(lblMenu);
        barra.add(Box.createVerticalStrut(8));

        // --- Botones de Navegación Centrados ---
        barra.add(crearBotonMenu("Tomar Asistencia", TARJETA_ASISTENCIA));
        barra.add(Box.createVerticalStrut(6));
        barra.add(crearBotonMenu("Gestión de Estudiantes", TARJETA_ESTUDIANTES));
        barra.add(Box.createVerticalStrut(6));
        barra.add(crearBotonMenu("Reportes y Consultas", TARJETA_REPORTES));

        barra.add(Box.createVerticalGlue());

        // --- Cerrar Sesión ---
        barra.add(crearBotonCerrarSesion());

        return barra;
    }

    private JButton crearBotonMenu(String texto, String tarjeta) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean activo = tarjeta.equals(tarjetaActiva);

                if (activo) {
                    g2.setColor(Tema.AZUL_ACTIVO);
                } else if (getModel().isRollover()) {
                    g2.setColor(Tema.AZUL_HOVER);
                } else {
                    g2.setColor(Tema.AZUL_SIDEBAR);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setBorder(new EmptyBorder(10, 14, 10, 14));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("tarjeta", tarjeta);
        boton.addActionListener(e -> mostrarTarjeta(tarjeta));

        botonesMenu.add(boton);
        return boton;
    }

    private void mostrarTarjeta(String tarjeta) {
        this.tarjetaActiva = tarjeta;
        cardLayout.show(panelContenido, tarjeta);
        for (JButton btn : botonesMenu) {
            btn.repaint();
        }
    }

    private JButton crearBotonCerrarSesion() {
        JButton boton = new JButton("Cerrar Sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x99, 0x1B, 0x1B)); // Red 800
                } else {
                    g2.setColor(new Color(0x33, 0x41, 0x55)); // Slate 700
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setForeground(new Color(0xF1, 0xF5, 0xF9));
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Deseas cerrar la sesión actual?",
                    "Cerrar Sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (resp == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        return boton;
    }
}
