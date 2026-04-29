package laberinto;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que dibuja el laberinto gráficamente
 */
public class LaberitoGUI extends JPanel {
    private final GeneradorLaberinto generador;
    private final int tamañoCelda;

    public LaberitoGUI(GeneradorLaberinto generador) {
        this.generador = generador;
        this.tamañoCelda = 30; // Pixeles por celda
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Celda[][] cuadricula = generador.getCuadricula();
        int n = generador.getN();
        int m = generador.getM();

        // Dibujar fondo
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Dibujar paredes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Celda c = cuadricula[i][j];
                int x = j * tamañoCelda;
                int y = i * tamañoCelda;

                // Dibuja paredes superiores, inferiores, izquierdas, derechas
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
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString("E", x + (tamañoCelda - fm.stringWidth("E")) / 2, y + ((tamañoCelda - fm.getHeight()) / 2) + fm.getAscent());
        }

        // Dibujar fin (F) en rojo
        Celda fin = generador.getCeldaFin();
        if (fin != null) {
            int x = fin.col * tamañoCelda;
            int y = fin.fila * tamañoCelda;
            g2d.setColor(new Color(255, 0, 0)); // Rojo
            g2d.fillOval(x + tamañoCelda / 4, y + tamañoCelda / 4, tamañoCelda / 2, tamañoCelda / 2);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString("F", x + (tamañoCelda - fm.stringWidth("F")) / 2, y + ((tamañoCelda - fm.getHeight()) / 2) + fm.getAscent());
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int n = generador.getN();
        int m = generador.getM();
        return new Dimension(m * tamañoCelda, n * tamañoCelda);
    }
}
