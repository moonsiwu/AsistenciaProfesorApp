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

/**
 * Panel institucional para la consulta y análisis de reportes de asistencia,
 * con estructura centrada y armónica.
 */
public class PanelReportes extends JPanel {

    private final Profesor profesor;
    private final CursoDAO cursoDAO = new CursoDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    private final JComboBox<Curso> comboCurso = new JComboBox<>();
    private final JSpinner selectorDesde;
    private final JSpinner selectorHasta;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Fecha", "Estudiante", "Estado", "Observaciones"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Asistencia.Estado.class;
            }
            return String.class;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel etiquetaResumen = new JLabel("Seleccione un curso y haga clic en 'Consultar Reporte'", SwingConstants.CENTER);

    // Tarjetas métricas
    private JPanel cardTotal;
    private JPanel cardPresentes;
    private JPanel cardAusentes;
    private JPanel cardTardesExcusas;

    public PanelReportes(Profesor profesor) {
        this.profesor = profesor;

        LocalDate hoy = LocalDate.now();
        selectorDesde = new JSpinner(new SpinnerDateModel());
        selectorDesde.setEditor(new JSpinner.DateEditor(selectorDesde, "dd/MM/yyyy"));
        selectorDesde.setValue(convertir(hoy.minusMonths(1)));
        selectorDesde.setFont(Tema.FUENTE_NORMAL);

        selectorHasta = new JSpinner(new SpinnerDateModel());
        selectorHasta.setEditor(new JSpinner.DateEditor(selectorHasta, "dd/MM/yyyy"));
        selectorHasta.setValue(convertir(hoy));
        selectorHasta.setFont(Tema.FUENTE_NORMAL);

        setLayout(new BorderLayout(0, 14));
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirContenidoCentral(), BorderLayout.CENTER);
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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Título y subtítulo centrados
        JLabel titulo = new JLabel("Reportes y Consultas de Asistencia", SwingConstants.CENTER);
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Consulte el historial de asistencia filtrando por curso y rango de fechas", SwingConstants.CENTER);
        subtitulo.setFont(Tema.FUENTE_SUBTITULO);
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitulo);
        panel.add(Box.createVerticalStrut(12));

        // Tarjeta de filtros centrada
        JPanel tarjetaFiltros = Tema.crearTarjeta();
        tarjetaFiltros.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 4));

        JLabel lblCurso = new JLabel("Curso:");
        lblCurso.setFont(Tema.FUENTE_BOLD);
        lblCurso.setForeground(Tema.TEXTO_PRINCIPAL);

        comboCurso.setFont(Tema.FUENTE_NORMAL);
        comboCurso.setPreferredSize(new Dimension(170, 36));

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setFont(Tema.FUENTE_BOLD);
        lblDesde.setForeground(Tema.TEXTO_PRINCIPAL);

        selectorDesde.setPreferredSize(new Dimension(130, 36));

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setFont(Tema.FUENTE_BOLD);
        lblHasta.setForeground(Tema.TEXTO_PRINCIPAL);

        selectorHasta.setPreferredSize(new Dimension(130, 36));

        JButton botonConsultar = Tema.crearBotonPrimario("Consultar Reporte");
        botonConsultar.addActionListener(e -> consultar());

        tarjetaFiltros.add(lblCurso);
        tarjetaFiltros.add(comboCurso);
        tarjetaFiltros.add(Box.createHorizontalStrut(8));
        tarjetaFiltros.add(lblDesde);
        tarjetaFiltros.add(selectorDesde);
        tarjetaFiltros.add(Box.createHorizontalStrut(8));
        tarjetaFiltros.add(lblHasta);
        tarjetaFiltros.add(selectorHasta);
        tarjetaFiltros.add(Box.createHorizontalStrut(12));
        tarjetaFiltros.add(botonConsultar);

        panel.add(tarjetaFiltros);
        panel.add(Box.createVerticalStrut(10));

        // Fila de Métricas del Reporte centradas
        JPanel panelMetricas = new JPanel(new GridLayout(1, 4, 12, 0));
        panelMetricas.setOpaque(false);

        cardTotal = Tema.crearTarjetaMetrica("Total Registros", "0", Tema.AZUL_PRINCIPAL);
        cardPresentes = Tema.crearTarjetaMetrica("Total Presentes", "0", Tema.VERDE_PRESENTE);
        cardAusentes = Tema.crearTarjetaMetrica("Total Ausentes", "0", Tema.ROJO_AUSENTE);
        cardTardesExcusas = Tema.crearTarjetaMetrica("Tardes / Excusas", "0", Tema.NARANJA_TARDE);

        panelMetricas.add(cardTotal);
        panelMetricas.add(cardPresentes);
        panelMetricas.add(cardAusentes);
        panelMetricas.add(cardTardesExcusas);

        panel.add(panelMetricas);
        return panel;
    }

    private JPanel construirContenidoCentral() {
        JPanel contenedorTarjeta = Tema.crearTarjeta();
        contenedorTarjeta.setLayout(new BorderLayout());

        Tema.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        contenedorTarjeta.add(scroll, BorderLayout.CENTER);
        return contenedorTarjeta;
    }

    private JPanel construirPiePagina() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(4, 0, 0, 0));

        etiquetaResumen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiquetaResumen.setForeground(Tema.TEXTO_SECUNDARIO);
        etiquetaResumen.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(etiquetaResumen, BorderLayout.CENTER);
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
            JOptionPane.showMessageDialog(this, "Seleccione un curso antes de generar el reporte.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate desde = convertir((Date) selectorDesde.getValue());
        LocalDate hasta = convertir((Date) selectorHasta.getValue());

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.",
                    "Rango de Fechas Inválido", JOptionPane.WARNING_MESSAGE);
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

            int presentes = conteo.get(Asistencia.Estado.PRESENTE);
            int ausentes = conteo.get(Asistencia.Estado.AUSENTE);
            int otros = conteo.get(Asistencia.Estado.TARDE) + conteo.get(Asistencia.Estado.EXCUSA);

            actualizarValorCard(cardTotal, String.valueOf(registros.size()));
            actualizarValorCard(cardPresentes, String.valueOf(presentes));
            actualizarValorCard(cardAusentes, String.valueOf(ausentes));
            actualizarValorCard(cardTardesExcusas, String.valueOf(otros));

            double porcentaje = registros.isEmpty() ? 0 : ((double) presentes / registros.size()) * 100.0;

            etiquetaResumen.setText(String.format(
                    "Total registros consultados: %d  |  Asistencia promedio: %.1f%%  |  Curso: %s",
                    registros.size(), porcentaje, curso.getNombreCurso()));

        } catch (SQLException ex) {
            mostrarError("No se pudo generar el reporte", ex);
        }
    }

    private void actualizarValorCard(JPanel card, String valor) {
        if (card == null) return;
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "valor".equals(c.getName())) {
                ((JLabel) c).setText(valor);
            }
        }
    }

    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
