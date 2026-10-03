# Deportes UNAL

Proyecto del curso Estructuras de Datos (2026-2), Universidad Nacional de Colombia, Sede Bogotá.

## Descripción

Sistema de consola para registrar estudiantes de la universidad con los deportes que practican y los deportes que les interesan. A partir de esos datos el sistema:

1. Agrupa a los estudiantes que comparten al menos un deporte en comunidades deportivas. Dos estudiantes quedan en la misma comunidad si comparten un deporte o si hay una cadena de estudiantes que los une compartiendo deportes.
2. Indica si un estudiante está conectado, directa o indirectamente, con alguien que practica uno de los deportes que le interesan, y por medio de quién. Si esa conexión no existe, lo indica.
3. Da acceso directo a los datos de un estudiante a partir de su ID.
4. Permite eliminar a un estudiante.
5. Cuenta cuántos estudiantes practican cada deporte y muestra los deportes ordenados de más a menos practicantes.

Esta es la versión de la Entrega 1: un prototipo que ya resuelve los requisitos con listas, colas y árboles AVL implementados por el equipo, sin usar las colecciones de `java.util`.

## Integrantes

- Juan Diego Cuartas Casas
- Laura Juliana Espinosa Muñoz
- Marco Antonio García Villamil
- Nicolás Hernández Cardona
- Santiago Neira Lamadrid
- Juan Pablo Sánchez Ibáñez

## Lenguajes y requisitos

Java 17 o superior (se usan `switch` con flechas). No se usa Maven ni Gradle; basta con el JDK. La gráfica de las mediciones se dibuja con un script de Python 3 que usa matplotlib; es opcional y no hace falta para compilar ni ejecutar el programa.

## Instalación y ejecución

Clonar el repositorio:

```bash
git clone https://github.com/laurajespm/Proyecto-ED.git
cd Proyecto-ED
```

Compilar todo en la carpeta `out` (macOS o Linux):

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

En Windows, con PowerShell:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

Ejecutar el programa con los datos de ejemplo:

```bash
java -cp out app.Main datos/estudiantes.txt
```

El archivo es opcional; sin él, el programa arranca vacío y los estudiantes se registran o se cargan desde el menú. El menú tiene estas opciones:

```
1. Cargar estudiantes desde un archivo
2. Registrar un estudiante
3. Consultar un estudiante por ID
4. Eliminar un estudiante
5. Ver las comunidades deportivas
6. Buscar conexión con un deporte de interés
7. Ver los deportes ordenados por cantidad de practicantes
8. Listar todos los estudiantes
0. Salir
```

Correr las pruebas (imprimen PASÓ o FALLÓ por cada caso y terminan con código 1 si alguna falla):

```bash
java -cp out pruebas.Pruebas
```

## Mediciones

La clase `mediciones.Mediciones` genera estudiantes al azar (cada uno practica de 1 a 3 de 300 deportes) y mide la búsqueda por ID, registrar y eliminar un estudiante, la búsqueda de conexión en el peor caso y el cálculo de comunidades, para n entre 10 000 y 320 000. Tarda unos tres minutos:

```bash
java -cp out mediciones.Mediciones
python3 mediciones/graficar.py
```

El primer comando deja `mediciones/resultados.csv` (el valor de la ronda más rápida) y `mediciones/rondas.csv` (todas las rondas); el segundo dibuja `mediciones/tiempos.png`. Los resultados guardados se tomaron en un portátil con otro programa ocupando gran parte del procesador, así que son preliminares; para la Entrega 2 se repetirán con el equipo libre.

## Formato del archivo de datos

Una línea por estudiante, con cuatro campos separados por punto y coma. Los deportes de cada lista van separados por coma y cualquiera de las dos listas puede quedar vacía. Las líneas vacías y las que empiezan con `#` se ignoran.

```
id;nombre;deportes que practica;deportes que le interesan
1001;Ana Torres;Fútbol,Atletismo;Natación
1016;Felipe Ramírez;;Baloncesto
```

Los nombres de los deportes se comparan sin tener en cuenta mayúsculas, tildes ni espacios sobrantes, así que `Fútbol`, `futbol` y ` FUTBOL ` son el mismo deporte. Si una línea tiene un error (ID repetido, ID que no es número, nombre vacío), se informa el número de línea y se sigue con las demás.

## Estructura del proyecto

```
Proyecto-ED/
├── README.md
├── datos/
│   └── estudiantes.txt        16 estudiantes de ejemplo
├── mediciones/
│   ├── graficar.py            dibuja tiempos.png a partir de resultados.csv
│   ├── resultados.csv
│   ├── rondas.csv
│   └── tiempos.png
└── src/
    ├── estructuras/           estructuras de datos propias (no dependen del problema)
    │   ├── DLL.java           lista doblemente enlazada
    │   ├── DLLNode.java       nodo de la lista
    │   ├── MyQueue.java       interfaz de cola
    │   ├── Queue.java         cola con arreglo circular que crece al llenarse
    │   └── ArbolAVL.java      árbol AVL de clave y valor
    ├── deportes/              lógica del problema
    │   ├── Estudiante.java
    │   ├── Deporte.java
    │   ├── ClaveRanking.java  orden de los deportes por cantidad de practicantes
    │   ├── Conexion.java      resultado de la búsqueda de conexión
    │   └── SistemaDeportes.java
    ├── app/
    │   └── Main.java          menú de consola y carga del archivo
    ├── pruebas/
    │   └── Pruebas.java       casos de prueba sin librerías externas
    └── mediciones/
        └── Mediciones.java    tiempos de las operaciones con datos al azar
```

## Estructuras usadas en esta entrega

| Dato | Estructura | Costo de las operaciones principales |
|---|---|---|
| Estudiantes por ID | Árbol AVL `ArbolAVL<Integer, Estudiante>` | buscar, insertar y eliminar en O(log n) |
| Deportes por nombre | Árbol AVL `ArbolAVL<String, Deporte>` | buscar o crear en O(log d) |
| Deportes ordenados por practicantes | Árbol AVL con clave (cantidad, nombre) | actualizar en O(log d), recorrer en orden en O(d) |
| Practicantes de cada deporte | Lista doblemente enlazada | agregar y quitar en O(1) con la referencia al nodo |
| Deportes de cada estudiante | Lista doblemente enlazada | recorrer en O(p) |
| Recorrido de comunidades y conexiones | Cola | encolar y desencolar en O(1) |

Aquí n es la cantidad de estudiantes, d la de deportes y p la de deportes que practica un estudiante. Las comunidades y la búsqueda de conexión son recorridos por anchura sobre los estudiantes y los deportes que practican, con costo O(n + m), donde m es el total de parejas (estudiante, deporte que practica). La búsqueda por anchura encuentra primero al estudiante con menos intermediarios. El informe de la entrega explica la elección de cada estructura y las alternativas que se descartaron.

## Próximas entregas

En la Entrega 2 se completará la implementación con los temas vistos hasta árboles AVL y se repetirán las mediciones con más cuidado, comparando el AVL con una lista y con un árbol binario de búsqueda sin balancear. Para la Entrega 3 se planea cambiar el acceso por ID a una tabla hash, representar las relaciones como un grafo explícito con listas de adyacencia y comparar las comunidades calculadas con recorridos contra conjuntos disjuntos.
