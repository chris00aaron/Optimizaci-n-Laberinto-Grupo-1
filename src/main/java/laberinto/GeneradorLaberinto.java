package laberinto;
import java.util.*;

public class GeneradorLaberinto {
    //Definir parámetros: N - M - N° de soluciones (min y max) - N° de entradas - N° de salidas
    private final int N;
    private final int M;
    private final int minSoluciones;
    private final int maxSoluciones;
    private final int numEntradas;
    private final int numSalidas;
    private final double complejidad; // Porcentaje de caminos adicionales (0-100)
    private final long semilla;       // Semilla para trazabilidad

    private Celda[][] cuadricula;
    private final Random random;
    private Celda celdaEntrada;
    //Verificar celdaFin
    private Celda celdaFin;

    // Constructor original para compatibilidad
    public GeneradorLaberinto(int n, int m, int minSol, int maxSol, int entradas, int salidas) {
        this(n, m, minSol, maxSol, entradas, salidas, 0);
    }

    // Constructor con parámetro de complejidad (semilla auto-generada)
    public GeneradorLaberinto(int n, int m, int minSol, int maxSol, int entradas, int salidas, double complejidad) {
        this(n, m, minSol, maxSol, entradas, salidas, complejidad, System.nanoTime());
    }

    // Constructor completo con semilla para trazabilidad
    public GeneradorLaberinto(int n, int m, int minSol, int maxSol, int entradas, int salidas, double complejidad, long semilla) {
        this.N = n;
        this.M = m;
        this.minSoluciones = minSol;
        this.maxSoluciones = maxSol;
        this.numEntradas = entradas;
        this.numSalidas = salidas;
        this.complejidad = Math.max(0, Math.min(100, complejidad)); // Limitar entre 0 y 100
        this.semilla = semilla;
        this.random = new Random(semilla); // Semilla para reproducibilidad
    }

    public void generar() {
        System.out.println("[Semilla usada: " + semilla + "]");

        //"Crear cuadrícula o celdas NxM"
        cuadricula = new Celda[N][M];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < M; j++)
                cuadricula[i][j] = new Celda(i, j);
                //"Marcar todas las celdas como no visitadas" (visitada=false por defecto)

        //"Delimitar cada celda con paredes" -> cada celda comparte pared con cada celda adyacente
        for (int i = 0; i < N; i++)
            for (int j = 0; j < M; j++)
                for (Celda v : adyacentes(cuadricula[i][j]))
                    cuadricula[i][j].paredes.add(v);

        //"Asignarle un número de celda aleatorio del borde para la entrada y la marcamos como visitada"
        celdaEntrada = elegirCeldaBordeAleatoria();
        celdaEntrada.visitada = true;

        //"Posicionarse en la celda marcada como entrada"
        Celda actual = celdaEntrada;
        Deque<Celda> pila = new ArrayDeque<>();
        pila.push(celdaEntrada);

        //Para "Marcar celda como fin a la que tenga la pila más larga"
        Celda celdaPilaMasLarga = celdaEntrada;
        int profundidadMaxima = pila.size();

        while (true) {
            //"¿Pila vacía?"
            if (pila.isEmpty()) {
                //SI -> "Marcar celda como fin a la que tenga la pila más larga"
                celdaFin = celdaPilaMasLarga;
                //"FIN"
                break;
            }

            //NO -> "¿Vecinos no visitados?"
            List<Celda> vecinos = vecinosNoVisitados(actual);

            if (vecinos.isEmpty()) {
                //NO:
                //"Sacar la coordenada de la pila"
                Celda sacada = pila.pop();
                //"Mover puntero a esa celda"
                actual = sacada;

            } else {
                //SI:
                //"Elegir celda vecina al azar"
                Celda siguiente = vecinos.get(random.nextInt(vecinos.size()));

                //"Puntero se mueve a celda elegida"
                Celda anterior = actual;
                actual = siguiente;

                //"Celda anterior se introduce a pila y se marca como visitada"
                pila.push(anterior);
                actual.visitada = true; //(la anterior ya estaba visitada; se marca la nueva)

                //"Eliminar la pared que está entre las dos celdas"
                anterior.paredes.remove(actual);
                actual.paredes.remove(anterior);

                //Registro auxiliar de la pila más profunda alcanzada
                if (pila.size() > profundidadMaxima) {
                    profundidadMaxima = pila.size();
                    celdaPilaMasLarga = actual;
                }
            }
        }

