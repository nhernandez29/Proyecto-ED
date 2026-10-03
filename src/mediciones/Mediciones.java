package mediciones;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import deportes.Conexion;
import deportes.Estudiante;
import deportes.SistemaDeportes;
import estructuras.DLL;

// Medición preliminar de las operaciones del prototipo con datos generados al azar. Para cada tamaño se
// arma un sistema con n estudiantes, cada uno practica de 1 a 3 de 300 deportes y le interesan de 0 a 2.
// Cada operación se repite REPETICIONES veces y se toma la mediana. Antes de medir se hace una corrida
// completa con 40 000 estudiantes que no se guarda, y en cada tamaño hay CALENTAMIENTO repeticiones que
// no se miden, para que la JVM ya haya compilado todo el código cuando empieza la medición. Todo eso se
// hace RONDAS veces, pasando por todos los tamaños en cada ronda. Si otro programa ocupa el procesador
// durante un rato, la ronda de ese momento sale más lenta, nunca más rápida; por eso de cada valor se
// guarda el de la ronda más rápida. Todas las rondas quedan en rondas.csv para poder revisarlas.
// Uso: java -cp out mediciones.Mediciones [carpeta de salida]   (por defecto, la carpeta mediciones)
public class Mediciones {

    static final int[] TAMANOS = { 10000, 20000, 40000, 80000, 160000, 320000 };
    static final int DEPORTES = 300;
    static final int REPETICIONES = 15;
    static final int CALENTAMIENTO = 5;
    static final int RONDAS = 5;
    static final int CONSULTAS = 200000; // búsquedas por ID en cada repetición
    static final int ALTAS = 20000; // parejas registrar + eliminar en cada repetición

    static long control = 0; // se acumula algo de cada resultado para que el compilador no descarte las llamadas

    public static void main(String[] args) throws FileNotFoundException {
        String carpeta = args.length > 0 ? args[0] : "mediciones";
        String encabezado = "n,m,buscar_ns,registrar_eliminar_ns,conexion_ms,comunidades_ms";
        // resultados[ronda][tamaño] = { m, buscar, registrar + eliminar, conexión, comunidades }
        double[][][] resultados = new double[RONDAS][TAMANOS.length][];
        medirTamano(40000); // calentamiento: no se guarda
        try (PrintWriter rondas = new PrintWriter(carpeta + "/rondas.csv")) {
            rondas.println("ronda," + encabezado);
            for (int r = 0; r < RONDAS; r++) {
                System.out.println("Ronda " + (r + 1) + " de " + RONDAS);
                for (int t = 0; t < TAMANOS.length; t++) {
                    resultados[r][t] = medirTamano(TAMANOS[t]);
                    imprimir(TAMANOS[t], resultados[r][t]);
                    rondas.print((r + 1) + ",");
                    guardar(rondas, TAMANOS[t], resultados[r][t]);
                }
            }
        }
        System.out.println("Ronda más rápida de cada valor");
        try (PrintWriter csv = new PrintWriter(carpeta + "/resultados.csv")) {
            csv.println(encabezado);
            for (int t = 0; t < TAMANOS.length; t++) {
                double[] fila = resultados[0][t].clone();
                for (int r = 1; r < RONDAS; r++) {
                    for (int c = 1; c < 5; c++) {
                        fila[c] = Math.min(fila[c], resultados[r][t][c]);
                    }
                }
                imprimir(TAMANOS[t], fila);
                guardar(csv, TAMANOS[t], fila);
            }
        }
        System.out.println("Resultados en " + carpeta + "/resultados.csv y " + carpeta + "/rondas.csv (control "
                + control + ")");
    }

    static void guardar(PrintWriter csv, int n, double[] fila) {
        csv.printf(Locale.ROOT, "%d,%d,%.1f,%.1f,%.3f,%.3f%n", n, (int) fila[0], fila[1], fila[2], fila[3], fila[4]);
    }

    static void imprimir(int n, double[] fila) {
        System.out.printf("  n = %6d  m = %6d  buscar %7.1f ns  registrar+eliminar %8.1f ns  conexión %8.3f ms"
                + "  comunidades %8.3f ms%n", n, (int) fila[0], fila[1], fila[2], fila[3], fila[4]);
    }

    // retorna m y la mediana de cada operación para un sistema con n estudiantes
    static double[] medirTamano(int n) {
        Random azar = new Random(n); // semilla fija: cada corrida genera los mismos datos
        SistemaDeportes sistema = new SistemaDeportes();
        int m = generar(sistema, n, azar);
        // el estudiante 0 practica un deporte y le interesa uno que nadie practica: la búsqueda
        // de conexión tiene que recorrer toda su comunidad sin encontrar a nadie (peor caso)
        sistema.registrarEstudiante(0, "Origen", new String[] { "Deporte 0" }, new String[] { "Sin practicantes" });
        m++;
        double buscar = medirBuscar(sistema, n, azar);
        double altas = medirAltas(sistema, n, azar);
        double conexion = medirConexion(sistema);
        double comunidades = medirComunidades(sistema);
        return new double[] { m, buscar, altas, conexion, comunidades };
    }

