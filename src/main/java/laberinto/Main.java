package laberinto;

public class Main {
    public static void main(String[] args) {
        GeneradorLaberinto lab = new GeneradorLaberinto(10, 15, 1, 3, 1, 1);
        lab.generar();
        
        // Visualización en consola
        System.out.println("=== Laberinto en Consola ===");
        lab.imprimir();
        
        // Visualización gráfica
        System.out.println("\n=== Abriendo ventana gráfica... ===");
        new VentanaLaberinto(lab);
    }
}