        // Agregar técnicas de complejidad según el nivel configurado
        if (complejidad > 0) {
            agregarCaminosAdicionales();   // Braid Maze (existente)
            agregarCaminosSeñuelo();       // Caminos largos que no llevan a nada
            crearZonasConfusion();         // Plazas abiertas desorientadoras
            alargarCaminoPrincipal();      // Forzar desvíos en la ruta directa
        }
    }

    // ==========================================
    // TÉCNICA 1: Braid Maze (existente, mejorada)
    // ==========================================
    /**
     * Agrega caminos adicionales (elimina paredes) para aumentar la complejidad
     * Según el parámetro complejidad (0-100), elimina ese porcentaje de paredes disponibles
     */
    private void agregarCaminosAdicionales() {
        // Para evitar que el laberinto se convierta en un espacio vacío y demasiado fácil,
        // la mejor técnica para aumentar la dificultad es convertir "callejones sin salida" 
        // en caminos continuos (bucles). Un laberinto con muchos bucles y sin caminos 
        // sin salida se llama "Braid Maze" y es muy confuso de resolver.
        
        List<Celda> caminosSinSalida = new ArrayList<>();
        
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                Celda c = cuadricula[i][j];
                // Omitir entrada y salida
                if (c == celdaEntrada || c == celdaFin) continue;
                
                int totalAdyacentes = adyacentes(c).size();
                int paredesCerradas = c.paredes.size();
                int pasosAbiertos = totalAdyacentes - paredesCerradas;
                
                // Si tiene exactamente 1 paso abierto, es un callejón sin salida
                if (pasosAbiertos == 1 && paredesCerradas > 0) {
                    caminosSinSalida.add(c);
                }
            }
        }

        // Calcular cuántos callejones vamos a conectar (100% = todos los callejones desaparecen)
        int callejonesAConectar = (int) (caminosSinSalida.size() * (complejidad / 100.0));
        
        // Ordenar por (fila, col) antes de mezclar → misma semilla = mismo resultado
        caminosSinSalida.sort(Comparator.comparingInt((Celda c) -> c.fila).thenComparingInt(c -> c.col));
        Collections.shuffle(caminosSinSalida, random);

        int cantidadConectados = 0;
        for (Celda callejon : caminosSinSalida) {
            if (cantidadConectados >= callejonesAConectar) break;
            
            // Verificamos si ya dejó de ser callejón por estar al lado de otro que acabamos de abrir
            int pasosAbiertosActual = adyacentes(callejon).size() - callejon.paredes.size();
            if (pasosAbiertosActual > 1 || callejon.paredes.isEmpty()) {
                continue;
            }

            // Elegir una pared sólida de este callejón al azar y abrirla
            // Ordenar por (fila,col) para que la selección aleatoria sea determinista con la semilla
            List<Celda> paredesDisponibles = new ArrayList<>(callejon.paredes);
            paredesDisponibles.sort(Comparator.comparingInt((Celda c) -> c.fila).thenComparingInt(c -> c.col));
            if (!paredesDisponibles.isEmpty()) {
                Celda paredRomper = paredesDisponibles.get(random.nextInt(paredesDisponibles.size()));
                callejon.paredes.remove(paredRomper);
                paredRomper.paredes.remove(callejon);
                cantidadConectados++;
            }
        }
    }

    // ==========================================
    // TÉCNICA 2: Caminos Señuelo (Decoy Paths)
    // ==========================================
    /**
     * Identifica callejones sin salida largos y los extiende rompiendo paredes
     * hacia zonas alejadas de la salida, creando caminos tentadores pero inútiles.
     */
    private void agregarCaminosSeñuelo() {
        // Intensidad proporcional a complejidad (0-100)
        int cantidadSeñuelos = (int) ((N * M) * (complejidad / 100.0) * 0.05);
        if (cantidadSeñuelos < 1) return;

        // Buscar celdas con exactamente 2 pasos abiertos (corredores) que estén
        // lejos de la salida — buenos candidatos para crear bifurcaciones falsas
        List<Celda> candidatos = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                Celda c = cuadricula[i][j];
                if (c == celdaEntrada || c == celdaFin) continue;

                int pasosAbiertos = adyacentes(c).size() - c.paredes.size();
                // Corredores con paredes disponibles para romper
                if (pasosAbiertos == 2 && !c.paredes.isEmpty()) {
                    // Preferir celdas alejadas de la salida
                    int distSalida = Math.abs(c.fila - celdaFin.fila) + Math.abs(c.col - celdaFin.col);
                    if (distSalida > (N + M) / 4) {
                        candidatos.add(c);
                    }
                }
            }
        }

        // Ordenar por (fila, col) antes de mezclar → misma semilla = mismo resultado
        candidatos.sort(Comparator.comparingInt((Celda c) -> c.fila).thenComparingInt(c -> c.col));
        Collections.shuffle(candidatos, random);

        int creados = 0;
        for (Celda c : candidatos) {
            if (creados >= cantidadSeñuelos) break;

            List<Celda> paredesDisp = new ArrayList<>(c.paredes);
            paredesDisp.sort(Comparator.comparingInt((Celda p) -> p.fila).thenComparingInt(p -> p.col));
            if (!paredesDisp.isEmpty()) {
                // Romper una pared para crear una bifurcación engañosa
                Celda vecina = paredesDisp.get(random.nextInt(paredesDisp.size()));
                c.paredes.remove(vecina);
                vecina.paredes.remove(c);
                creados++;
            }
        }
    }

    // ==========================================
    // TÉCNICA 3: Zonas de Confusión
    // ==========================================
    /**
     * Selecciona regiones rectangulares aleatorias y elimina todas las paredes internas,
     * creando "plazas abiertas" donde el solver/humano pierde el sentido de orientación.
     */
    private void crearZonasConfusion() {
        // Cantidad de zonas proporcional a la complejidad
        int cantidadZonas = (int) (complejidad / 100.0 * Math.max(1, (N * M) / 80));
        if (cantidadZonas < 1) return;

        int tamañoZona = Math.min(3, Math.min(N, M) - 1); // Zonas de 3x3 o menos
        if (tamañoZona < 2) return;

        for (int z = 0; z < cantidadZonas; z++) {
            // Elegir esquina superior-izquierda aleatoria
            int filaInicio = random.nextInt(N - tamañoZona);
            int colInicio = random.nextInt(M - tamañoZona);

            // Eliminar todas las paredes internas de la zona
            for (int i = filaInicio; i < filaInicio + tamañoZona; i++) {
                for (int j = colInicio; j < colInicio + tamañoZona; j++) {
                    Celda c = cuadricula[i][j];
                    // Eliminar paredes con vecinos que están dentro de la zona
                    for (Celda vecina : adyacentes(c)) {
                        if (vecina.fila >= filaInicio && vecina.fila < filaInicio + tamañoZona
                            && vecina.col >= colInicio && vecina.col < colInicio + tamañoZona) {
                            c.paredes.remove(vecina);
                            vecina.paredes.remove(c);
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // TÉCNICA 4: Alargamiento del Camino Principal
    // ==========================================
    /**
     * Analiza el camino más corto actual con BFS y, si es demasiado directo,
     * agrega paredes estratégicas para forzar desvíos.
     */
    private void alargarCaminoPrincipal() {
        // Solo aplicar si la complejidad es significativa
        if (complejidad < 30) return;

        // BFS interno para encontrar el camino actual más corto
        List<Celda> caminoActual = bfsInterno(celdaEntrada, celdaFin);
        if (caminoActual.isEmpty()) return;

        int distManhattan = Math.abs(celdaEntrada.fila - celdaFin.fila) 
                          + Math.abs(celdaEntrada.col - celdaFin.col);
        
        // Si el camino ya es al menos 2x la distancia Manhattan, no alargar más
        if (caminoActual.size() >= distManhattan * 2) return;

        // Intentar bloquear pasajes del camino actual para forzar desvíos
        // Solo bloqueamos un porcentaje pequeño para no desconectar el laberinto
        int bloqueos = (int) (caminoActual.size() * (complejidad / 100.0) * 0.15);
        
        List<Integer> indices = new ArrayList<>();
        // No bloquear inicio ni fin del camino
        for (int i = 2; i < caminoActual.size() - 2; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices, random);

        int bloqueados = 0;
        for (int idx : indices) {
            if (bloqueados >= bloqueos) break;

            Celda c = caminoActual.get(idx);
            Celda siguiente = caminoActual.get(idx + 1);

            // Verificar que no están separadas por pared (están conectadas)
            if (!c.paredes.contains(siguiente)) {
                // Agregar pared entre ellas
                c.paredes.add(siguiente);
                siguiente.paredes.add(c);

                // Verificar que el laberinto sigue siendo resoluble
                List<Celda> nuevoPath = bfsInterno(celdaEntrada, celdaFin);
                if (nuevoPath.isEmpty()) {
                    // Revertir: el bloqueo desconectó el laberinto
                    c.paredes.remove(siguiente);
                    siguiente.paredes.remove(c);
                } else {
                    bloqueados++;
                }
            }
        }
    }

    /**
     * BFS interno para verificar caminos (no modifica estado visual de celdas)
     */
    private List<Celda> bfsInterno(Celda inicio, Celda fin) {
        Map<Celda, Celda> padres = new HashMap<>();
        Queue<Celda> cola = new LinkedList<>();
        Set<Celda> visitados = new HashSet<>();

        cola.add(inicio);
        visitados.add(inicio);
        padres.put(inicio, null);

        while (!cola.isEmpty()) {
            Celda actual = cola.poll();
            if (actual == fin) {
                // Reconstruir camino
                List<Celda> camino = new ArrayList<>();
                Celda c = fin;
                while (c != null) {
                    camino.add(0, c);
                    c = padres.get(c);
                }
                return camino;
            }

            for (Celda vecina : adyacentes(actual)) {
                if (!actual.paredes.contains(vecina) && !visitados.contains(vecina)) {
                    visitados.add(vecina);
                    padres.put(vecina, actual);
                    cola.add(vecina);
                }
            }
        }
        return Collections.emptyList(); // Sin camino
    }

    //Celdas adyacentes en la cuadrícula (vecinas geométricas)
    private List<Celda> adyacentes(Celda celdaCentral) {
        List<Celda> vecinas = new ArrayList<>(4);
        if (celdaCentral.fila > 0) {
            vecinas.add(cuadricula[celdaCentral.fila - 1][celdaCentral.col]);
        }
        if (celdaCentral.fila < N - 1) {
            vecinas.add(cuadricula[celdaCentral.fila + 1][celdaCentral.col]);
        }
        if (celdaCentral.col > 0) {
            vecinas.add(cuadricula[celdaCentral.fila][celdaCentral.col - 1]);
        }
        if (celdaCentral.col < M - 1) {
            vecinas.add(cuadricula[celdaCentral.fila][celdaCentral.col + 1]);
        }
        return vecinas;
    }

    private List<Celda> vecinosNoVisitados(Celda celdaActual) {
        List<Celda> celdasNoVisitadas = new ArrayList<>();
        for (Celda celdaAdyacente : adyacentes(celdaActual)) {
            //Aqui hace la pregunta si la celda no solo es adyacente sino es visitada
            if (!celdaAdyacente.visitada) {
                celdasNoVisitadas.add(celdaAdyacente);
            }
        }
        return celdasNoVisitadas;
    }

    private Celda elegirCeldaBordeAleatoria() {
        List<Celda> borde = new ArrayList<>();
        for (int j = 0; j < M; j++) {
            borde.add(cuadricula[0][j]);
            borde.add(cuadricula[N - 1][j]);
        }
        for (int i = 1; i < N - 1; i++) {
            borde.add(cuadricula[i][0]);
            borde.add(cuadricula[i][M - 1]);
        }
        return borde.get(random.nextInt(borde.size()));
    }

    //Render ASCII (solo visualización; consulta el set de paredes de cada celda).
    //Las "direcciones" aquí son geométricas para dibujar, no parte del algoritmo.
    public void imprimir() {
        StringBuilder sb = new StringBuilder();

        // Borde superior
        for (int j = 0; j < M; j++) {
            Celda c = cuadricula[0][j];
            boolean abierto = esAperturaExterior(c, 0, c.col);
            sb.append('+').append(abierto ? "   " : "---");
        }
        sb.append("+\n");

        for (int i = 0; i < N; i++) {
            Celda c0 = cuadricula[i][0];
            sb.append(esAperturaExterior(c0, c0.fila, 0) ? " " : "|");
            for (int j = 0; j < M; j++) {
                Celda c = cuadricula[i][j];
                sb.append(' ').append(c == celdaEntrada ? 'E' : c == celdaFin ? 'F' : ' ').append(' ');
                if (j == M - 1) {
                    sb.append(esAperturaExterior(c, c.fila, M - 1) ? " " : "|");
                } else {
                    Celda der = cuadricula[i][j + 1];
                    sb.append(c.paredes.contains(der) ? "|" : " ");
                }
            }
            sb.append('\n');

            for (int j = 0; j < M; j++) {
                Celda c = cuadricula[i][j];
                if (i == N - 1) {
                    sb.append('+').append(esAperturaExterior(c, N - 1, c.col) ? "   " : "---");
                } else {
                    Celda ab = cuadricula[i + 1][j];
                    sb.append('+').append(c.paredes.contains(ab) ? "---" : "   ");
                }
            }
            sb.append("+\n");
        }
        System.out.print(sb);
    }

    //¿La celda c está en el borde indicado y es entrada/fin? (para abrir la pared exterior al dibujar)
    private boolean esAperturaExterior(Celda c, int filaBorde, int colBorde) {
        if (c != celdaEntrada && c != celdaFin) return false;
        return c.fila == filaBorde && c.col == colBorde
            && (filaBorde == 0 || filaBorde == N - 1 || colBorde == 0 || colBorde == M - 1);
    }

    // Getters para acceso desde GUI
    public int getN() { return N; }
    public int getM() { return M; }
    public double getComplejidad() { return complejidad; }
    public long getSemilla() { return semilla; }
    public Celda[][] getCuadricula() { return cuadricula; }
    public Celda getCeldaEntrada() { return celdaEntrada; }
    public Celda getCeldaFin() { return celdaFin; }
}
