package deportes;

import estructuras.DLL;
import estructuras.DLLNode;

// Resultado de buscar si un estudiante está conectado con alguien que practica uno de los deportes
// que le interesan. Si la conexión existe, guarda la cadena de estudiantes y los deportes que
// comparte cada par seguido, es decir, a quién acudir y a través de quién
public class Conexion {
    private final Estudiante origen;
    private final Estudiante destino;
    private final Deporte deporteDeInteres;
    private final DLL<Estudiante> camino;
    private final DLL<Deporte> enlaces;
    private final String motivo;

    // conexión encontrada: el camino se arma hacia atrás con las referencias que dejó el recorrido
    Conexion(Estudiante origen, Estudiante destino, Deporte deporteDeInteres) { // O(k), con k la longitud del camino
        this.origen = origen;
        this.destino = destino;
        this.deporteDeInteres = deporteDeInteres;
        this.camino = new DLL<>();
        this.enlaces = new DLL<>();
        this.motivo = null;
        for (Estudiante actual = destino; actual != null; actual = actual.anterior) {
            camino.pushFront(actual);
            if (actual.deporteComun != null) {
                enlaces.pushFront(actual.deporteComun);
            }
        }
    }

    // no hay conexión
    Conexion(Estudiante origen, String motivo) {
        this.origen = origen;
        this.destino = null;
        this.deporteDeInteres = null;
        this.camino = new DLL<>();
        this.enlaces = new DLL<>();
        this.motivo = motivo;
    }

    public boolean existe() { // O(1)
        return destino != null;
    }

    // directa: el origen comparte un deporte con quien practica el deporte de interés
    public boolean esDirecta() { // O(1)
        return existe() && camino.size() == 2;
    }

    public int intermediarios() { // O(1)
        return existe() ? camino.size() - 2 : 0;
    }

    public Estudiante getOrigen() { // O(1)
        return origen;
    }

    public Estudiante getDestino() { // O(1)
        return destino;
    }

    public Deporte getDeporteDeInteres() { // O(1)
        return deporteDeInteres;
    }

    public DLL<Estudiante> getCamino() { // O(1)
        return camino;
    }

    public String getMotivo() { // O(1)
        return motivo;
    }

    @Override
    public String toString() { // O(k)
        if (!existe()) {
            return "No existe conexión para " + origen.getNombre() + ": " + motivo + ".";
        }
        StringBuilder texto = new StringBuilder();
        texto.append(origen.getNombre()).append(" puede contactar a ").append(destino.getNombre())
                .append(" (ID ").append(destino.getId()).append("), que practica ")
                .append(deporteDeInteres.getNombre()).append(". Conexión ")
                .append(esDirecta() ? "directa" : "indirecta, con " + intermediarios()
                        + (intermediarios() == 1 ? " intermediario" : " intermediarios"))
                .append(": ");
        DLLNode<Deporte> enlace = enlaces.getHead();
        for (DLLNode<Estudiante> n = camino.getHead(); n != null; n = n.getNext()) {
            texto.append(n.getData().getNombre());
            if (enlace != null) {
                texto.append(" -[").append(enlace.getData().getNombre()).append("]- ");
                enlace = enlace.getNext();
            }
        }
        return texto.toString();
    }
}
