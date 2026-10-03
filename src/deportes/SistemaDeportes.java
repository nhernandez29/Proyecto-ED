package deportes;

import estructuras.ArbolAVL;
import estructuras.DLL;
import estructuras.DLLNode;
import estructuras.Queue;

// Fachada del sistema. Notación de las complejidades: n estudiantes, d deportes, p deportes que practica
// un estudiante, i deportes que le interesan y m el total de parejas (estudiante, deporte que practica).
//
// Estudiantes y deportes forman, sin decirlo, un grafo bipartito: cada estudiante está unido a los deportes
// que practica y cada deporte a sus practicantes (las dos listas son las aristas). Dos estudiantes están
// conectados directamente si comparten un deporte, e indirectamente si hay una cadena de estudiantes que
// comparten deportes entre sí. Las consultas de conexión y de comunidades recorren ese grafo por anchura.
public class SistemaDeportes {

    private final ArbolAVL<Integer, Estudiante> estudiantes = new ArbolAVL<>();
    private final ArbolAVL<String, Deporte> deportes = new ArbolAVL<>();
    private final ArbolAVL<ClaveRanking, Deporte> ranking = new ArbolAVL<>();

    // Cada recorrido usa un número distinto: un estudiante o un deporte está visitado si su marca es igual
    // al número del recorrido actual. Así no hay que limpiar las marcas antes de cada recorrido
    private int numeroRecorrido = 0;

    // RF1. Registra un estudiante. Los deportes que no existían se crean. Si un deporte aparece como
    // practicado y como interés, cuenta solo como practicado
    public Estudiante registrarEstudiante(int id, String nombre, String[] practica, String[] intereses) {
        // O(log n + (p + i) log d)
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (estudiantes.contiene(id)) {
            throw new IllegalArgumentException("Ya existe un estudiante con el ID " + id);
        }
        Estudiante estudiante = new Estudiante(id, nombre);
        for (String nombreDeporte : practica) {
            if (nombreDeporte == null || nombreDeporte.isBlank()) {
                continue;
            }
            Deporte deporte = obtenerOCrear(nombreDeporte);
            if (estudiante.practica(deporte)) {
                continue; // el deporte estaba repetido en la lista
            }
            quitarDelRanking(deporte);
            DLLNode<Estudiante> nodo = deporte.getPracticantes().pushBack(estudiante);
            estudiante.getPractica().pushBack(new Estudiante.Practica(deporte, nodo));
            agregarAlRanking(deporte);
        }
        for (String nombreDeporte : intereses) {
            if (nombreDeporte == null || nombreDeporte.isBlank()) {
                continue;
            }
            Deporte deporte = obtenerOCrear(nombreDeporte);
            if (!estudiante.practica(deporte) && !estudiante.leInteresa(deporte)) {
                estudiante.getIntereses().pushBack(deporte);
            }
        }
        estudiantes.insertar(id, estudiante);
        return estudiante;
    }

    // RF2. Acceso directo a los datos de un estudiante por su ID; retorna null si no existe
    public Estudiante buscarEstudiante(int id) { // O(log n)
        return estudiantes.buscar(id);
    }

    // RF3. Elimina un estudiante del sistema y lo saca de la lista de cada deporte que practicaba
    public boolean eliminarEstudiante(int id) { // O(log n + p log d)
        Estudiante estudiante = estudiantes.eliminar(id);
        if (estudiante == null) {
            return false;
        }
        for (DLLNode<Estudiante.Practica> n = estudiante.getPractica().getHead(); n != null; n = n.getNext()) {
            Deporte deporte = n.getData().deporte;
            quitarDelRanking(deporte);
            deporte.getPracticantes().deleteNode(n.getData().nodo); // O(1) gracias a la referencia guardada
            agregarAlRanking(deporte);
        }
        return true;
    }

    // RF4. Comunidades deportivas: grupos de estudiantes conectados entre sí, directa o indirectamente,
    // por deportes que practican en común. Un estudiante que no comparte ningún deporte no forma comunidad
    public DLL<DLL<Estudiante>> comunidades() { // O(n + m)
        DLL<DLL<Estudiante>> resultado = new DLL<>();
        numeroRecorrido++;
        DLL<Estudiante> todos = estudiantes.valoresEnOrden();
        for (DLLNode<Estudiante> n = todos.getHead(); n != null; n = n.getNext()) {
            Estudiante estudiante = n.getData();
            if (estudiante.marcaVisita != numeroRecorrido) {
                DLL<Estudiante> comunidad = recorrerDesde(estudiante);
                if (comunidad.size() > 1) {
                    resultado.pushBack(comunidad);
                }
            }
        }
        return resultado;
    }

    // la comunidad a la que pertenece un estudiante (él incluido)
    public DLL<Estudiante> comunidadDe(int id) { // O(n + m) en el peor caso
        Estudiante estudiante = buscarObligatorio(id);
        numeroRecorrido++;
        return recorrerDesde(estudiante);
    }

