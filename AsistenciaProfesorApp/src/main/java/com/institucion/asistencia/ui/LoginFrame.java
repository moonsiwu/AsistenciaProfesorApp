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

        // --- Logo / Emblema institucional oficial (Escudo BD Azul Cobalto + Reloj Digital Gris Metálico) ---
        JPanel panelLogo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // 1. Escudo de base de datos en Azul Cobalto (#0047AB)
                g2.setColor(new Color(0x00, 0x47, 0xAB));
                Polygon escudo = new Polygon();
                escudo.addPoint(cx - 24, cy - 22);
                escudo.addPoint(cx + 24, cy - 22);
                escudo.addPoint(cx + 24, cy + 4);
                escudo.addPoint(cx, cy + 25);
                escudo.addPoint(cx - 24, cy + 4);
                g2.fill(escudo);

                // Capas sutiles de cilindro relacional dentro del escudo
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(cx - 16, cy - 18, 32, 6, 4, 4);
                g2.drawRoundRect(cx - 16, cy - 10, 32, 6, 4, 4);

                // 2. Reloj digital sobre el escudo en Gris Metálico (#64748B)
                g2.setColor(new Color(0x47, 0x55, 0x69));
                g2.fillRoundRect(cx - 19, cy - 2, 38, 18, 6, 6);
                g2.setColor(new Color(0x94, 0xA3, 0xB8));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(cx - 19, cy - 2, 38, 18, 6, 6);

                // Pantalla digital interna LCD
                g2.setColor(new Color(0x0F, 0x17, 0x2A));
                g2.fillRoundRect(cx - 16, cy + 1, 32, 12, 4, 4);

                // Dígitos digitales nítidos (08:00)
                g2.setColor(new Color(0x38, 0xBD, 0xF8));
                g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2.drawString("08:00", cx - 14, cy + 10);

                g2.dispose();
            }
        };
        panelLogo.setOpaque(false);
        panelLogo.setPreferredSize(new Dimension(400, 60));
        panelLogo.setMaximumSize(new Dimension(400, 60));
        panelLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Textos de Título, Slogan y Subtítulo Centrados ---
        JLabel titulo = new JLabel("Control de Asistencia", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(new Color(0x0F, 0x17, 0x2A));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel slogan = new JLabel("\"Precisión en tiempo real, control que transforma tu empresa.\"", SwingConstants.CENTER);
        slogan.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        slogan.setForeground(new Color(0x00, 0x47, 0xAB));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Portal de Acceso para Docentes", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(0x47, 0x55, 0x69));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(panelLogo);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(2));
        tarjeta.add(slogan);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(subtitulo);
        tarjeta.add(Box.createVerticalStrut(18));

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

        JLabel lblInfoDemo = new JLabel("<html><div style='text-align: center;'><b>Comparar Cuentas (Clave: 123456):</b><br><code>profesor1</code> | <code>profesor2</code> | <code>estudiante1</code></div></html>", SwingConstants.CENTER);
        lblInfoDemo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfoDemo.setForeground(new Color(0x47, 0x55, 0x69));
        lblInfoDemo.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelBotonesDemo = new JPanel(new GridLayout(1, 3, 6, 0));
        panelBotonesDemo.setOpaque(false);

        JButton btnProf1 = new JButton("Prof. 1");
        btnProf1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnProf1.setForeground(new Color(0x1D, 0x4E, 0xD8));
        btnProf1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProf1.addActionListener(e -> {
            campoUsuario.setText("profesor1");
            campoContrasena.setText("123456");
            etiquetaError.setText(" ");
        });

        JButton btnProf2 = new JButton("Prof. 2");
        btnProf2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnProf2.setForeground(new Color(0x15, 0x80, 0x3D));
        btnProf2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProf2.addActionListener(e -> {
            campoUsuario.setText("profesor2");
            campoContrasena.setText("123456");
            etiquetaError.setText(" ");
        });

        JButton btnEst1 = new JButton("Estudiante");
        btnEst1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnEst1.setForeground(new Color(0x7E, 0x22, 0xCE));
        btnEst1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEst1.addActionListener(e -> {
            campoUsuario.setText("estudiante1");
            campoContrasena.setText("123456");
            etiquetaError.setText(" ");
        });

        panelBotonesDemo.add(btnProf1);
        panelBotonesDemo.add(btnProf2);
        panelBotonesDemo.add(btnEst1);

        panelDemo.add(lblInfoDemo, BorderLayout.CENTER);
        panelDemo.add(panelBotonesDemo, BorderLayout.SOUTH);
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
