package laberinto;

import java.util.*;

/**
 * Solucionador de laberinto usando BFS (Breadth-First Search)
 * Encuentra el camino más corto desde la entrada hasta la salida.
 * 
 * Soporta dos modos de operación:
 * - Completo: resolver() ejecuta todo de golpe
 * - Paso a paso: iniciar() + siguientePaso() + siguientePasoCamino()
 */
public class SolverLaberinto {
    private final GeneradorLaberinto generador;
    private final Set<Celda> exploradas;
    private final Map<Celda, Celda> padre;
    private List<Celda> caminoFinal;
    private int pasoActual;

    // Estado para modo paso a paso
    private EstadoSolver estado;
    private Queue<Celda> cola;
    private Celda celdaActualBot;       // Posición actual del bot (para visualización)
    private int indiceCaminoActual;     // Índice en caminoFinal durante reconstrucción visual

    public SolverLaberinto(GeneradorLaberinto generador) {
        this.generador = generador;
        this.exploradas = new LinkedHashSet<>();
        this.padre = new HashMap<>();
        this.caminoFinal = new ArrayList<>();
        this.pasoActual = 0;
        this.estado = EstadoSolver.NO_INICIADO;
        this.indiceCaminoActual = 0;
    }

    // ==========================================
    // MODO COMPLETO (compatibilidad)
    // ==========================================

    /**
     * Resuelve el laberinto usando BFS de forma completa
     * @return true si encuentra camino, false si no hay solución
     */
    public boolean resolver() {
        iniciar();
        
        while (estado == EstadoSolver.EXPLORANDO) {
            siguientePaso();
        }

        if (estado == EstadoSolver.RECONSTRUYENDO_CAMINO) {
            // Marcar todo el camino de una vez
            while (estado == EstadoSolver.RECONSTRUYENDO_CAMINO) {
                siguientePasoCamino();
            }
            return true;
        }
        
        return false;
    }

    // ==========================================
    // MODO PASO A PASO
    // ==========================================

    /**
     * Inicializa el BFS sin ejecutar ningún paso.
     * Después de llamar a este método, usar siguientePaso() para avanzar.
     */
    public void iniciar() {
        Celda entrada = generador.getCeldaEntrada();
        Celda salida = generador.getCeldaFin();

        if (entrada == null || salida == null) {
            estado = EstadoSolver.SIN_SOLUCION;
            return;
        }

        exploradas.clear();
        padre.clear();
        caminoFinal.clear();
        pasoActual = 0;
        indiceCaminoActual = 0;

        cola = new LinkedList<>();
        cola.add(entrada);
        exploradas.add(entrada);
        padre.put(entrada, null);
        
        celdaActualBot = entrada;
        entrada.explorada = true;
        entrada.posicionActual = true;

        estado = EstadoSolver.EXPLORANDO;
    }

    /**
     * Ejecuta UN solo paso de BFS: saca una celda de la cola y explora sus vecinos.
     * 
     * @return el estado actual después de este paso
     */
    public EstadoSolver siguientePaso() {
        if (estado != EstadoSolver.EXPLORANDO) {
            return estado;
        }

        if (cola == null || cola.isEmpty()) {
            estado = EstadoSolver.SIN_SOLUCION;
            return estado;
        }

        // Limpiar posición anterior del bot
        if (celdaActualBot != null) {
            celdaActualBot.posicionActual = false;
        }

        Celda actual = cola.poll();
        pasoActual++;
        celdaActualBot = actual;
        actual.posicionActual = true;
        actual.explorada = true;

        // ¿Encontramos la salida?
        if (actual == generador.getCeldaFin()) {
            reconstruirCamino(actual);
            actual.posicionActual = false;
            estado = EstadoSolver.RECONSTRUYENDO_CAMINO;
            indiceCaminoActual = 0;
            return estado;
        }

        // Explorar vecinos (celdas sin pared entre ellas)
        for (Celda vecina : obtenerVecinos(actual)) {
            if (!exploradas.contains(vecina)) {
                exploradas.add(vecina);
                padre.put(vecina, actual);
                cola.add(vecina);
            }
        }

        // ¿Cola vacía después de explorar? No hay solución
        if (cola.isEmpty()) {
            estado = EstadoSolver.SIN_SOLUCION;
        }

        return estado;
    }

