#!/usr/bin/env python3
"""Ejecuta aceptación, mutantes y Newman contra un backend ya iniciado.

El script NO compila, NO genera el JAR, NO inicia Java y NO detiene el backend.
La aplicación debe estar ejecutándose previamente desde IntelliJ IDEA.
"""

from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path


def run_command(
    command: list[str],
    working_directory: Path,
    log_file: Path,
) -> int:
    print("\n$", subprocess.list2cmdline(command))

    with log_file.open("w", encoding="utf-8") as output:
        process = subprocess.Popen(
            command,
            cwd=working_directory,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
        )

        assert process.stdout is not None

        for line in process.stdout:
            print(line, end="")
            output.write(line)

        return process.wait()


def check_health(
    url: str,
    attempts: int,
    delay_seconds: float,
) -> tuple[bool, str]:
    last_detail = ""

    for attempt in range(1, attempts + 1):
        try:
            with urllib.request.urlopen(url, timeout=4) as response:
                raw = response.read().decode("utf-8", errors="replace")
                body = json.loads(raw)

                if response.status == 200 and body.get("estado") == "UP":
                    return True, raw

                last_detail = (
                    f"HTTP {response.status}, estado={body.get('estado')!r}"
                )
        except (
            urllib.error.URLError,
            TimeoutError,
            json.JSONDecodeError,
        ) as error:
            last_detail = str(error)

        print(
            f"Backend no disponible todavía "
            f"({attempt}/{attempts}): {last_detail}"
        )
        time.sleep(delay_seconds)

    return False, last_detail


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Ejecuta A01-A27, mutantes y Newman contra el backend "
            "que ya corre en IntelliJ IDEA."
        ),
    )
    parser.add_argument(
        "--project",
        type=Path,
        default=Path.cwd(),
        help="Raíz del proyecto donde se guardará resultados/.",
    )
    parser.add_argument(
        "--base-url",
        default="http://localhost:8080/api/v1",
        help="URL base del backend iniciado en IntelliJ IDEA.",
    )
    parser.add_argument(
        "--collection",
        type=Path,
        help="Colección Postman JSON.",
    )
    parser.add_argument(
        "--environment",
        type=Path,
        help="Environment Postman JSON.",
    )
    parser.add_argument(
        "--skip-mutations",
        action="store_true",
        help="Omite los mutantes contractuales.",
    )
    parser.add_argument(
        "--skip-newman",
        action="store_true",
        help="Omite Newman.",
    )
    parser.add_argument(
        "--junit-a21",
        action="store_true",
        help=(
            "Marca A21 como cubierto por una prueba JUnit ejecutada "
            "previamente desde IntelliJ IDEA o Maven."
        ),
    )
    parser.add_argument(
        "--health-attempts",
        type=int,
        default=5,
        help="Intentos para verificar /salud.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_arguments()
    project = args.project.resolve()
    script_directory = Path(__file__).resolve().parent
    acceptance_suite = script_directory / "suite_aceptacion_A27.py"
    mutation_suite = script_directory / "mutantes.py"

    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    report_directory = project / "resultados" / timestamp
    report_directory.mkdir(parents=True, exist_ok=True)

    print("=" * 72)
    print("PRAXTHON SANDBOX - AUTOMATIZACIÓN CONTRA BACKEND DE INTELLIJ")
    print("=" * 72)
    print("Backend esperado:", args.base_url)
    print("Resultados:", report_directory)
    print("El script no iniciará ni detendrá el backend.\n")

    healthy, health_detail = check_health(
        f"{args.base_url.rstrip('/')}/salud",
        attempts=args.health_attempts,
        delay_seconds=1.0,
    )

    health_report = {
        "success": healthy,
        "url": f"{args.base_url.rstrip('/')}/salud",
        "detail": health_detail,
    }
    (report_directory / "salud.json").write_text(
        json.dumps(health_report, indent=4, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )

    if not healthy:
        print("\nERROR: el backend no responde UP.")
        print("Inicia SpeisandboxApplication en IntelliJ IDEA y vuelve a ejecutar.")
        return 1

    steps: list[tuple[str, int]] = []
    acceptance_command = [
        sys.executable,
        str(acceptance_suite),
        "--base-url",
        args.base_url,
        "--json-report",
        str(report_directory / "aceptacion.json"),
    ]

    if args.junit_a21:
        acceptance_command.append("--junit-a21")

    acceptance_code = run_command(
        acceptance_command,
        project,
        report_directory / "aceptacion.log",
    )
    steps.append(("Suite A01-A27", acceptance_code))

    if not args.skip_mutations:
        mutation_command = [
            sys.executable,
            str(mutation_suite),
            "--suite",
            str(acceptance_suite),
            "--target-url",
            args.base_url,
        ]

        mutation_code = run_command(
            mutation_command,
            project,
            report_directory / "mutantes.log",
        )
        steps.append(("Mutantes contractuales", mutation_code))

    if not args.skip_newman:
        if not args.collection:
            print("\nNewman omitido: no se indicó --collection.")
        elif not shutil.which("newman"):
            print("\nERROR: Newman no está instalado o no está en PATH.")
            print("Instala con: npm install --global newman")
            steps.append(("Newman", 127))
        else:
            newman_command = [
                "newman",
                "run",
                str(args.collection.resolve()),
                "--reporters",
                "cli,json,junit",
                "--reporter-json-export",
                str(report_directory / "newman.json"),
                "--reporter-junit-export",
                str(report_directory / "newman.xml"),
            ]

            if args.environment:
                newman_command.extend(
                    [
                        "--environment",
                        str(args.environment.resolve()),
                    ]
                )

            newman_code = run_command(
                newman_command,
                project,
                report_directory / "newman.log",
            )
            steps.append(("Newman", newman_code))

    summary = {
        "backendManagedByScript": False,
        "baseUrl": args.base_url,
        "success": all(code == 0 for _, code in steps),
        "steps": [
            {
                "name": name,
                "exitCode": code,
                "success": code == 0,
            }
            for name, code in steps
        ],
    }
    (report_directory / "resumen.json").write_text(
        json.dumps(summary, indent=4, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )

    print("\n" + "=" * 72)
    print("RESUMEN")
    print("=" * 72)

    for name, code in steps:
        print(f"{'OK' if code == 0 else 'FALLÓ':<8} {name}")

    print("\nEl backend de IntelliJ IDEA permanece en ejecución.")
    print("Evidencia:", report_directory)

    return 0 if all(code == 0 for _, code in steps) else 1


if __name__ == "__main__":
    raise SystemExit(main())
