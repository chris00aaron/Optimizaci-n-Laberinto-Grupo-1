package laberinto;

/**
 * Estados posibles del solver durante la resolución paso a paso
 */
public enum EstadoSolver {
    NO_INICIADO,           // Aún no se ha llamado a iniciar()
    EXPLORANDO,            // BFS en progreso, explorando celdas
    RECONSTRUYENDO_CAMINO, // Se encontró la salida, mostrando camino paso a paso
    COMPLETADO,            // Resolución terminada exitosamente
    SIN_SOLUCION           // No existe camino de entrada a salida
}
