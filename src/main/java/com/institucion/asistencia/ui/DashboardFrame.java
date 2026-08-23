package com.institucion.asistencia.ui;

import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Ventana principal que se muestra después de iniciar sesión. */
public class DashboardFrame extends JFrame {

    private final Profesor profesor;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);

    private static final String TARJETA_ASISTENCIA = "asistencia";
    private static final String TARJETA_ESTUDIANTES = "estudiantes";
    private static final String TARJETA_REPORTES = "reportes";

    public DashboardFrame(Profesor profesor) {
        super("Sistema de Asistencia - " + profesor.getNombreCompleto());
        this.profesor = profesor;
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());

        raiz.add(construirBarraLateral(), BorderLayout.WEST);

        panelContenido.setBackground(Tema.FONDO);
        panelContenido.add(new PanelTomarAsistencia(profesor), TARJETA_ASISTENCIA);
        panelContenido.add(new PanelEstudiantes(profesor), TARJETA_ESTUDIANTES);
        panelContenido.add(new PanelReportes(profesor), TARJETA_REPORTES);

        raiz.add(panelContenido, BorderLayout.CENTER);
        setContentPane(raiz);
        cardLayout.show(panelContenido, TARJETA_ASISTENCIA);
    }

    private JPanel construirBarraLateral() {
        JPanel barra = new JPanel();
        barra.setBackground(Tema.AZUL_PRINCIPAL);
        barra.setPreferredSize(new Dimension(220, 0));
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.setBorder(new EmptyBorder(24, 0, 24, 0));

        JLabel titulo = new JLabel("  Asistencia");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setBorder(new EmptyBorder(0, 16, 4, 0));

        JLabel nombreProfesor = new JLabel("  " + profesor.getNombreCompleto());
        nombreProfesor.setFont(Tema.FUENTE_SUBTITULO);
        nombreProfesor.setForeground(new Color(210, 224, 240));
        nombreProfesor.setAlignmentX(Component.LEFT_ALIGNMENT);
        nombreProfesor.setBorder(new EmptyBorder(0, 16, 24, 0));

        barra.add(titulo);
        barra.add(nombreProfesor);
        barra.add(crearBotonMenu("📅  Tomar asistencia", TARJETA_ASISTENCIA));
        barra.add(crearBotonMenu("👥  Estudiantes", TARJETA_ESTUDIANTES));
        barra.add(crearBotonMenu("📊  Reportes", TARJETA_REPORTES));
        barra.add(Box.createVerticalGlue());
        barra.add(crearBotonCerrarSesion());

        return barra;
    }

    private JButton crearBotonMenu(String texto, String tarjeta) {
        JButton boton = new JButton(texto);
        boton.setFont(Tema.FUENTE_BOLD);
        boton.setForeground(Color.WHITE);
        boton.setBackground(Tema.AZUL_PRINCIPAL);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        boton.setBorder(new EmptyBorder(10, 16, 10, 10));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> cardLayout.show(panelContenido, tarjeta));
        boton.getModel().addChangeListener(e -> {
            if (boton.getModel().isRollover()) {
                boton.setBackground(Tema.AZUL_CLARO);
            } else {
                boton.setBackground(Tema.AZUL_PRINCIPAL);
            }
        });
        return boton;
    }

    private JButton crearBotonCerrarSesion() {
        JButton boton = new JButton("⎋  Cerrar sesión");
        boton.setFont(Tema.FUENTE_NORMAL);
        boton.setForeground(new Color(230, 230, 230));
        boton.setBackground(Tema.AZUL_PRINCIPAL);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        boton.setBorder(new EmptyBorder(8, 16, 8, 10));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        return boton;
    }
}
