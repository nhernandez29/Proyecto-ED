# Grafica mediciones/resultados.csv en mediciones/tiempos.png.
# Uso (desde la raíz del repositorio): python3 mediciones/graficar.py   (necesita matplotlib)
import csv
import os

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

CARPETA = os.path.dirname(os.path.abspath(__file__))

with open(os.path.join(CARPETA, "resultados.csv")) as archivo:
    filas = list(csv.DictReader(archivo))
n = [int(f["n"]) for f in filas]
nm_total = [int(f["n"]) + int(f["m"]) for f in filas]  # estudiantes más relaciones
buscar = [float(f["buscar_ns"]) for f in filas]
conexion = [float(f["conexion_ms"]) for f in filas]
comunidades = [float(f["comunidades_ms"]) for f in filas]

plt.rcParams.update({"font.size": 8, "font.family": "Arial"})
fig, (izq, der) = plt.subplots(1, 2, figsize=(8.8 / 2.54, 5.4 / 2.54))

izq.plot(n, buscar, "o-", color="#4F6D8F", ms=3, lw=1)
izq.set_xscale("log", base=2)
izq.set_xticks(n)
izq.set_xticklabels([str(x // 1000) for x in n])
izq.minorticks_off()
izq.set_ylim(0, max(buscar) * 1.25)
izq.set_xlabel("n (miles de estudiantes)")
izq.set_ylabel("ns por búsqueda")
izq.set_title("(a) Búsqueda por ID", fontsize=8)

# tiempo por estudiante o relación: si el costo es O(n + m), esta curva es horizontal
por_elemento_conexion = [t * 1e6 / x for t, x in zip(conexion, nm_total)]
por_elemento_comunidades = [t * 1e6 / x for t, x in zip(comunidades, nm_total)]
der.plot(n, por_elemento_conexion, "o-", color="#B5651D", ms=3, lw=1, label="conexión")
der.plot(n, por_elemento_comunidades, "s-", color="#5B8A3C", ms=2.6, lw=1, label="comunidades")
der.set_xscale("log", base=2)
der.set_xticks(n)
der.set_xticklabels([str(x // 1000) for x in n])
der.minorticks_off()
der.set_ylim(0, max(por_elemento_comunidades + por_elemento_conexion) * 1.25)
der.set_xlabel("n (miles de estudiantes)")
der.set_ylabel("ns por estudiante o relación")
der.set_title("(b) Recorridos por anchura", fontsize=8)
der.legend(frameon=False, fontsize=7, loc="upper left", handlelength=1.6)

for ax in (izq, der):
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.tick_params(length=2, pad=1.5)
    ax.grid(alpha=0.25, lw=0.5)
fig.tight_layout(pad=0.3, w_pad=0.8)
fig.savefig(os.path.join(CARPETA, "tiempos.png"), dpi=300)
print("Gráfica en", os.path.join(CARPETA, "tiempos.png"))
