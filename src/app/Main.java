package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import deportes.Conexion;
import deportes.Deporte;
import deportes.Estudiante;
import deportes.SistemaDeportes;
import estructuras.DLL;
import estructuras.DLLNode;

// Interfaz de consola del prototipo. Los datos se pueden cargar desde un archivo de texto en UTF-8 con una
// línea por estudiante: id;nombre;deportes que practica;deportes que le interesan (las listas, separadas por coma)
// Uso: java -cp out app.Main [archivo de datos]
public class Main {

    private static final SistemaDeportes sistema = new SistemaDeportes();
    private static final Scanner entrada = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length > 0) {
            cargarArchivo(args[0]);
        }
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> cargarArchivo(leerTexto("Ruta del archivo: "));
                    case 2 -> registrar();
                    case 3 -> consultar();
                    case 4 -> eliminar();
                    case 5 -> mostrarComunidades();
                    case 6 -> buscarConexion();
                    case 7 -> mostrarRanking();
                    case 8 -> listarEstudiantes();
                    case 0 -> System.out.println("Hasta luego.");
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("=== Deportes UNAL (" + sistema.cantidadEstudiantes() + " estudiantes, "
                + sistema.cantidadDeportes() + " deportes) ===");
        System.out.println("1. Cargar estudiantes desde un archivo");
        System.out.println("2. Registrar un estudiante");
        System.out.println("3. Consultar un estudiante por ID");
        System.out.println("4. Eliminar un estudiante");
        System.out.println("5. Ver las comunidades deportivas");
        System.out.println("6. Buscar conexión con un deporte de interés");
        System.out.println("7. Ver los deportes ordenados por cantidad de practicantes");
        System.out.println("8. Listar todos los estudiantes");
        System.out.println("0. Salir");
    }

    private static void registrar() {
        int id = leerEntero("ID: ");
        String nombre = leerTexto("Nombre: ");
        String[] practica = leerTexto("Deportes que practica (separados por coma): ").split(",");
        String[] intereses = leerTexto("Deportes que le interesan (separados por coma): ").split(",");
        Estudiante estudiante = sistema.registrarEstudiante(id, nombre, practica, intereses);
        System.out.println("Registrado: " + estudiante);
    }

    private static void consultar() {
        Estudiante estudiante = sistema.buscarEstudiante(leerEntero("ID: "));
        System.out.println(estudiante == null ? "No existe un estudiante con ese ID." : estudiante.toString());
    }

    private static void eliminar() {
        boolean eliminado = sistema.eliminarEstudiante(leerEntero("ID: "));
        System.out.println(eliminado ? "Estudiante eliminado." : "No existe un estudiante con ese ID.");
    }

    private static void mostrarComunidades() {
        DLL<DLL<Estudiante>> comunidades = sistema.comunidades();
        if (comunidades.isEmpty()) {
            System.out.println("Todavía no hay estudiantes que compartan un deporte.");
            return;
        }
        int numero = 1;
        for (DLLNode<DLL<Estudiante>> n = comunidades.getHead(); n != null; n = n.getNext()) {
            DLL<Estudiante> comunidad = n.getData();
            System.out.println("Comunidad " + numero + " (" + comunidad.size() + " estudiantes): " + nombres(comunidad));
            numero++;
        }
    }

    private static void buscarConexion() {
        Conexion conexion = sistema.buscarConexion(leerEntero("ID del estudiante: "));
        System.out.println(conexion);
    }

    private static void mostrarRanking() {
        int posicion = 1;
        for (DLLNode<Deporte> n = sistema.deportesPorPracticantes().getHead(); n != null; n = n.getNext()) {
            Deporte deporte = n.getData();
            System.out.println(posicion + ". " + deporte.getNombre() + ": " + deporte.cantidadPracticantes()
                    + (deporte.cantidadPracticantes() == 1 ? " estudiante" : " estudiantes"));
            posicion++;
        }
    }

    private static void listarEstudiantes() {
        for (DLLNode<Estudiante> n = sistema.estudiantesPorId().getHead(); n != null; n = n.getNext()) {
            System.out.println(n.getData());
        }
    }

    // carga el archivo línea por línea; las líneas vacías o que empiezan con # se ignoran
    private static void cargarArchivo(String ruta) {
        int cargados = 0;
        int numeroLinea = 0;
        // se lee siempre en UTF-8: con la codificación por defecto (en Windows con Java 17 no es UTF-8) "Fútbol"
        // llegaría con otros caracteres y quedaría como un deporte distinto de "futbol"
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (numeroLinea == 1 && linea.startsWith("\uFEFF")) {
                    linea = linea.substring(1); // marca BOM que agregan algunos editores al guardar en UTF-8
                }
                if (linea.isBlank() || linea.trim().startsWith("#")) {
                    continue;
                }
                String[] partes = linea.split(";", -1);
                if (partes.length != 4) {
                    System.out.println("Línea " + numeroLinea + " ignorada: se esperaban 4 campos separados por ;");
                    continue;
                }
                try {
                    sistema.registrarEstudiante(Integer.parseInt(partes[0].trim()), partes[1],
                            partes[2].split(","), partes[3].split(","));
                    cargados++;
                } catch (NumberFormatException e) { // va antes porque es un caso de IllegalArgumentException
                    System.out.println("Línea " + numeroLinea + " ignorada: el ID debe ser un número entero");
                } catch (IllegalArgumentException e) {
                    System.out.println("Línea " + numeroLinea + " ignorada: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
        System.out.println(cargados + (cargados == 1 ? " estudiante cargado" : " estudiantes cargados") + " desde " + ruta);
    }

    private static String nombres(DLL<Estudiante> estudiantes) {
        StringBuilder texto = new StringBuilder();
        for (DLLNode<Estudiante> n = estudiantes.getHead(); n != null; n = n.getNext()) {
            texto.append(n.getData().getNombre());
            if (n.getNext() != null) {
                texto.append(", ");
            }
        }
        return texto.toString();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                System.out.println("Se esperaba un número entero.");
            }
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!entrada.hasNextLine()) { // fin de la entrada (por ejemplo, Ctrl+D o un archivo redirigido)
            System.out.println();
            System.out.println("Fin de la entrada. Hasta luego.");
            System.exit(0);
        }
        return entrada.nextLine();
    }
}
