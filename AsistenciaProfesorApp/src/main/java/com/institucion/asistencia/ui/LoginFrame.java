package com.institucion.asistencia.ui;

import com.institucion.asistencia.dao.ProfesorDAO;
import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * Pantalla de inicio de sesión con diseño profesional corporativo,
 * estructura perfectamente centrada y armónica.
 */
public class LoginFrame extends JFrame {

    private final JTextField campoUsuario = new JTextField();
    private final JPasswordField campoContrasena = new JPasswordField();
    private final JLabel etiquetaError = new JLabel(" ", SwingConstants.CENTER);
    private final ProfesorDAO profesorDAO = new ProfesorDAO();

    public LoginFrame() {
        super("Sistema de Control de Asistencia - Acceso Institucional");
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 630);
        setMinimumSize(new Dimension(460, 600));
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void construirInterfaz() {
        JPanel panelFondo = new JPanel(new GridBagLayout());
        panelFondo.setBackground(new Color(0xF1, 0xF5, 0xF9)); // Slate 100 suave

        // Tarjeta principal blanca centrada
        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                // Borde suave
                g2.setColor(new Color(0xCB, 0xD5, 0xE1)); // Slate 300
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setPreferredSize(new Dimension(400, 550));
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(30, 36, 26, 36));

        // --- Logo / Emblema institucional vectorial limpio centrado ---
        JPanel panelLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                // Círculo azul institucional
                g2.setColor(new Color(0x1E, 0x40, 0xAF));
                g2.fillOval(cx - 24, cy - 24, 48, 48);
                // Dibujo de libro abierto blanco
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawArc(cx - 14, cy - 8, 14, 14, 0, 180);
                g2.drawArc(cx, cy - 8, 14, 14, 0, 180);
                g2.drawLine(cx, cy - 8, cx, cy + 8);
                g2.drawLine(cx - 14, cy - 1, cx - 14, cy + 7);
                g2.drawLine(cx + 14, cy - 1, cx + 14, cy + 7);
                g2.drawLine(cx - 14, cy + 7, cx, cy + 8);
                g2.drawLine(cx + 14, cy + 7, cx, cy + 8);
                g2.dispose();
            }
        };
        panelLogo.setOpaque(false);
        panelLogo.setPreferredSize(new Dimension(400, 52));
        panelLogo.setMaximumSize(new Dimension(400, 52));
        panelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Textos de Título y Subtítulo Centrados ---
        JLabel titulo = new JLabel("Control de Asistencia", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(new Color(0x0F, 0x17, 0x2A));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Portal de Acceso para Docentes", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(0x47, 0x55, 0x69));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(panelLogo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(22));

        // --- Campo Usuario ---
        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsuario.setForeground(new Color(0x1E, 0x29, 0x3B));
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblUsuario);
        tarjeta.add(Box.createVerticalStrut(4));

        Tema.estilizarCampoTexto(campoUsuario);
        campoUsuario.setHorizontalAlignment(JTextField.CENTER);
        campoUsuario.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        campoUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campoUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(campoUsuario);
        tarjeta.add(Box.createVerticalStrut(12));

        // --- Campo Contraseña ---
        JLabel lblContrasena = new JLabel("Contraseña");
        lblContrasena.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblContrasena.setForeground(new Color(0x1E, 0x29, 0x3B));
        lblContrasena.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblContrasena);
        tarjeta.add(Box.createVerticalStrut(4));

        Tema.estilizarCampoTexto(campoContrasena);
        campoContrasena.setHorizontalAlignment(JTextField.CENTER);
        campoContrasena.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        campoContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campoContrasena.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(campoContrasena);
        tarjeta.add(Box.createVerticalStrut(6));

        // --- Mensaje de Error Centrado ---
        etiquetaError.setForeground(new Color(0xDC, 0x26, 0x26));
        etiquetaError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaError.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(etiquetaError);
        tarjeta.add(Box.createVerticalStrut(10));

        // --- Botón Iniciar Sesión Centrado ---
        JButton botonEntrar = new JButton("Iniciar Sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0x1E, 0x3A, 0x8A));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x25, 0x63, 0xEB));
                } else {
                    g2.setColor(new Color(0x1D, 0x4E, 0xD8));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        botonEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        botonEntrar.setForeground(Color.WHITE);
        botonEntrar.setContentAreaFilled(false);
        botonEntrar.setBorderPainted(false);
        botonEntrar.setFocusPainted(false);
        botonEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        botonEntrar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        botonEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        botonEntrar.addActionListener(e -> intentarLogin());
        tarjeta.add(botonEntrar);
        tarjeta.add(Box.createVerticalStrut(16));

        // --- Cuadro de Credenciales de Prueba Centrado ---
        JPanel panelDemo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF8, 0xFA, 0xFC));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.setColor(new Color(0xE2, 0xE8, 0xF0));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        panelDemo.setOpaque(false);
        panelDemo.setLayout(new BorderLayout(8, 6));
        panelDemo.setBorder(new EmptyBorder(10, 12, 10, 12));
        panelDemo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        panelDemo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInfoDemo = new JLabel("<html><div style='text-align: center;'><b>Datos de prueba:</b> Usuario: <code>profesor1</code> | Clave: <code>123456</code></div></html>", SwingConstants.CENTER);
        lblInfoDemo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfoDemo.setForeground(new Color(0x47, 0x55, 0x69));
        lblInfoDemo.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnAutoLlenar = new JButton("Usar datos de prueba") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xDB, 0xEA, 0xFE));
                } else {
                    g2.setColor(new Color(0xEE, 0xF2, 0xF6));
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAutoLlenar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAutoLlenar.setForeground(new Color(0x1D, 0x4E, 0xD8));
        btnAutoLlenar.setContentAreaFilled(false);
        btnAutoLlenar.setBorderPainted(false);
        btnAutoLlenar.setFocusPainted(false);
        btnAutoLlenar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAutoLlenar.setBorder(new EmptyBorder(4, 8, 4, 8));
        btnAutoLlenar.addActionListener(e -> {
            campoUsuario.setText("profesor1");
            campoContrasena.setText("123456");
            etiquetaError.setText(" ");
        });

        panelDemo.add(lblInfoDemo, BorderLayout.CENTER);
        panelDemo.add(btnAutoLlenar, BorderLayout.SOUTH);
        tarjeta.add(panelDemo);

        // Atajo de teclado Enter
        KeyAdapter enterParaEntrar = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    intentarLogin();
                }
            }
        };
        campoUsuario.addKeyListener(enterParaEntrar);
        campoContrasena.addKeyListener(enterParaEntrar);

        panelFondo.add(tarjeta);
        setContentPane(panelFondo);
    }

    private void intentarLogin() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            etiquetaError.setText("Por favor, ingresa tu usuario y contraseña.");
            return;
        }

        try {
            Profesor profesor = profesorDAO.autenticar(usuario, contrasena);
            if (profesor == null) {
                etiquetaError.setText("Usuario o contraseña incorrectos.");
                return;
            }
            new DashboardFrame(profesor).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            String mensajeCorto = "Error al conectar con la base de datos.";
            String detalle = ex.getMessage();

            if (detalle != null) {
                String lower = detalle.toLowerCase();
                if (lower.contains("communications link failure") || lower.contains("connect")) {
                    mensajeCorto = "El servidor MySQL no responde. Verifica que el servicio este activo.";
                } else if (lower.contains("access denied")) {
                    mensajeCorto = "Credenciales de MySQL invalidas en config.properties.";
                } else if (lower.contains("unknown database")) {
                    mensajeCorto = "La base de datos 'asistencia_db' no existe.";
                }
            }

            etiquetaError.setText(mensajeCorto);
            JOptionPane.showMessageDialog(this,
                    mensajeCorto + "\n\nDetalle:\n" +
                    (detalle != null ? detalle : ex.toString()),
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
