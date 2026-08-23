package com.institucion.asistencia.ui;

import com.institucion.asistencia.dao.ProfesorDAO;
import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

/** Pantalla inicial: el profesor ingresa usuario y contraseña. */
public class LoginFrame extends JFrame {

    private final JTextField campoUsuario = new JTextField(18);
    private final JPasswordField campoContrasena = new JPasswordField(18);
    private final JLabel etiquetaError = new JLabel(" ");
    private final ProfesorDAO profesorDAO = new ProfesorDAO();

    public LoginFrame() {
        super("Sistema de Asistencia - Iniciar sesión");
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void construirInterfaz() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Tema.FONDO);

        // Encabezado
        JPanel encabezado = new JPanel();
        encabezado.setBackground(Tema.AZUL_PRINCIPAL);
        encabezado.setPreferredSize(new Dimension(420, 130));
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBorder(new EmptyBorder(30, 0, 0, 0));

        JLabel icono = new JLabel("📋", SwingConstants.CENTER);
        icono.setFont(new Font("SansSerif", Font.PLAIN, 34));
        icono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("Control de Asistencia", SwingConstants.CENTER);
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Acceso para docentes", SwingConstants.CENTER);
        subtitulo.setFont(Tema.FUENTE_SUBTITULO);
        subtitulo.setForeground(new Color(220, 230, 240));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        encabezado.add(icono);
        encabezado.add(titulo);
        encabezado.add(subtitulo);

        // Formulario
        JPanel formulario = new JPanel();
        formulario.setBackground(Tema.FONDO);
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.setBorder(new EmptyBorder(40, 50, 20, 50));

        formulario.add(crearEtiquetaCampo("Usuario"));
        campoUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formulario.add(campoUsuario);
        formulario.add(Box.createVerticalStrut(16));

        formulario.add(crearEtiquetaCampo("Contraseña"));
        campoContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formulario.add(campoContrasena);
        formulario.add(Box.createVerticalStrut(8));

        etiquetaError.setForeground(Tema.ROJO_AUSENTE);
        etiquetaError.setFont(Tema.FUENTE_SUBTITULO);
        etiquetaError.setAlignmentX(Component.LEFT_ALIGNMENT);
        formulario.add(etiquetaError);
        formulario.add(Box.createVerticalStrut(12));

        JButton botonEntrar = new JButton("Iniciar sesión");
        estilizarBotonPrimario(botonEntrar);
        botonEntrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonEntrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        botonEntrar.addActionListener(e -> intentarLogin());
        formulario.add(botonEntrar);

        JLabel pie = new JLabel("Datos de prueba: profesor1 / 123456");
        pie.setFont(new Font("SansSerif", Font.ITALIC, 11));
        pie.setForeground(Tema.TEXTO_SECUNDARIO);
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);
        pie.setBorder(new EmptyBorder(14, 0, 0, 0));
        formulario.add(pie);

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

        contenedor.add(encabezado, BorderLayout.NORTH);
        contenedor.add(formulario, BorderLayout.CENTER);
        setContentPane(contenedor);
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(Tema.FUENTE_BOLD);
        label.setForeground(Tema.TEXTO_PRINCIPAL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }

    private void estilizarBotonPrimario(JButton boton) {
        boton.setBackground(Tema.AZUL_PRINCIPAL);
        boton.setForeground(Color.WHITE);
        boton.setFont(Tema.FUENTE_BOTON);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void intentarLogin() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            etiquetaError.setText("Ingresa usuario y contraseña.");
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
            etiquetaError.setText("Error de conexión con la base de datos.");
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar a la base de datos:\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
