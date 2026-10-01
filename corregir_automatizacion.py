#!/usr/bin/env python3
"""Aplica correcciones a la suite Praxthon existente.

Ejecutar desde la carpeta que contiene:
- ejecutar_todo.py
- suite_aceptacion_A27.py
- mutantes.py
"""

from pathlib import Path
import py_compile

ROOT = Path.cwd()
SUITE = ROOT / "suite_aceptacion_A27.py"
MUTANTS = ROOT / "mutantes.py"
RUNNER = ROOT / "ejecutar_todo.py"

for file_path in (SUITE, MUTANTS, RUNNER):
    if not file_path.exists():
        raise SystemExit(f"No se encontro {file_path.name} en {ROOT}")

suite = SUITE.read_text(encoding="utf-8")
suite = suite.replace("→", "->")

console_code = (
    "\n\ndef configure_console() -> None:\n"
    "    for stream in (sys.stdout, sys.stderr):\n"
    "        reconfigure = getattr(stream, \"reconfigure\", None)\n"
    "        if callable(reconfigure):\n"
    "            reconfigure(encoding=\"utf-8\", errors=\"replace\")\n"
    "\n\nconfigure_console()\n"
)

if "def configure_console()" not in suite:
    marker = "from typing import Any, Callable\n"
    if marker not in suite:
        raise SystemExit(
            "No se encontro 'from typing import Any, Callable' en la suite."
        )
    suite = suite.replace(marker, marker + console_code, 1)

SUITE.write_text(suite, encoding="utf-8")

mutants = MUTANTS.read_text(encoding="utf-8")
old_expression = "was_detected = process.returncode != 0"
new_expression = (
    "was_detected = (\n"
    "                process.returncode == 1\n"
    "                and \"0/1 casos superados\" in process.stdout\n"
    "            )"
)

if old_expression in mutants:
    mutants = mutants.replace(old_expression, new_expression, 1)
elif "0/1 casos superados" not in mutants:
    raise SystemExit(
        "No se encontro la condicion esperada en mutantes.py."
    )

MUTANTS.write_text(mutants, encoding="utf-8")

runner = RUNNER.read_text(encoding="utf-8")
if "PYTHONUTF8" not in runner:
    process_marker = "process = subprocess.Popen(\n"
    replacement = (
        "process_environment = dict(os.environ)\n"
        "    process_environment[\"PYTHONUTF8\"] = \"1\"\n"
        "    process_environment[\"PYTHONIOENCODING\"] = \"utf-8\"\n\n"
        "    process = subprocess.Popen(\n"
    )
    if process_marker in runner and "import os" in runner:
        runner = runner.replace(process_marker, replacement, 1)
        popen_marker = "errors=\"replace\",\n        )"
        popen_replacement = (
            "errors=\"replace\",\n"
            "            env=process_environment,\n"
            "        )"
        )
        runner = runner.replace(popen_marker, popen_replacement, 1)

RUNNER.write_text(runner, encoding="utf-8")

for file_path in (SUITE, MUTANTS, RUNNER):
    py_compile.compile(str(file_path), doraise=True)

print("Correcciones aplicadas correctamente:")
print("- Consola Python configurada en UTF-8")
print("- Flechas Unicode reemplazadas por ->")
print("- Crashes ya no cuentan como mutantes detectados")
print("- Sintaxis de los tres scripts validada")
