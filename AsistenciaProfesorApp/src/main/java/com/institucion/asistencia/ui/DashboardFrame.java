package com.institucion.asistencia.ui;

import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.ServicioNube;
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

    private PanelTomarAsistencia panelTomarAsistencia;
    private PanelEstudiantes panelEstudiantes;
    private PanelReportes panelReportes;
    private JButton botonEstadoNube;

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

        panelTomarAsistencia = new PanelTomarAsistencia(profesor);
        panelEstudiantes = new PanelEstudiantes(profesor);
        panelReportes = new PanelReportes(profesor);

        panelContenido.setBackground(Tema.FONDO);
        panelContenido.add(panelTomarAsistencia, TARJETA_ASISTENCIA);
        panelContenido.add(panelEstudiantes, TARJETA_ESTUDIANTES);
        panelContenido.add(panelReportes, TARJETA_REPORTES);

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

        // --- Botón de Sincronización en la Nube ---
        botonEstadoNube = crearBotonEstadoNube();
        barra.add(botonEstadoNube);
        barra.add(Box.createVerticalStrut(8));

        // --- Cerrar Sesión ---
        barra.add(crearBotonCerrarSesion());

        return barra;
    }

    private JButton crearBotonEstadoNube() {
        boolean conectado = ServicioNube.verificarConexion();
        String textoInicial = conectado ? "🟢 Nube Sincronizada" : "☁️ Configurar Nube";

        JButton boton = new JButton(textoInicial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x0F, 0x76, 0x6E)); // Teal 700
                } else {
                    g2.setColor(new Color(0x1E, 0x3A, 0x8A)); // Blue 900
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(new Color(255, 255, 255, 50));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> abrirDialogoConfigurarNube());
        return boton;
    }

    private void abrirDialogoConfigurarNube() {
        boolean conectado = ServicioNube.verificarConexion();
        String urlActual = ServicioNube.obtenerUrlActual();
        boolean esFirebase = ServicioNube.esFirebaseExterno();

        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setPreferredSize(new Dimension(480, 260));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        String estadoHtml = conectado
                ? "<span style='color:#166534;'><b>● CONECTADO EN TIEMPO REAL</b></span> (" + (esFirebase ? "Google Firebase" : "Nube Sincronizada Web + Java") + ")"
                : "<span style='color:#991b1b;'><b>● MODO RESPALDO LOCAL</b></span>";

        JLabel info = new JLabel("<html><div style='line-height:1.45;'>"
                + "Estado actual: " + estadoHtml + "<br><br>"
                + "<b>URL de Base de Datos en la Nube:</b><br>"
                + "<small style='color:#475569;'>Puedes usar el servidor nube integrado (<code>http://localhost:8080</code>) que sincroniza al instante con la página Web, o pegar una URL de <b>Google Firebase Realtime Database</b> (<code>https://tu-proyecto-default-rtdb.firebaseio.com</code>).</small>"
                + "</div></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTextField campoUrl = new JTextField(urlActual);
        Tema.estilizarCampoTexto(campoUrl);

        JPanel centro = new JPanel(new GridLayout(2, 1, 6, 6));
        centro.setOpaque(false);
        JButton btnUsarIntegrada = Tema.crearBotonSecundario("Usar Nube Integrada (http://localhost:8080)");
        btnUsarIntegrada.addActionListener(ev -> campoUrl.setText("http://localhost:8080"));
        centro.add(campoUrl);
        centro.add(btnUsarIntegrada);

        panel.add(info, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);

        Object[] opciones = {"Guardar y Sincronizar", "Refrescar Datos Ahora", "Cancelar"};
        int sel = JOptionPane.showOptionDialog(
                this,
                panel,
                "☁️ Configuración de Sincronización en la Nube",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (sel == 0) {
            String nuevaUrl = campoUrl.getText().trim();
            if (!nuevaUrl.isEmpty()) {
                boolean ok = ServicioNube.configurarNuevaUrlNube(nuevaUrl);
                actualizarBotonNube();
                recargarPanelActivo();
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "¡Conectado y sincronizado exitosamente con la nube!\nURL: " + ServicioNube.obtenerUrlActual(),
                            "Nube Sincronizada", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Se guardó la URL, pero no se pudo conectar en este momento.\nSe usará el respaldo automático hasta que responda.",
                            "Aviso de Conexión", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (sel == 1) {
            actualizarBotonNube();
            recargarPanelActivo();
            JOptionPane.showMessageDialog(this, "Datos actualizados desde la nube.",
                    "Sincronización", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void actualizarBotonNube() {
        if (botonEstadoNube != null) {
            boolean ok = ServicioNube.verificarConexion();
            botonEstadoNube.setText(ok ? "🟢 Nube Sincronizada" : "☁️ Configurar Nube");
            botonEstadoNube.repaint();
        }
    }

    private void recargarPanelActivo() {
        if (TARJETA_ASISTENCIA.equals(tarjetaActiva) && panelTomarAsistencia != null) {
            panelTomarAsistencia.recargarDesdeNube();
        } else if (TARJETA_ESTUDIANTES.equals(tarjetaActiva) && panelEstudiantes != null) {
            panelEstudiantes.recargarDesdeNube();
        } else if (TARJETA_REPORTES.equals(tarjetaActiva) && panelReportes != null) {
            panelReportes.recargarDesdeNube();
        }
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
        recargarPanelActivo();
        actualizarBotonNube();
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
