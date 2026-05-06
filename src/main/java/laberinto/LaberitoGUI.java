package laberinto;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que dibuja el laberinto gráficamente.
 * Calcula dinámicamente el tamaño de celda para que siempre
 * quepa en el espacio disponible.
 */
public class LaberitoGUI extends JPanel {
    private GeneradorLaberinto generador;
    private int tamañoCelda;
    private String textoEstado = ""; // Texto de estado superpuesto

    public LaberitoGUI(GeneradorLaberinto generador) {
        this.generador = generador;
        this.tamañoCelda = calcularTamañoCelda();
    }

    /**
     * Actualiza el generador para reflejar cambios
     */
    public void actualizarGenerador(GeneradorLaberinto nuevoGenerador) {
        this.generador = nuevoGenerador;
        this.tamañoCelda = calcularTamañoCelda();
    }

    /**
     * Establece un texto de estado que se muestra sobre el laberinto
     */
    public void setTextoEstado(String texto) {
        this.textoEstado = texto;
    }

    /**
     * Calcula el tamaño de celda para que el laberinto quepa en la ventana.
     * Mínimo 8px, máximo 40px por celda.
     */
    private int calcularTamañoCelda() {
        int n = generador.getN();
        int m = generador.getM();
        // Intentar 30px por defecto, reducir si no cabe
        int maxAlto = 600;  // espacio disponible estimado
        int maxAncho = 700;
        int porAlto = Math.max(8, maxAlto / n);
        int porAncho = Math.max(8, maxAncho / m);
        return Math.min(40, Math.min(porAlto, porAncho));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Celda[][] cuadricula = generador.getCuadricula();
        int n = generador.getN();
        int m = generador.getM();

        // Recalcular tamaño de celda basado en espacio real disponible
        int celdaH = Math.max(8, (getHeight() - 30) / n); // -30 para margen de texto
        int celdaW = Math.max(8, getWidth() / m);
        tamañoCelda = Math.min(40, Math.min(celdaH, celdaW));

        // Dibujar fondo
        g2d.setColor(new Color(245, 245, 250));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Dibujar celdas con colores según estado
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Celda c = cuadricula[i][j];
                int x = j * tamañoCelda;
                int y = i * tamañoCelda;

                // Colorear celda según estado (prioridad: posición > camino > explorada)
                if (c.posicionActual) {
                    g2d.setColor(new Color(255, 200, 0)); // Amarillo - posición del bot
                    g2d.fillRect(x, y, tamañoCelda, tamañoCelda);
                } else if (c.enCamino) {
                    g2d.setColor(new Color(0, 102, 204)); // Azul oscuro - camino final
                    g2d.fillRect(x, y, tamañoCelda, tamañoCelda);
                } else if (c.explorada) {
                    g2d.setColor(new Color(173, 216, 230)); // Azul claro - explorada
                    g2d.fillRect(x, y, tamañoCelda, tamañoCelda);
                }
            }
        }

        // Dibujar paredes
        g2d.setColor(new Color(40, 40, 60));
        g2d.setStroke(new BasicStroke(Math.max(1, tamañoCelda / 15f)));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Celda c = cuadricula[i][j];
                int x = j * tamañoCelda;
                int y = i * tamañoCelda;

                // Pared superior
                if (i == 0 || (i > 0 && cuadricula[i - 1][j] != null && 
                    c.paredes.contains(cuadricula[i - 1][j]))) {
                    g2d.drawLine(x, y, x + tamañoCelda, y);
                }
                // Pared izquierda
                if (j == 0 || (j > 0 && cuadricula[i][j - 1] != null && 
                    c.paredes.contains(cuadricula[i][j - 1]))) {
                    g2d.drawLine(x, y, x, y + tamañoCelda);
                }
                // Pared derecha
                if (j == m - 1 || (j < m - 1 && cuadricula[i][j + 1] != null && 
                    c.paredes.contains(cuadricula[i][j + 1]))) {
                    g2d.drawLine(x + tamañoCelda, y, x + tamañoCelda, y + tamañoCelda);
                }
                // Pared inferior
                if (i == n - 1 || (i < n - 1 && cuadricula[i + 1][j] != null && 
                    c.paredes.contains(cuadricula[i + 1][j]))) {
                    g2d.drawLine(x, y + tamañoCelda, x + tamañoCelda, y + tamañoCelda);
                }
            }
        }

        // Dibujar entrada (E) en verde
        Celda entrada = generador.getCeldaEntrada();
        if (entrada != null) {
            int x = entrada.col * tamañoCelda;
            int y = entrada.fila * tamañoCelda;
            g2d.setColor(new Color(34, 177, 76)); // Verde
            g2d.fillOval(x + tamañoCelda / 4, y + tamañoCelda / 4, tamañoCelda / 2, tamañoCelda / 2);
            if (tamañoCelda >= 16) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, Math.max(8, tamañoCelda / 3)));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString("E", x + (tamañoCelda - fm.stringWidth("E")) / 2, y + ((tamañoCelda - fm.getHeight()) / 2) + fm.getAscent());
            }
        }

        // Dibujar fin (F) en rojo
        Celda fin = generador.getCeldaFin();
        if (fin != null) {
            int x = fin.col * tamañoCelda;
            int y = fin.fila * tamañoCelda;
            g2d.setColor(new Color(220, 40, 40)); // Rojo
            g2d.fillOval(x + tamañoCelda / 4, y + tamañoCelda / 4, tamañoCelda / 2, tamañoCelda / 2);
            if (tamañoCelda >= 16) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, Math.max(8, tamañoCelda / 3)));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString("F", x + (tamañoCelda - fm.stringWidth("F")) / 2, y + ((tamañoCelda - fm.getHeight()) / 2) + fm.getAscent());
            }
        }

        // Dibujar texto de estado superpuesto
        if (textoEstado != null && !textoEstado.isEmpty()) {
            int textoY = n * tamañoCelda + 18;
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.setColor(new Color(40, 40, 60));
            g2d.drawString(textoEstado, 5, textoY);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int n = generador.getN();
        int m = generador.getM();
        return new Dimension(m * tamañoCelda + 2, n * tamañoCelda + 25);
    }
}
