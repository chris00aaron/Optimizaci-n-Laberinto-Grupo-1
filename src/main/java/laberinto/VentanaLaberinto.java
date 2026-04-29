package laberinto;

import javax.swing.*;

/**
 * Ventana principal que muestra el laberinto gráficamente
 */
public class VentanaLaberinto extends JFrame {
    public VentanaLaberinto(GeneradorLaberinto generador) {
        setTitle("Laberinto - Visualización Gráfica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        LaberitoGUI panel = new LaberitoGUI(generador);
        add(panel);
        
        pack();
        setLocationRelativeTo(null); // Centrar ventana
        setVisible(true);
    }
}