    /**
     * Una vez encontrada la salida (estado = RECONSTRUYENDO_CAMINO),
     * avanza una celda del camino final para visualización.
     * 
     * @return el estado actual después de este paso
     */
    public EstadoSolver siguientePasoCamino() {
        if (estado != EstadoSolver.RECONSTRUYENDO_CAMINO) {
            return estado;
        }

        if (indiceCaminoActual >= caminoFinal.size()) {
            estado = EstadoSolver.COMPLETADO;
            return estado;
        }

        // Limpiar posición anterior del bot
        if (celdaActualBot != null) {
            celdaActualBot.posicionActual = false;
        }

        Celda celda = caminoFinal.get(indiceCaminoActual);
        celda.enCamino = true;
        celda.posicionActual = true;
        celdaActualBot = celda;
        indiceCaminoActual++;

        // ¿Terminamos?
        if (indiceCaminoActual >= caminoFinal.size()) {
            celda.posicionActual = false;
            estado = EstadoSolver.COMPLETADO;
        }

        return estado;
    }

    /**
     * Obtiene los vecinos accesibles de una celda (sin pared entre ellas)
     */
    private List<Celda> obtenerVecinos(Celda c) {
        List<Celda> vecinos = new ArrayList<>();
        Celda[][] cuadricula = generador.getCuadricula();
        int n = generador.getN();
        int m = generador.getM();

        // Arriba
        if (c.fila > 0) {
            Celda vecina = cuadricula[c.fila - 1][c.col];
            if (!c.paredes.contains(vecina)) {
                vecinos.add(vecina);
            }
        }
        // Abajo
        if (c.fila < n - 1) {
            Celda vecina = cuadricula[c.fila + 1][c.col];
            if (!c.paredes.contains(vecina)) {
                vecinos.add(vecina);
            }
        }
        // Izquierda
        if (c.col > 0) {
            Celda vecina = cuadricula[c.fila][c.col - 1];
            if (!c.paredes.contains(vecina)) {
                vecinos.add(vecina);
            }
        }
        // Derecha
        if (c.col < m - 1) {
            Celda vecina = cuadricula[c.fila][c.col + 1];
            if (!c.paredes.contains(vecina)) {
                vecinos.add(vecina);
            }
        }

        return vecinos;
    }

    /**
     * Reconstruye el camino desde salida hasta entrada usando el mapa de padres
     */
    private void reconstruirCamino(Celda salida) {
        caminoFinal.clear();
        Celda actual = salida;
        
        while (actual != null) {
            caminoFinal.add(0, actual); // Agregar al inicio para obtener orden entrada->salida
            actual = padre.get(actual);
        }
    }

    // ==========================================
    // GETTERS
    // ==========================================

    /** Obtiene el estado actual del solver */
    public EstadoSolver getEstado() { return estado; }

    /** Obtiene la celda donde está actualmente el bot */
    public Celda getCeldaActualBot() { return celdaActualBot; }

    /** Obtiene el camino encontrado (solo válido después de resolver()) */
    public List<Celda> getCaminoFinal() { return new ArrayList<>(caminoFinal); }

    /** Obtiene las celdas exploradas durante la búsqueda */
    public Set<Celda> getExploradas() { return new LinkedHashSet<>(exploradas); }

    /** Obtiene el número de pasos necesarios para resolver */
    public int getPasosTotales() { return pasoActual; }

    /** Obtiene la longitud del camino más corto */
    public int getLongitudCamino() { return caminoFinal.size(); }

    /** Índice actual en la reconstrucción del camino */
    public int getIndiceCaminoActual() { return indiceCaminoActual; }

    /** Total de celdas exploradas hasta ahora */
    public int getCeldasExploradas() { return exploradas.size(); }

    /**
     * Calcula la eficiencia: (longitud camino / celdas exploradas) * 100
     */
    public double getEficiencia() {
        if (exploradas.isEmpty()) return 0;
        return (double) caminoFinal.size() / exploradas.size() * 100;
    }
}
