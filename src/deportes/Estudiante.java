package deportes;

import estructuras.DLL;
import estructuras.DLLNode;

// Un estudiante con su ID, su nombre, los deportes que practica y los que le interesan
public class Estudiante {
    private final int id;
    private final String nombre;
    private final DLL<Practica> practica;
    private final DLL<Deporte> intereses;

    // datos que usa el recorrido por anchura de SistemaDeportes: en qué recorrido se visitó por
    // última vez, desde qué estudiante se llegó y por cuál deporte compartido
    int marcaVisita;
    Estudiante anterior;
    Deporte deporteComun;

    // Cada deporte que practica guarda también el nodo de este estudiante en la lista de practicantes
    // del deporte. Así, al eliminar al estudiante, se le saca de cada lista en O(1) sin buscarlo
    static class Practica {
        final Deporte deporte;
        final DLLNode<Estudiante> nodo;

        Practica(Deporte deporte, DLLNode<Estudiante> nodo) {
            this.deporte = deporte;
            this.nodo = nodo;
        }
    }

    Estudiante(int id, String nombre) {
        this.id = id;
        this.nombre = nombre.trim();
        this.practica = new DLL<>();
        this.intereses = new DLL<>();
    }

    public int getId() { // O(1)
        return id;
    }

    public String getNombre() { // O(1)
        return nombre;
    }

    public int cantidadDeportesQuePractica() { // O(1)
        return practica.size();
    }

    public boolean practica(Deporte deporte) { // O(p), con p los deportes que practica
        for (DLLNode<Practica> n = practica.getHead(); n != null; n = n.getNext()) {
            if (n.getData().deporte == deporte) {
                return true;
            }
        }
        return false;
    }

    public boolean leInteresa(Deporte deporte) { // O(i), con i los deportes que le interesan
        return intereses.find(deporte) != null;
    }

    public DLL<String> deportesQuePractica() { // O(p)
        DLL<String> nombres = new DLL<>();
        for (DLLNode<Practica> n = practica.getHead(); n != null; n = n.getNext()) {
            nombres.pushBack(n.getData().deporte.getNombre());
        }
        return nombres;
    }

    public DLL<String> deportesDeInteres() { // O(i)
        DLL<String> nombres = new DLL<>();
        for (DLLNode<Deporte> n = intereses.getHead(); n != null; n = n.getNext()) {
            nombres.pushBack(n.getData().getNombre());
        }
        return nombres;
    }

    DLL<Practica> getPractica() { // O(1)
        return practica;
    }

    DLL<Deporte> getIntereses() { // O(1)
        return intereses;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.id == ((Estudiante) obj).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() { // O(p + i)
        return nombre + " (ID " + id + ") | practica: " + deportesQuePractica() + " | le interesan: "
                + deportesDeInteres();
    }
}
