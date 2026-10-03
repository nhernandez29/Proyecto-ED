package estructuras;

// Árbol AVL: árbol binario de búsqueda que, después de cada inserción o eliminación, rota los nodos
// necesarios para que en todo nodo la altura de los dos subárboles difiera a lo sumo en 1. Así la
// altura es O(log n) y buscar, insertar y eliminar cuestan O(log n) incluso si las claves llegan en orden
public class ArbolAVL<K extends Comparable<K>, V> {

    private static class Nodo<K, V> {
        private final K clave;
        private V valor;
        private int altura;
        private Nodo<K, V> izquierdo;
        private Nodo<K, V> derecho;

        private Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.altura = 1;
        }
    }

    private Nodo<K, V> raiz;
    private int tamano;

    public V buscar(K clave) { // O(log n)
        revisarClave(clave);
        Nodo<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.clave);
            if (comparacion == 0) {
                return actual.valor;
            }
            actual = comparacion < 0 ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    public boolean contiene(K clave) { // O(log n)
        return buscar(clave) != null;
    }

    // si la clave ya existe, se reemplaza su valor
    public void insertar(K clave, V valor) { // O(log n)
        revisarClave(clave);
        if (valor == null) {
            throw new IllegalArgumentException("El valor no puede ser null"); // null significa "no está" en buscar
        }
        raiz = insertar(raiz, clave, valor);
    }

    // retorna el valor que tenía la clave, o null si no estaba
    public V eliminar(K clave) { // O(log n)
        V valor = buscar(clave);
        if (valor != null) {
            raiz = eliminar(raiz, clave);
            tamano--;
        }
        return valor;
    }

    public int size() { // O(1)
        return tamano;
    }

    public boolean isEmpty() { // O(1)
        return tamano == 0;
    }

    public int altura() { // O(1)
        return altura(raiz);
    }

    // recorrido inorden: los valores quedan ordenados por clave
    public DLL<V> valoresEnOrden() { // O(n)
        DLL<V> lista = new DLL<>();
        inorden(raiz, lista);
        return lista;
    }

    private Nodo<K, V> insertar(Nodo<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            tamano++;
            return new Nodo<>(clave, valor);
        }
        int comparacion = clave.compareTo(nodo.clave);
        if (comparacion < 0) {
            nodo.izquierdo = insertar(nodo.izquierdo, clave, valor);
        } else if (comparacion > 0) {
            nodo.derecho = insertar(nodo.derecho, clave, valor);
        } else {
            nodo.valor = valor;
            return nodo;
        }
        return balancear(nodo);
    }

    private Nodo<K, V> eliminar(Nodo<K, V> nodo, K clave) {
        int comparacion = clave.compareTo(nodo.clave);
        if (comparacion < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, clave);
        } else if (comparacion > 0) {
            nodo.derecho = eliminar(nodo.derecho, clave);
        } else {
            if (nodo.izquierdo == null) {
                return nodo.derecho;
            }
            if (nodo.derecho == null) {
                return nodo.izquierdo;
            }
            // con dos hijos, el nodo se reemplaza por el menor de su subárbol derecho
            Nodo<K, V> sucesor = nodo.derecho;
            while (sucesor.izquierdo != null) {
                sucesor = sucesor.izquierdo;
            }
            Nodo<K, V> reemplazo = new Nodo<>(sucesor.clave, sucesor.valor);
            reemplazo.izquierdo = nodo.izquierdo;
            reemplazo.derecho = eliminar(nodo.derecho, sucesor.clave);
            nodo = reemplazo;
        }
        return balancear(nodo);
    }

    // revisa el factor de balance y aplica la rotación que corresponda (simple o doble)
    private Nodo<K, V> balancear(Nodo<K, V> nodo) { // O(1)
        actualizarAltura(nodo);
        int balance = altura(nodo.izquierdo) - altura(nodo.derecho);
        if (balance > 1) {
            if (altura(nodo.izquierdo.izquierdo) < altura(nodo.izquierdo.derecho)) {
                nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            }
            return rotarDerecha(nodo);
        }
        if (balance < -1) {
            if (altura(nodo.derecho.derecho) < altura(nodo.derecho.izquierdo)) {
                nodo.derecho = rotarDerecha(nodo.derecho);
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    private Nodo<K, V> rotarDerecha(Nodo<K, V> nodo) { // O(1)
        Nodo<K, V> nuevaRaiz = nodo.izquierdo;
        nodo.izquierdo = nuevaRaiz.derecho;
        nuevaRaiz.derecho = nodo;
        actualizarAltura(nodo);
        actualizarAltura(nuevaRaiz);
        return nuevaRaiz;
    }

    private Nodo<K, V> rotarIzquierda(Nodo<K, V> nodo) { // O(1)
        Nodo<K, V> nuevaRaiz = nodo.derecho;
        nodo.derecho = nuevaRaiz.izquierdo;
        nuevaRaiz.izquierdo = nodo;
        actualizarAltura(nodo);
        actualizarAltura(nuevaRaiz);
        return nuevaRaiz;
    }

    private void actualizarAltura(Nodo<K, V> nodo) { // O(1)
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    private int altura(Nodo<K, V> nodo) { // O(1)
        return nodo == null ? 0 : nodo.altura;
    }

    private void inorden(Nodo<K, V> nodo, DLL<V> lista) {
        if (nodo != null) {
            inorden(nodo.izquierdo, lista);
            lista.pushBack(nodo.valor);
            inorden(nodo.derecho, lista);
        }
    }

    private void revisarClave(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser null");
        }
    }
}
