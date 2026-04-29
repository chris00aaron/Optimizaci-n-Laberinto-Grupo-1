package laberinto;
import java.util.*;

//Celda con su posición, estado de visita y conjunto de paredes.
//"paredes" = celdas vecinas con las que aún hay pared en pie.

public class Celda {
            //Una vez asignado no cambia
        final int fila, col;
        //Empieza en false
        boolean visitada;
        //Set de celdas vecinas con pared aun en pie
        final Set<Celda> paredes = new HashSet<>();
        Celda(int fila, int col) { 
            this.fila = fila; this.col = col; 
        }
}
