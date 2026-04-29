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

    private Celda[][] cuadricula;
    private final Random random = new Random();
    private Celda celdaEntrada;
    //Verificar celdaFin
    private Celda celdaFin;

    public GeneradorLaberinto(int n, int m, int minSol, int maxSol, int entradas, int salidas) {
        this.N = n;
        this.M = m;
        this.minSoluciones = minSol;
        this.maxSoluciones = maxSol;
        this.numEntradas = entradas;
        this.numSalidas = salidas;
    }

    public void generar() {
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
                return;
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
    }

    //Celdas adyacentes en la cuadrícula (vecinas geométricas)
    private List<Celda> adyacentes(Celda c) {
        List<Celda> v = new ArrayList<>(4);
        if (c.fila > 0)     v.add(cuadricula[c.fila - 1][c.col]);
        if (c.fila < N - 1) v.add(cuadricula[c.fila + 1][c.col]);
        if (c.col > 0)      v.add(cuadricula[c.fila][c.col - 1]);
        if (c.col < M - 1)  v.add(cuadricula[c.fila][c.col + 1]);
        return v;
    }

    private List<Celda> vecinosNoVisitados(Celda c) {
        List<Celda> v = new ArrayList<>();
        for (Celda a : adyacentes(c))
            //Aqui hace la pregunta si la celda no solo es adyacente sino es visitada
            if (!a.visitada) v.add(a);
        return v;
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
    public Celda[][] getCuadricula() { return cuadricula; }
    public Celda getCeldaEntrada() { return celdaEntrada; }
    public Celda getCeldaFin() { return celdaFin; }
}