    // RF5 y RF6. Busca, por anchura, el estudiante más cercano que practica alguno de los deportes que le
    // interesan al estudiante dado. Como el recorrido avanza por niveles, el primero que se encuentra es el
    // que tiene menos intermediarios. Si nadie alcanzable lo practica, se indica que no hay conexión
    public Conexion buscarConexion(int id) { // O(n + m)
        Estudiante origen = buscarObligatorio(id);
        if (origen.getIntereses().isEmpty()) {
            return new Conexion(origen, "no tiene deportes de interés registrados");
        }
        numeroRecorrido++;
        // se marcan los deportes de interés para revisar a cada estudiante en O(p) y no en O(p · i)
        for (DLLNode<Deporte> n = origen.getIntereses().getHead(); n != null; n = n.getNext()) {
            n.getData().marcaInteres = numeroRecorrido;
        }
        Queue<Estudiante> cola = new Queue<>();
        origen.marcaVisita = numeroRecorrido;
        origen.anterior = null;
        origen.deporteComun = null;
        cola.enqueue(origen);
        while (!cola.isEmpty()) {
            Estudiante actual = cola.dequeue();
            if (actual != origen) {
                Deporte deporte = deporteDeInteresQuePractica(actual);
                if (deporte != null) {
                    return new Conexion(origen, actual, deporte);
                }
            }
            encolarVecinos(actual, cola);
        }
        return new Conexion(origen, "ningún estudiante conectado con " + origen.getNombre() + " practica "
                + origen.deportesDeInteres());
    }

    // RF7. Deportes ordenados de más a menos practicantes (con empate, alfabéticamente)
    public DLL<Deporte> deportesPorPracticantes() { // O(d)
        return ranking.valoresEnOrden();
    }

    // copia de la lista de quienes practican un deporte; null si el deporte no existe
    public DLL<Estudiante> practicantesDe(String nombreDeporte) { // O(log d + k), con k los practicantes
        Deporte deporte = deportes.buscar(Deporte.normalizar(nombreDeporte));
        if (deporte == null) {
            return null;
        }
        DLL<Estudiante> copia = new DLL<>();
        for (DLLNode<Estudiante> n = deporte.getPracticantes().getHead(); n != null; n = n.getNext()) {
            copia.pushBack(n.getData());
        }
        return copia;
    }

    public DLL<Estudiante> estudiantesPorId() { // O(n)
        return estudiantes.valoresEnOrden();
    }

    public int cantidadEstudiantes() { // O(1)
        return estudiantes.size();
    }

    public int cantidadDeportes() { // O(1)
        return deportes.size();
    }

    // recorrido por anchura desde un estudiante; marca con el número de recorrido actual a los visitados
    private DLL<Estudiante> recorrerDesde(Estudiante origen) { // O(tamaño de la comunidad)
        DLL<Estudiante> visitados = new DLL<>();
        Queue<Estudiante> cola = new Queue<>();
        origen.marcaVisita = numeroRecorrido;
        origen.anterior = null;
        origen.deporteComun = null;
        cola.enqueue(origen);
        while (!cola.isEmpty()) {
            Estudiante actual = cola.dequeue();
            visitados.pushBack(actual);
            encolarVecinos(actual, cola);
        }
        return visitados;
    }

    // Encola a los estudiantes que comparten un deporte con actual y que no se han visitado. Cada deporte
    // se expande una sola vez por recorrido, así que en total se revisa cada pareja (estudiante, deporte)
    // un número constante de veces
    private void encolarVecinos(Estudiante actual, Queue<Estudiante> cola) {
        for (DLLNode<Estudiante.Practica> n = actual.getPractica().getHead(); n != null; n = n.getNext()) {
            Deporte deporte = n.getData().deporte;
            if (deporte.marcaVisita == numeroRecorrido) {
                continue;
            }
            deporte.marcaVisita = numeroRecorrido;
            for (DLLNode<Estudiante> v = deporte.getPracticantes().getHead(); v != null; v = v.getNext()) {
                Estudiante vecino = v.getData();
                if (vecino.marcaVisita != numeroRecorrido) {
                    vecino.marcaVisita = numeroRecorrido;
                    vecino.anterior = actual;
                    vecino.deporteComun = deporte;
                    cola.enqueue(vecino);
                }
            }
        }
    }

    private Deporte deporteDeInteresQuePractica(Estudiante estudiante) { // O(p)
        for (DLLNode<Estudiante.Practica> n = estudiante.getPractica().getHead(); n != null; n = n.getNext()) {
            if (n.getData().deporte.marcaInteres == numeroRecorrido) {
                return n.getData().deporte;
            }
        }
        return null;
    }

    private Deporte obtenerOCrear(String nombre) { // O(log d)
        String clave = Deporte.normalizar(nombre);
        Deporte deporte = deportes.buscar(clave);
        if (deporte == null) {
            deporte = new Deporte(nombre);
            deportes.insertar(clave, deporte);
            agregarAlRanking(deporte);
        }
        return deporte;
    }

    private void quitarDelRanking(Deporte deporte) { // O(log d)
        ranking.eliminar(new ClaveRanking(deporte.cantidadPracticantes(), deporte.getClave()));
    }

    private void agregarAlRanking(Deporte deporte) { // O(log d)
        ranking.insertar(new ClaveRanking(deporte.cantidadPracticantes(), deporte.getClave()), deporte);
    }

    private Estudiante buscarObligatorio(int id) { // O(log n)
        Estudiante estudiante = estudiantes.buscar(id);
        if (estudiante == null) {
            throw new IllegalArgumentException("No existe un estudiante con el ID " + id);
        }
        return estudiante;
    }
}
