package deportes;

// Clave del árbol que ordena los deportes: primero los que tienen más practicantes y, con empate,
// en orden alfabético. Cuando cambia la cantidad de un deporte, se saca del árbol con la clave
// vieja y se vuelve a meter con la nueva (dos operaciones O(log d))
public class ClaveRanking implements Comparable<ClaveRanking> {
    private final int cantidad;
    private final String nombre;

    public ClaveRanking(int cantidad, String nombre) {
        this.cantidad = cantidad;
        this.nombre = nombre;
    }

    @Override
    public int compareTo(ClaveRanking otra) { // O(L)
        if (cantidad != otra.cantidad) {
            return Integer.compare(otra.cantidad, cantidad); // más practicantes va primero
        }
        return nombre.compareTo(otra.nombre);
    }
}
