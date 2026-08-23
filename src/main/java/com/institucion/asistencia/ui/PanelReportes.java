package com.institucion.asistencia.ui;

import com.institucion.asistencia.dao.AsistenciaDAO;
import com.institucion.asistencia.dao.CursoDAO;
import com.institucion.asistencia.model.Asistencia;
import com.institucion.asistencia.model.Curso;
import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelReportes extends JPanel {

    private final Profesor profesor;
    private final CursoDAO cursoDAO = new CursoDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    private final JComboBox<Curso> comboCurso = new JComboBox<>();
    private final JSpinner selectorDesde;
    private final JSpinner selectorHasta;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Fecha", "Estudiante", "Estado", "Observación"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel etiquetaResumen = new JLabel(" ");

    public PanelReportes(Profesor profesor) {
        this.profesor = profesor;

        LocalDate hoy = LocalDate.now();
        selectorDesde = new JSpinner(new SpinnerDateModel());
        selectorDesde.setEditor(new JSpinner.DateEditor(selectorDesde, "dd/MM/yyyy"));
        selectorDesde.setValue(convertir(hoy.minusMonths(1)));

        selectorHasta = new JSpinner(new SpinnerDateModel());
        selectorHasta.setEditor(new JSpinner.DateEditor(selectorHasta, "dd/MM/yyyy"));
        selectorHasta.setValue(convertir(hoy));

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirPiePagina(), BorderLayout.SOUTH);

        cargarCursos();
    }

    private Date convertir(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate convertir(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.FONDO);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titulo = new JLabel("Reportes de asistencia");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filtros.setBackground(Tema.FONDO);
        filtros.add(etiquetaBold("Curso:"));
        filtros.add(comboCurso);
        filtros.add(etiquetaBold("Desde:"));
        filtros.add(selectorDesde);
        filtros.add(etiquetaBold("Hasta:"));
        filtros.add(selectorHasta);

        JButton botonConsultar = new JButton("Consultar");
        botonConsultar.setBackground(Tema.AZUL_PRINCIPAL);
        botonConsultar.setForeground(Color.WHITE);
        botonConsultar.setFont(Tema.FUENTE_BOTON);
        botonConsultar.setFocusPainted(false);
        botonConsultar.setBorderPainted(false);
        botonConsultar.setOpaque(true);
        botonConsultar.addActionListener(e -> consultar());
        filtros.add(botonConsultar);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(filtros, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel etiquetaBold(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FUENTE_BOLD);
        return l;
    }

    private JScrollPane construirTabla() {
        tabla.setRowHeight(30);
        tabla.setFont(Tema.FUENTE_NORMAL);
        tabla.getTableHeader().setFont(Tema.FUENTE_BOLD);
        tabla.getTableHeader().setBackground(Tema.AZUL_PRINCIPAL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        return scroll;
    }

    private JPanel construirPiePagina() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.FONDO);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));
        etiquetaResumen.setFont(Tema.FUENTE_SUBTITULO);
        etiquetaResumen.setForeground(Tema.TEXTO_SECUNDARIO);
        panel.add(etiquetaResumen, BorderLayout.WEST);
        return panel;
    }

    private void cargarCursos() {
        try {
            comboCurso.removeAllItems();
            List<Curso> cursos = cursoDAO.listarPorProfesor(profesor.getIdProfesor());
            for (Curso c : cursos) {
                comboCurso.addItem(c);
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar los cursos", ex);
        }
    }

    private void consultar() {
        Curso curso = (Curso) comboCurso.getSelectedItem();
        if (curso == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un curso.");
            return;
        }
        LocalDate desde = convertir((Date) selectorDesde.getValue());
        LocalDate hasta = convertir((Date) selectorHasta.getValue());

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a 'Hasta'.");
            return;
        }

        modeloTabla.setRowCount(0);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            List<Asistencia> registros = asistenciaDAO.reportePorCurso(curso.getIdCurso(), desde, hasta);
            Map<Asistencia.Estado, Integer> conteo = new HashMap<>();
            for (Asistencia.Estado e : Asistencia.Estado.values()) {
                conteo.put(e, 0);
            }

            for (Asistencia a : registros) {
                modeloTabla.addRow(new Object[]{
                        a.getFecha().format(formato),
                        a.getNombreEstudiante(),
                        a.getEstado(),
                        a.getObservacion() == null ? "" : a.getObservacion()
                });
                conteo.put(a.getEstado(), conteo.get(a.getEstado()) + 1);
            }

            etiquetaResumen.setText(String.format(
                    "Total: %d registros — Presentes: %d | Ausentes: %d | Tarde: %d | Excusas: %d",
                    registros.size(),
                    conteo.get(Asistencia.Estado.PRESENTE),
                    conteo.get(Asistencia.Estado.AUSENTE),
                    conteo.get(Asistencia.Estado.TARDE),
                    conteo.get(Asistencia.Estado.EXCUSA)));
        } catch (SQLException ex) {
            mostrarError("No se pudo generar el reporte", ex);
        }
    }

    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
