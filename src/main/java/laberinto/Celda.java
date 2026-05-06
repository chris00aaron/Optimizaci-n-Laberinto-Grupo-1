package laberinto;
import java.util.*;

/**
 * Celda con su posición, estado de visita y conjunto de paredes.
 * "paredes" = celdas vecinas con las que aún hay pared en pie.
 */
public class Celda {
    // Una vez asignado no cambia
    public final int fila, col;
    
    // Empieza en false (para generación del laberinto)
    public boolean visitada;
    
    // Set de celdas vecinas con pared aún en pie.
    // LinkedHashSet: mantiene orden de inserción (determinista con la misma semilla).
    public final Set<Celda> paredes = new LinkedHashSet<>();
    
    // Propiedades para el solver (visualización)
    public boolean explorada = false;      // Visitada durante BFS
    public boolean enCamino = false;       // Parte del camino final
    public boolean posicionActual = false; // Posición actual del solver
    
    public Celda(int fila, int col) { 
        this.fila = fila; 
        this.col = col; 
    }
    
    /**
     * Reinicia el estado del solver para una nueva resolución
     */
    public void reiniciarSolver() {
        explorada = false;
        enCamino = false;
        posicionActual = false;
    }
}

