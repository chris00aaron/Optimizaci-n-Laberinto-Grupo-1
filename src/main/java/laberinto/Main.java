package laberinto;

public class Main {
    public static void main(String[] args) {
        // Crear laberinto con parámetros: N, M, minSol, maxSol, entradas, salidas, complejidad
        // Complejidad al 50% para mayor dificultad por defecto
        GeneradorLaberinto lab = new GeneradorLaberinto(15, 20, 1, 3, 1, 1, 50);
        lab.generar();
        
        // Visualización en consola
        System.out.println("=== Laberinto en Consola ===");
        System.out.println("Semilla: " + lab.getSemilla());
        lab.imprimir();
        
        // Visualización gráfica e interactiva
        System.out.println("\n=== Abriendo ventana interactiva... ===");
        new VentanaLaberinto(lab);
    }
}
