package deportes;

import java.text.Normalizer;

import estructuras.DLL;

// Un deporte y la lista de estudiantes que lo practican. La cantidad de practicantes es el tamaño
// de esa lista, que la DLL lleva en un contador
public class Deporte {
    private final String nombre; // como se escribió la primera vez, para mostrarlo
    private final String clave; // nombre normalizado, con el que se busca en el árbol de deportes
    private final DLL<Estudiante> practicantes;

    // marcas que usa el recorrido por anchura de SistemaDeportes (ver allí la explicación)
    int marcaVisita;
    int marcaInteres;

    Deporte(String nombre) {
        this.nombre = nombre.trim();
        this.clave = normalizar(nombre);
        this.practicantes = new DLL<>();
    }

    public String getNombre() { // O(1)
        return nombre;
    }

    public String getClave() { // O(1)
        return clave;
    }

    public int cantidadPracticantes() { // O(1)
        return practicantes.size();
    }

    DLL<Estudiante> getPracticantes() { // O(1)
        return practicantes;
    }

    // "Fútbol", " futbol " y "FUTBOL" son el mismo deporte: se quitan tildes, espacios de más y mayúsculas
    public static String normalizar(String nombre) { // O(L), con L la longitud del nombre
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del deporte no puede ser null");
        }
        String sinTildes = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().replaceAll("\\s+", " ");
    }

    @Override
    public String toString() { // O(1)
        return nombre + " (" + cantidadPracticantes() + ")";
    }
}
