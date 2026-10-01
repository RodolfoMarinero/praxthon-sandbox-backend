#!/usr/bin/env python3
"""Mutación contractual mediante proxy HTTP controlado.

Cada mutante altera deliberadamente una respuesta. La suite debe fallar para
considerarlo detectado. El backend real no se modifica.
"""

from __future__ import annotations

import argparse
import http.client
import json
import subprocess
import sys
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlsplit


MUTANTS = [
    ("M01", "A10", "Acepta importe cero", "force_201"),
    ("M02", "A11", "Acepta importe negativo", "force_201"),
    ("M03", "A12", "Ignora tercer decimal", "force_201"),
    ("M04", "A14", "Permite USD", "force_201"),
    ("M05", "A15", "Elimina un error acumulado", "remove_last_error"),
    ("M06", "A16", "Permite referencia duplicada", "force_201"),
    ("M07", "A17", "Permite cuentas iguales", "force_201"),
    ("M08", "A22", "ID inexistente responde 200", "force_200"),
    ("M09", "A23", "Reduce total de registros", "total_zero"),
    ("M10", "A25", "Oculta conflicto de idempotencia", "force_200"),
]


class MutationProxy(BaseHTTPRequestHandler):
    target = None
    mutation = None
    target_case = None

    def do_GET(self):
        self.forward()

    def do_POST(self):
        self.forward()

    def log_message(self, format, *args):
        return

    def forward(self):
        parsed = urlsplit(self.target)
        length = int(self.headers.get("Content-Length", "0"))
        request_body = self.rfile.read(length) if length else None
        headers = {
            key: value
            for key, value in self.headers.items()
            if key.lower() not in {"host", "content-length", "accept-encoding"}
        }
        connection = http.client.HTTPConnection(parsed.hostname, parsed.port, timeout=15)
        connection.request(
            self.command,
            f"{parsed.path.rstrip('/')}{self.path}",
            body=request_body,
            headers=headers,
        )
        response = connection.getresponse()
        body = response.read()
        status = response.status
        content_type = response.getheader("Content-Type", "application/json")

        case_id = self.headers.get("X-Test-Case")
        if case_id == self.target_case:
            status, body = mutate(status, body, self.mutation)

        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)
        connection.close()


def mutate(status: int, raw: bytes, mutation: str) -> tuple[int, bytes]:
    try:
        body = json.loads(raw.decode("utf-8")) if raw else {}
    except json.JSONDecodeError:
        body = {}

    if mutation == "force_201":
        status = 201
        body = {
            "id": "op_mutante",
            "referenciaSeguimiento": "MUTANTE",
            "estado": "RECIBIDO",
            "tipoOperacion": "T2T",
            "importe": {"valor": 1, "divisa": "MXN"},
            "fechaRegistro": "2026-01-01T00:00:00Z",
            "transiciones": [],
        }
    elif mutation == "force_200":
        status = 200
        if not body:
            body = {"id": "op_mutante"}
    elif mutation == "remove_last_error":
        errors = body.get("errores", []) if isinstance(body, dict) else []
        if len(errors) > 1:
            body["errores"] = errors[:-1]
    elif mutation == "total_zero":
        if isinstance(body, dict):
            body["totalElementos"] = 0

    return status, json.dumps(body, ensure_ascii=False).encode("utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--suite", required=True)
    parser.add_argument("--target-url", default="http://localhost:8080/api/v1")
    parser.add_argument("--port", type=int, default=8091)
    args = parser.parse_args()

    detected = 0
    for mutant_id, case_id, title, mutation in MUTANTS:
        MutationProxy.target = args.target_url
        MutationProxy.mutation = mutation
        MutationProxy.target_case = case_id
        server = ThreadingHTTPServer(("127.0.0.1", args.port), MutationProxy)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            process = subprocess.run(
                [
                    sys.executable,
                    args.suite,
                    "--base-url",
                    f"http://127.0.0.1:{args.port}",
                    "--only",
                    case_id,
                    "--junit-a21",
                ],
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                encoding="utf-8",
                errors="replace",
            )
            was_detected = (
                process.returncode == 1
                and "0/1 casos superados" in process.stdout
            )
            if was_detected:
                detected += 1
            print(
                f"{mutant_id} {title:<42} "
                f"{'DETECTADO' if was_detected else 'SOBREVIVIÓ'}"
            )
            if not was_detected:
                print(process.stdout)
        finally:
            server.shutdown()
            server.server_close()
            thread.join(timeout=2)

    total = len(MUTANTS)
    percentage = round((detected / total) * 100)
    print(f"\nMutantes detectados: {detected}/{total} ({percentage}%)")
    return 0 if detected == total else 1


if __name__ == "__main__":
    raise SystemExit(main())
