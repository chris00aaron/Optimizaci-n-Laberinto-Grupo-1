package laberinto;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal con GUI interactivo para controlar el laberinto y el solver.
 * Incluye controles de semilla, complejidad, y solver paso-a-paso.
 */
public class VentanaLaberinto extends JFrame {
    private LaberitoGUI panelLaberinto;
    private GeneradorLaberinto generador;
    private JSpinner spinnerN, spinnerM;
    private JSlider sliderComplejidad, sliderVelocidad;
    private JTextField txtSemilla;
    private JButton btnRegenerar, btnPaso, btnAutomatico, btnPausar, btnLimpiar;
    private JLabel lblEstadisticas, lblComplejidad, lblEstadoSolver, lblVelocidad;
    private SolverLaberinto solver;
    private boolean resuelto = false;
    private Timer timerAnimacion;

    public VentanaLaberinto(GeneradorLaberinto generador) {
        this.generador = generador;
        
        setTitle("Laberinto - Editor y Solucionador (Semilla: " + generador.getSemilla() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        // Panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(240, 240, 248));

        // Panel de control (arriba)
        mainPanel.add(crearPanelControl(), BorderLayout.NORTH);

        // Panel del laberinto (centro)
        panelLaberinto = new LaberitoGUI(generador);
        JScrollPane scrollLaberinto = new JScrollPane(panelLaberinto);
        scrollLaberinto.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 200)));
        mainPanel.add(scrollLaberinto, BorderLayout.CENTER);

        // Panel de estadísticas (derecha)
        mainPanel.add(crearPanelEstadisticas(), BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 160)),
            "Controles del Laberinto"));
        panel.setBackground(new Color(248, 248, 255));

        // ── Fila 1: Tamaño + Semilla ──
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila1.setOpaque(false);
        fila1.add(new JLabel("Filas:"));
        spinnerN = new JSpinner(new SpinnerNumberModel(generador.getN(), 5, 50, 1));
        fila1.add(spinnerN);
        fila1.add(new JLabel("Columnas:"));
        spinnerM = new JSpinner(new SpinnerNumberModel(generador.getM(), 5, 50, 1));
        fila1.add(spinnerM);
        fila1.add(Box.createHorizontalStrut(15));
        fila1.add(new JLabel("Semilla:"));
        txtSemilla = new JTextField(String.valueOf(generador.getSemilla()), 14);
        txtSemilla.setToolTipText("Ingrese una semilla numérica para reproducibilidad. Dejar vacío = aleatoria.");
        fila1.add(txtSemilla);
        panel.add(fila1);

        // ── Fila 2: Complejidad ──
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila2.setOpaque(false);
        fila2.add(new JLabel("Complejidad:"));
        sliderComplejidad = new JSlider(0, 100, (int) generador.getComplejidad());
        sliderComplejidad.setMajorTickSpacing(10);
        sliderComplejidad.setMinorTickSpacing(5);
        sliderComplejidad.setPaintTicks(true);
        sliderComplejidad.setPaintLabels(true);
        sliderComplejidad.setPreferredSize(new Dimension(250, 50));
        sliderComplejidad.setOpaque(false);
        sliderComplejidad.addChangeListener(e -> {
            lblComplejidad.setText(sliderComplejidad.getValue() + "%");
        });
        lblComplejidad = new JLabel((int)generador.getComplejidad() + "%");
        lblComplejidad.setFont(new Font("SansSerif", Font.BOLD, 13));
        fila2.add(sliderComplejidad);
        fila2.add(lblComplejidad);
        panel.add(fila2);

        // ── Fila 3: Botones de acción ──
        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        fila3.setOpaque(false);

        btnRegenerar = crearBoton("🔄 Regenerar", new Color(50, 130, 200));
        btnRegenerar.addActionListener(e -> regenerarLaberinto());
        fila3.add(btnRegenerar);

        fila3.add(new JSeparator(SwingConstants.VERTICAL));

        btnPaso = crearBoton("▶ Paso", new Color(50, 160, 80));
        btnPaso.addActionListener(e -> ejecutarUnPaso());
        fila3.add(btnPaso);

        btnAutomatico = crearBoton("▶▶ Automático", new Color(50, 160, 80));
        btnAutomatico.addActionListener(e -> ejecutarAutomatico());
        fila3.add(btnAutomatico);

        btnPausar = crearBoton("⏸ Pausar", new Color(200, 150, 30));
        btnPausar.setEnabled(false);
        btnPausar.addActionListener(e -> pausarAnimacion());
        fila3.add(btnPausar);

        btnLimpiar = crearBoton("🧹 Limpiar", new Color(180, 60, 60));
        btnLimpiar.setEnabled(false);
        btnLimpiar.addActionListener(e -> limpiarSolucion());
        fila3.add(btnLimpiar);

        panel.add(fila3);

        // ── Fila 4: Velocidad + Estado ──
        JPanel fila4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila4.setOpaque(false);
        fila4.add(new JLabel("Velocidad:"));
        sliderVelocidad = new JSlider(10, 500, 80);
        sliderVelocidad.setPreferredSize(new Dimension(150, 30));
        sliderVelocidad.setOpaque(false);
        sliderVelocidad.setToolTipText("Milisegundos entre pasos automáticos (menor = más rápido)");
        sliderVelocidad.addChangeListener(e -> {
            lblVelocidad.setText(sliderVelocidad.getValue() + "ms");
            if (timerAnimacion != null && timerAnimacion.isRunning()) {
                timerAnimacion.setDelay(sliderVelocidad.getValue());
            }
        });
        lblVelocidad = new JLabel("80ms");
        fila4.add(sliderVelocidad);
        fila4.add(lblVelocidad);
        fila4.add(Box.createHorizontalStrut(20));
        
        lblEstadoSolver = new JLabel("Estado: Listo");
        lblEstadoSolver.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstadoSolver.setForeground(new Color(80, 80, 120));
        fila4.add(lblEstadoSolver);
        panel.add(fila4);

        return panel;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 160)),
            "Estadísticas"));
        panel.setPreferredSize(new Dimension(260, 0));
        panel.setBackground(new Color(248, 248, 255));

        lblEstadisticas = new JLabel(generarTextoEstadisticas());
        lblEstadisticas.setVerticalAlignment(JLabel.TOP);
        lblEstadisticas.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lblEstadisticas);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private String generarTextoEstadisticas() {
        StringBuilder sb = new StringBuilder("<html><body style='padding:5px;'>");
        sb.append("<b>Laberinto:</b><br/>");
        sb.append("• Tamaño: ").append(generador.getN()).append(" × ").append(generador.getM()).append("<br/>");
        sb.append("• Celdas: ").append(generador.getN() * generador.getM()).append("<br/>");
        sb.append("• Complejidad: ").append((int)generador.getComplejidad()).append("%<br/>");
        sb.append("• Semilla: <code>").append(generador.getSemilla()).append("</code><br/>");
        sb.append("<br/>");

        sb.append("<b>Solver BFS:</b><br/>");
        if (solver != null && solver.getEstado() != EstadoSolver.NO_INICIADO) {
            EstadoSolver estado = solver.getEstado();
            String estadoTexto;
            String color;
            switch (estado) {
                case EXPLORANDO: estadoTexto = "Explorando..."; color = "#3080C8"; break;
                case RECONSTRUYENDO_CAMINO: estadoTexto = "Trazando camino..."; color = "#0066CC"; break;
                case COMPLETADO: estadoTexto = "✓ Completado"; color = "#228B22"; break;
                case SIN_SOLUCION: estadoTexto = "✗ Sin solución"; color = "#CC0000"; break;
                default: estadoTexto = "No iniciado"; color = "#666666"; break;
            }
            sb.append("• Estado: <span style='color:").append(color).append(";font-weight:bold;'>").append(estadoTexto).append("</span><br/>");
            sb.append("• Pasos BFS: ").append(solver.getPasosTotales()).append("<br/>");
            sb.append("• Exploradas: ").append(solver.getCeldasExploradas()).append("<br/>");
            sb.append("• Camino: ").append(solver.getLongitudCamino()).append(" celdas<br/>");
            if (estado == EstadoSolver.RECONSTRUYENDO_CAMINO) {
                sb.append("• Mostrando: ").append(solver.getIndiceCaminoActual())
                  .append("/").append(solver.getLongitudCamino()).append("<br/>");
            }
            if (estado == EstadoSolver.COMPLETADO) {
                sb.append("• Eficiencia: ").append(String.format("%.1f", solver.getEficiencia())).append("%<br/>");
            }
        } else {
            sb.append("• Estado: No iniciado<br/>");
            sb.append("• Pasos: 0<br/>");
            sb.append("• Exploradas: 0<br/>");
            sb.append("• Camino: 0<br/>");
        }

        sb.append("<br/><b>Leyenda:</b><br/>");
        sb.append("• <span style='color:#22B14C;'>●</span> Entrada (E)<br/>");
        sb.append("• <span style='color:#DC2828;'>●</span> Salida (F)<br/>");
        sb.append("• <span style='color:#FFC800;'>■</span> Bot actual<br/>");
        sb.append("• <span style='color:#ADD8E6;'>■</span> Explorada<br/>");
        sb.append("• <span style='color:#0066CC;'>■</span> Camino final<br/>");

        sb.append("</body></html>");
        return sb.toString();
    }

    // ==========================================
    // ACCIONES
    // ==========================================

    private void regenerarLaberinto() {
        if (timerAnimacion != null && timerAnimacion.isRunning()) {
            timerAnimacion.stop();
        }

        int n = (int) spinnerN.getValue();
        int m = (int) spinnerM.getValue();
        double complejidad = sliderComplejidad.getValue();

        // Parsear semilla
        long semilla;
        String textoSemilla = txtSemilla.getText().trim();
        if (textoSemilla.isEmpty()) {
            semilla = System.nanoTime();
        } else {
            try {
                semilla = Long.parseLong(textoSemilla);
            } catch (NumberFormatException ex) {
                // Si no es número válido, usar hash del texto como semilla
                semilla = textoSemilla.hashCode();
            }
        }

        generador = new GeneradorLaberinto(n, m, 1, 3, 1, 1, complejidad, semilla);
        generador.generar();

        // Actualizar campo de semilla con el valor usado
        txtSemilla.setText(String.valueOf(generador.getSemilla()));
        setTitle("Laberinto - Editor y Solucionador (Semilla: " + generador.getSemilla() + ")");

        panelLaberinto.actualizarGenerador(generador);
        panelLaberinto.setTextoEstado("");
        panelLaberinto.repaint();

        solver = null;
        resuelto = false;
        habilitarBotonesSolver(true);
        btnLimpiar.setEnabled(false);
        btnPausar.setEnabled(false);
        lblEstadoSolver.setText("Estado: Listo");

        actualizarEstadisticas();
    }

    /**
     * Ejecuta UN solo paso del solver (modo manual)
     */
    private void ejecutarUnPaso() {
        if (timerAnimacion != null && timerAnimacion.isRunning()) {
            return; // No paso manual durante automático
        }

        // Iniciar solver si no está iniciado
        if (solver == null || solver.getEstado() == EstadoSolver.NO_INICIADO) {
            iniciarSolver();
            if (solver == null) return;
        }

        EstadoSolver estado = solver.getEstado();

        if (estado == EstadoSolver.EXPLORANDO) {
            solver.siguientePaso();
            actualizarVisualizacion();
        } else if (estado == EstadoSolver.RECONSTRUYENDO_CAMINO) {
            solver.siguientePasoCamino();
            actualizarVisualizacion();
        }

        // Verificar si terminó
        if (solver.getEstado() == EstadoSolver.COMPLETADO) {
            finalizarResolucion();
        } else if (solver.getEstado() == EstadoSolver.SIN_SOLUCION) {
            lblEstadoSolver.setText("Estado: ✗ Sin solución");
            JOptionPane.showMessageDialog(this,
                "No se encontró camino desde la entrada a la salida.",
                "Sin solución", JOptionPane.WARNING_MESSAGE);
            habilitarBotonesSolver(false);
            btnLimpiar.setEnabled(true);
        }
    }

    /**
     * Ejecuta el solver en modo automático con Timer
     */
    private void ejecutarAutomatico() {
        // Iniciar solver si no está iniciado
        if (solver == null || solver.getEstado() == EstadoSolver.NO_INICIADO) {
            iniciarSolver();
            if (solver == null) return;
        }

        if (timerAnimacion != null && timerAnimacion.isRunning()) {
            return;
        }

        habilitarBotonesSolver(false);
        btnPausar.setEnabled(true);
        btnRegenerar.setEnabled(false);

        timerAnimacion = new Timer(sliderVelocidad.getValue(), e -> {
            EstadoSolver estado = solver.getEstado();

            if (estado == EstadoSolver.EXPLORANDO) {
                solver.siguientePaso();
                actualizarVisualizacion();
            } else if (estado == EstadoSolver.RECONSTRUYENDO_CAMINO) {
                solver.siguientePasoCamino();
                actualizarVisualizacion();
            }

            if (solver.getEstado() == EstadoSolver.COMPLETADO) {
                timerAnimacion.stop();
                finalizarResolucion();
            } else if (solver.getEstado() == EstadoSolver.SIN_SOLUCION) {
                timerAnimacion.stop();
                lblEstadoSolver.setText("Estado: ✗ Sin solución");
                JOptionPane.showMessageDialog(VentanaLaberinto.this,
                    "No se encontró camino desde la entrada a la salida.",
                    "Sin solución", JOptionPane.WARNING_MESSAGE);
                habilitarBotonesSolver(false);
                btnLimpiar.setEnabled(true);
                btnRegenerar.setEnabled(true);
            }
        });
        timerAnimacion.start();
    }

    /**
     * Pausa la animación automática
     */
    private void pausarAnimacion() {
        if (timerAnimacion != null && timerAnimacion.isRunning()) {
            timerAnimacion.stop();
            btnPausar.setEnabled(false);
            habilitarBotonesSolver(true);
            btnRegenerar.setEnabled(true);
            lblEstadoSolver.setText("Estado: ⏸ Pausado (Paso " + solver.getPasosTotales() + ")");
        }
    }

    private void iniciarSolver() {
        // Limpiar estado visual anterior
        limpiarEstadoVisualCeldas();

        solver = new SolverLaberinto(generador);
        solver.iniciar();
        
        if (solver.getEstado() == EstadoSolver.SIN_SOLUCION) {
            JOptionPane.showMessageDialog(this,
                "No se puede iniciar: entrada o salida no definida.",
                "Error", JOptionPane.ERROR_MESSAGE);
            solver = null;
            return;
        }

        btnLimpiar.setEnabled(true);
        actualizarVisualizacion();
    }

    private void actualizarVisualizacion() {
        EstadoSolver estado = solver.getEstado();
        String estadoTexto;
        
        switch (estado) {
            case EXPLORANDO:
                estadoTexto = "Explorando... Paso " + solver.getPasosTotales() 
                            + " | Exploradas: " + solver.getCeldasExploradas();
                break;
            case RECONSTRUYENDO_CAMINO:
                estadoTexto = "Trazando camino... " + solver.getIndiceCaminoActual() 
                            + "/" + solver.getLongitudCamino();
                break;
            case COMPLETADO:
                estadoTexto = "✓ Completado — Camino: " + solver.getLongitudCamino() 
                            + " | Eficiencia: " + String.format("%.1f", solver.getEficiencia()) + "%";
                break;
            default:
                estadoTexto = estado.toString();
                break;
        }

        lblEstadoSolver.setText("Estado: " + estadoTexto);
        panelLaberinto.setTextoEstado(estadoTexto);
        panelLaberinto.repaint();
        actualizarEstadisticas();
    }

    private void finalizarResolucion() {
        resuelto = true;
        habilitarBotonesSolver(false);
        btnLimpiar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnRegenerar.setEnabled(true);
        actualizarVisualizacion();
    }

    private void limpiarSolucion() {
        if (timerAnimacion != null && timerAnimacion.isRunning()) {
            timerAnimacion.stop();
        }

        limpiarEstadoVisualCeldas();

        solver = null;
        resuelto = false;
        habilitarBotonesSolver(true);
        btnLimpiar.setEnabled(false);
        btnPausar.setEnabled(false);
        lblEstadoSolver.setText("Estado: Listo");

        panelLaberinto.setTextoEstado("");
        panelLaberinto.repaint();
        actualizarEstadisticas();
    }

    private void limpiarEstadoVisualCeldas() {
        Celda[][] cuadricula = generador.getCuadricula();
        for (Celda[] fila : cuadricula) {
            for (Celda celda : fila) {
                celda.reiniciarSolver();
            }
        }
    }

    private void habilitarBotonesSolver(boolean habilitado) {
        btnPaso.setEnabled(habilitado);
        btnAutomatico.setEnabled(habilitado);
    }

    private void actualizarEstadisticas() {
        lblEstadisticas.setText(generarTextoEstadisticas());
    }
}