    // registra n estudiantes con IDs del 1 al n en orden aleatorio; retorna el total de parejas (estudiante, deporte)
    static int generar(SistemaDeportes sistema, int n, Random azar) {
        int[] ids = new int[n];
        for (int i = 0; i < n; i++) {
            ids[i] = i + 1;
        }
        mezclar(ids, azar);
        int m = 0;
        for (int id : ids) {
            String[] practica = deportesAlAzar(1 + azar.nextInt(3), azar);
            String[] intereses = deportesAlAzar(azar.nextInt(3), azar);
            m += sistema.registrarEstudiante(id, "Estudiante " + id, practica, intereses).cantidadDeportesQuePractica();
        }
        return m;
    }

    static double medirBuscar(SistemaDeportes sistema, int n, Random azar) { // ns por búsqueda
        int[] consultas = new int[CONSULTAS];
        for (int i = 0; i < CONSULTAS; i++) {
            consultas[i] = 1 + azar.nextInt(n);
        }
        double[] tiempos = new double[REPETICIONES];
        for (int r = -CALENTAMIENTO; r < REPETICIONES; r++) {
            long inicio = System.nanoTime();
            for (int id : consultas) {
                control += sistema.buscarEstudiante(id).getId();
            }
            long fin = System.nanoTime();
            if (r >= 0) {
                tiempos[r] = (double) (fin - inicio) / CONSULTAS;
            }
        }
        return mediana(tiempos);
    }

    // registrar un estudiante nuevo y eliminarlo enseguida deja el sistema como estaba
    static double medirAltas(SistemaDeportes sistema, int n, Random azar) { // ns por pareja
        String[][] practica = new String[ALTAS][];
        String[][] intereses = new String[ALTAS][];
        for (int i = 0; i < ALTAS; i++) {
            practica[i] = deportesAlAzar(2, azar);
            intereses[i] = deportesAlAzar(1, azar);
        }
        double[] tiempos = new double[REPETICIONES];
        for (int r = -CALENTAMIENTO; r < REPETICIONES; r++) {
            long inicio = System.nanoTime();
            for (int i = 0; i < ALTAS; i++) {
                int id = n + 1 + i;
                control += sistema.registrarEstudiante(id, "Nuevo", practica[i], intereses[i]).getId();
                sistema.eliminarEstudiante(id);
            }
            long fin = System.nanoTime();
            if (r >= 0) {
                tiempos[r] = (double) (fin - inicio) / ALTAS;
            }
        }
        return mediana(tiempos);
    }

    static double medirConexion(SistemaDeportes sistema) { // ms por búsqueda
        double[] tiempos = new double[REPETICIONES];
        for (int r = -CALENTAMIENTO; r < REPETICIONES; r++) {
            long inicio = System.nanoTime();
            Conexion conexion = sistema.buscarConexion(0);
            long fin = System.nanoTime();
            if (conexion.existe()) {
                throw new IllegalStateException("el peor caso no debería encontrar conexión");
            }
            control += conexion.getMotivo().length();
            if (r >= 0) {
                tiempos[r] = (fin - inicio) / 1e6;
            }
        }
        return mediana(tiempos);
    }

    static double medirComunidades(SistemaDeportes sistema) { // ms por cálculo
        double[] tiempos = new double[REPETICIONES];
        for (int r = -CALENTAMIENTO; r < REPETICIONES; r++) {
            long inicio = System.nanoTime();
            DLL<DLL<Estudiante>> comunidades = sistema.comunidades();
            long fin = System.nanoTime();
            control += comunidades.size();
            if (r >= 0) {
                tiempos[r] = (fin - inicio) / 1e6;
            }
        }
        return mediana(tiempos);
    }

    // k nombres de deporte distintos entre los DEPORTES posibles
    static String[] deportesAlAzar(int k, Random azar) {
        String[] nombres = new String[k];
        int[] elegidos = new int[k];
        for (int i = 0; i < k; i++) {
            int d;
            boolean repetido;
            do {
                d = azar.nextInt(DEPORTES);
                repetido = false;
                for (int j = 0; j < i; j++) {
                    repetido = repetido || elegidos[j] == d;
                }
            } while (repetido);
            elegidos[i] = d;
            nombres[i] = "Deporte " + d;
        }
        return nombres;
    }

    static void mezclar(int[] a, Random azar) { // Fisher-Yates
        for (int i = a.length - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    static double mediana(double[] valores) {
        double[] copia = valores.clone();
        Arrays.sort(copia);
        int mitad = copia.length / 2;
        return copia.length % 2 == 1 ? copia[mitad] : (copia[mitad - 1] + copia[mitad]) / 2;
    }
}
