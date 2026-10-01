#!/usr/bin/env python3
"""Suite de aceptación A01-A27 para Praxthon Sandbox SPEI.

Solo usa la biblioteca estándar de Python. No requiere requests ni pytest.
"""

from __future__ import annotations

import argparse
import json
import sys
import time
import urllib.error
import urllib.request
import uuid
from dataclasses import dataclass
from decimal import Decimal
from pathlib import Path
from typing import Any, Callable


def configure_console() -> None:
    for stream in (sys.stdout, sys.stderr):
        reconfigure = getattr(stream, "reconfigure", None)
        if callable(reconfigure):
            reconfigure(encoding="utf-8", errors="replace")


configure_console()


@dataclass
class CaseResult:
    case_id: str
    title: str
    passed: bool
    detail: str
    duration_ms: int


class ApiResponse:
    def __init__(self, status: int, body: Any, headers: dict[str, str]):
        self.status = status
        self.body = body
        self.headers = headers


class ApiClient:
    def __init__(self, base_url: str, timeout: float):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    def request(
        self,
        method: str,
        path: str,
        *,
        body: Any | None = None,
        headers: dict[str, str] | None = None,
        case_id: str | None = None,
    ) -> ApiResponse:
        request_headers = {
            "Accept": "application/json",
            **(headers or {}),
        }
        if case_id:
            request_headers["X-Test-Case"] = case_id
        data = None
        if body is not None:
            request_headers["Content-Type"] = "application/json"
            data = json.dumps(body, ensure_ascii=False).encode("utf-8")

        request = urllib.request.Request(
            f"{self.base_url}{path}",
            data=data,
            headers=request_headers,
            method=method,
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                raw = response.read().decode("utf-8", errors="replace")
                parsed = json.loads(raw) if raw else None
                return ApiResponse(
                    response.status,
                    parsed,
                    dict(response.headers.items()),
                )
        except urllib.error.HTTPError as error:
            raw = error.read().decode("utf-8", errors="replace")
            try:
                parsed = json.loads(raw) if raw else None
            except json.JSONDecodeError:
                parsed = {"raw": raw}
            return ApiResponse(
                error.code,
                parsed,
                dict(error.headers.items()),
            )


class Assertions:
    @staticmethod
    def equal(actual: Any, expected: Any, label: str) -> None:
        if actual != expected:
            raise AssertionError(
                f"{label}: esperado={expected!r}, obtenido={actual!r}"
            )

    @staticmethod
    def true(condition: bool, message: str) -> None:
        if not condition:
            raise AssertionError(message)

    @staticmethod
    def error_codes(response: ApiResponse, expected: set[str]) -> None:
        body = response.body or {}
        errors = body.get("errores", []) if isinstance(body, dict) else []
        actual = {
            item.get("codigo")
            for item in errors
            if isinstance(item, dict) and item.get("codigo")
        }

        if actual != expected:
            raise AssertionError(
                "Códigos diferentes: "
                f"esperados={sorted(expected)}, "
                f"obtenidos={sorted(actual)}"
            )


def deep_copy(value: Any) -> Any:
    return json.loads(json.dumps(value))


def unique_reference(case_id: str) -> str:
    timestamp_milliseconds = int(
        time.time() * 1000
    )

    return f"{case_id}{timestamp_milliseconds}"


def valid_t2t(case_id: str) -> dict[str, Any]:
    return {
        "tipoOperacion": "T2T",
        "referenciaSeguimiento": unique_reference(case_id),
        "importe": {
            "valor": 1500.50,
            "divisa": "MXN",
        },
        "emisor": {
            "institucion": "801",
            "cuenta": "801180000118359717",
            "nombre": "Ana Ruiz Delgado",
            "identificacionFiscal": "RUDA900112HN4",
        },
        "receptor": {
            "institucion": "802",
            "cuenta": "802180000200030011",
            "nombre": "Luis Cano Mora",
        },
        "concepto": "Pago de servicios",
        "folioNumerico": 4821,
    }


def valid_vnt(case_id: str) -> dict[str, Any]:
    return {
        "tipoOperacion": "VNT",
        "referenciaSeguimiento": unique_reference(case_id),
        "importe": {
            "valor": 3200.00,
            "divisa": "MXN",
        },
        "emisor": {
            "institucion": "801",
            "sucursal": "0417",
            "nombre": "Marta Solis Vega",
            "documentoIdentidad": {
                "tipo": "INE",
                "numero": "IDMEX1734558",
            },
        },
        "receptor": {
            "institucion": "803",
            "cuenta": "803180000900050028",
            "nombre": "Comercial Gamma",
        },
        "concepto": "Deposito en ventanilla",
        "folioNumerico": 1190,
    }


def operation_id(response: ApiResponse) -> str:
    Assertions.true(isinstance(response.body, dict), "Response no es objeto JSON")
    value = response.body.get("id")
    Assertions.true(bool(value), "El response no contiene id")
    return str(value)


def create_operation(
    client: ApiClient,
    case_id: str,
    body: dict[str, Any],
    *,
    key: str | None = None,
    scenario: str | None = None,
) -> ApiResponse:
    headers = {
        "Clave-Idempotencia": key or str(uuid.uuid4()),
    }
    if scenario:
        headers["X-Escenario-Forzado"] = scenario
    return client.request(
        "POST",
        "/operaciones",
        body=body,
        headers=headers,
        case_id=case_id,
    )


def wait_for_status(
    client: ApiClient,
    case_id: str,
    operation: str,
    expected: set[str],
    attempts: int,
    delay: float,
) -> ApiResponse:
    response: ApiResponse | None = None
    for _ in range(attempts):
        response = client.request(
            "GET",
            f"/operaciones/{operation}",
            case_id=case_id,
        )
        if response.status == 200 and isinstance(response.body, dict):
            if response.body.get("estado") in expected:
                return response
        time.sleep(delay)
    assert response is not None
    obtained = response.body.get("estado") if isinstance(response.body, dict) else None
    raise AssertionError(
        f"No alcanzó {sorted(expected)}; último estado={obtained!r}"
    )


def assert_transition_code(response: ApiResponse, code: str) -> None:
    body = response.body or {}
    transitions = body.get("transiciones", []) if isinstance(body, dict) else []
    reasons = {
        transition.get("motivo")
        for transition in transitions
        if isinstance(transition, dict) and transition.get("motivo")
    }
    Assertions.true(
        code in reasons,
        f"No se encontró {code} en transiciones; motivos={sorted(reasons)}",
    )


def accepted_case(
    client: ApiClient,
    case_id: str,
    body: dict[str, Any],
    final_status: str,
    scenario: str,
    context: dict[str, Any],
    transition_code: str | None = None,
) -> None:
    response = create_operation(
        client,
        case_id,
        body,
        scenario=scenario,
    )
    if response.status != 201:
        raise AssertionError(
            "HTTP inicial: "
            f"esperado=201, "
            f"obtenido={response.status}, "
            f"response={json.dumps(response.body, ensure_ascii=False)}"
        )
    Assertions.equal(response.body.get("estado"), "RECIBIDO", "estado inicial")
    op_id = operation_id(response)
    final = wait_for_status(
        client,
        case_id,
        op_id,
        {final_status},
        context["poll_attempts"],
        context["poll_delay"],
    )
    if transition_code:
        assert_transition_code(final, transition_code)
    context[f"operation_{case_id}"] = op_id


def rejected_case(
    client: ApiClient,
    case_id: str,
    body: dict[str, Any],
    status: int,
    codes: set[str],
) -> None:
    response = create_operation(client, case_id, body)
    Assertions.equal(response.status, status, "HTTP")
    Assertions.error_codes(response, codes)


def build_cases() -> list[tuple[str, str, Callable]]:
    cases: list[tuple[str, str, Callable]] = []

    cases.append(("A01", "T2T válida -> LIQUIDADO", lambda c, x: accepted_case(c, "A01", valid_t2t("A01"), "LIQUIDADO", "S01", x)))
    cases.append(("A02", "VNT válida -> LIQUIDADO", lambda c, x: accepted_case(c, "A02", valid_vnt("A02"), "LIQUIDADO", "S01", x)))

    def a03(c, x):
        body = valid_t2t("A03")
        del body["emisor"]["cuenta"]
        rejected_case(c, "A03", body, 422, {"PRX-011"})
    cases.append(("A03", "T2T sin emisor.cuenta -> PRX-011", a03))

    def a04(c, x):
        body = valid_vnt("A04")
        body["emisor"]["cuenta"] = "801180000118359717"
        rejected_case(c, "A04", body, 422, {"PRX-012"})
    cases.append(("A04", "VNT con emisor.cuenta -> PRX-012", a04))

    def a05(c, x):
        body = valid_vnt("A05")
        del body["emisor"]["sucursal"]
        rejected_case(c, "A05", body, 422, {"PRX-011"})
    cases.append(("A05", "VNT sin sucursal -> PRX-011", a05))

    def a06(c, x):
        body = valid_t2t("A06")
        body["receptor"]["cuenta"] = "80218000020003001"
        rejected_case(c, "A06", body, 422, {"PRX-001"})
    cases.append(("A06", "Cuenta de 17 dígitos -> PRX-001", a06))

    def a07(c, x):
        body = valid_t2t("A07")
        body["receptor"]["cuenta"] = "802180000200030012"
        rejected_case(c, "A07", body, 422, {"PRX-002"})
    cases.append(("A07", "Dígito verificador incorrecto -> PRX-002", a07))

    def a08(c, x):
        body = valid_t2t("A08")
        body["receptor"]["institucion"] = "899"
        rejected_case(c, "A08", body, 422, {"PRX-003"})
    cases.append(("A08", "Institución 899 -> PRX-003", a08))

    def a09(c, x):
        body = valid_t2t("A09")
        body["receptor"]["institucion"] = "803"
        rejected_case(c, "A09", body, 422, {"PRX-030"})
    cases.append(("A09", "Prefijo distinto de institución -> PRX-030", a09))

    def amount_case(case_id: str, value: Any, code: str):
        def run(c, x):
            body = valid_t2t(case_id)
            body["importe"]["valor"] = value
            rejected_case(c, case_id, body, 422, {code})
        return run

    cases.append(("A10", "Importe cero -> PRX-004", amount_case("A10", 0, "PRX-004")))
    cases.append(("A11", "Importe negativo -> PRX-004", amount_case("A11", -10, "PRX-004")))
    cases.append(("A12", "Tres decimales -> PRX-005", amount_case("A12", 10.123, "PRX-005")))
    cases.append(("A13", "Importe sobre límite -> PRX-005", amount_case("A13", 1000000.01, "PRX-005")))

    def a14(c, x):
        body = valid_t2t("A14")
        body["importe"]["divisa"] = "USD"
        rejected_case(c, "A14", body, 422, {"PRX-006"})
    cases.append(("A14", "Divisa USD -> PRX-006", a14))

    def a15(c, x):
        body = valid_t2t("A15")
        body["concepto"] = ""
        body["folioNumerico"] = 0
        rejected_case(c, "A15", body, 422, {"PRX-007", "PRX-008"})
    cases.append(("A15", "Errores acumulados -> PRX-007 + PRX-008", a15))

    def a16(c, x):
        body = valid_t2t("A16")
        first = create_operation(c, "A16", body)
        if first.status != 201:
            raise AssertionError(
                "HTTP primera referencia: "
                f"esperado=201, "
                f"obtenido={first.status}, "
                f"response={json.dumps(first.body, ensure_ascii=False)}"
            )
        second = create_operation(c, "A16", body, key=str(uuid.uuid4()))
        Assertions.equal(second.status, 422, "HTTP referencia repetida")
        Assertions.error_codes(second, {"PRX-010"})
    cases.append(("A16", "Referencia repetida -> PRX-010", a16))

    def a17(c, x):
        body = valid_t2t("A17")
        body["receptor"]["institucion"] = body["emisor"]["institucion"]
        body["receptor"]["cuenta"] = body["emisor"]["cuenta"]
        rejected_case(c, "A17", body, 422, {"PRX-013"})
    cases.append(("A17", "Emisor igual a receptor -> PRX-013", a17))

    cases.append(("A18", "Escenario fondos insuficientes -> PRX-020", lambda c, x: accepted_case(c, "A18", valid_t2t("A18"), "DEVUELTO", "S02", x, "PRX-020")))
    cases.append(("A19", "Escenario cuenta inexistente -> PRX-021", lambda c, x: accepted_case(c, "A19", valid_t2t("A19"), "DEVUELTO", "S03", x, "PRX-021")))

    def a20(c, x):
        body = valid_t2t("A20")
        body["receptor"]["institucion"] = "805"
        body["receptor"]["cuenta"] = "805180000100010002"
        accepted_case(c, "A20", body, "DEVUELTO", "S04", x, "PRX-022")
    cases.append(("A20", "Institución 805 -> PRX-022", a20))

    def a21(c, x):
        # El contrato público no expone una transición manual. La cobertura real
        # de PRX-014 corresponde a JUnit. El orquestador establece este indicador
        # únicamente cuando mvn test fue exitoso.
        Assertions.true(
            bool(x.get("junit_a21")),
            "A21 requiere JUnit exitoso o un endpoint de transición de pruebas",
        )
    cases.append(("A21", "LIQUIDADO -> DEVUELTO -> PRX-014 (JUnit)", a21))

    def a22(c, x):
        response = c.request("GET", "/operaciones/op_inexistente", case_id="A22")
        Assertions.equal(response.status, 404, "HTTP ID inexistente")
    cases.append(("A22", "ID inexistente -> HTTP 404", a22))

    def a23(c, x):
        response = c.request("GET", "/operaciones?pagina=0&tamano=20", case_id="A23")
        Assertions.equal(response.status, 200, "HTTP listado")
        body = response.body or {}
        total = body.get("totalElementos")
        Assertions.true(isinstance(total, int) and total >= 120, f"Se esperaban al menos 120, obtenido={total}")
        Assertions.equal(body.get("pagina"), 0, "página")
        Assertions.equal(body.get("tamano"), 20, "tamaño")
    cases.append(("A23", "Listado paginado con al menos 120 registros", a23))

    def a24(c, x):
        body = valid_t2t("A24")
        key = str(uuid.uuid4())
        first = create_operation(c, "A24", body, key=key)
        second = create_operation(c, "A24", body, key=key)
        if first.status != 201:
            raise AssertionError(
                "HTTP primer envío: "
                f"esperado=201, "
                f"obtenido={first.status}, "
                f"response={json.dumps(first.body, ensure_ascii=False)}"
            )
        Assertions.equal(second.status, 200, "HTTP reintento idempotente")
        Assertions.equal(operation_id(first), operation_id(second), "mismo ID")
    cases.append(("A24", "Misma clave y cuerpo -> 201 y 200", a24))

    def a25(c, x):
        first_body = valid_t2t("A25")
        second_body = deep_copy(first_body)
        second_body["concepto"] = "Cuerpo diferente"
        key = str(uuid.uuid4())
        first = create_operation(c, "A25", first_body, key=key)
        second = create_operation(c, "A25", second_body, key=key)
        if first.status != 201:
            raise AssertionError(
                "HTTP primer envío: "
                f"esperado=201, "
                f"obtenido={first.status}, "
                f"response={json.dumps(first.body, ensure_ascii=False)}"
            )
        Assertions.equal(second.status, 409, "HTTP conflicto")
        Assertions.error_codes(second, {"PRX-015"})
    cases.append(("A25", "Misma clave y cuerpo distinto -> PRX-015", a25))

    cases.append(("A26", "Sin respuesta -> EN_PROCESO / PRX-023", lambda c, x: accepted_case(c, "A26", valid_t2t("A26"), "EN_PROCESO", "S05", x, "PRX-023")))

    def a27(c, x):
        health = c.request("GET", "/salud", case_id="A27")
        Assertions.equal(health.status, 200, "HTTP salud")
        Assertions.equal(health.body.get("estado"), "UP", "estado salud")
        catalog = c.request("GET", "/catalogos/instituciones", case_id="A27")
        Assertions.equal(catalog.status, 200, "HTTP catálogo")
        Assertions.true(isinstance(catalog.body, list) and len(catalog.body) >= 5, "Se esperaban al menos cinco instituciones")
    cases.append(("A27", "Salud y catálogo", a27))

    return cases


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Suite de aceptación A01-A27")
    parser.add_argument("--base-url", default="http://localhost:8080/api/v1")
    parser.add_argument("--timeout", type=float, default=10.0)
    parser.add_argument("--poll-attempts", type=int, default=12)
    parser.add_argument("--poll-delay", type=float, default=0.5)
    parser.add_argument("--only", help="Caso individual, por ejemplo A10")
    parser.add_argument("--junit-a21", action="store_true", help="Marca A21 cubierto por JUnit exitoso")
    parser.add_argument("--json-report", type=Path)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    client = ApiClient(args.base_url, args.timeout)
    context = {
        "poll_attempts": args.poll_attempts,
        "poll_delay": args.poll_delay,
        "junit_a21": args.junit_a21,
    }
    selected = [case for case in build_cases() if not args.only or case[0] == args.only]
    if not selected:
        print(f"No existe el caso {args.only}", file=sys.stderr)
        return 2

    results: list[CaseResult] = []
    print(f"Ejecutando {len(selected)} casos contra {args.base_url}\n")
    for case_id, title, test in selected:
        start = time.perf_counter()
        try:
            test(client, context)
            passed = True
            detail = "OK"
        except Exception as error:
            passed = False
            detail = str(error)
        duration_ms = int((time.perf_counter() - start) * 1000)
        results.append(CaseResult(case_id, title, passed, detail, duration_ms))
        marker = "OK" if passed else "FALLÓ"
        print(f"{case_id:<4} {title:<58} {marker} ({duration_ms} ms)")
        if not passed:
            print(f"     {detail}")

    passed_count = sum(result.passed for result in results)
    total = len(results)
    percentage = round((passed_count / total) * 100) if total else 0
    print(f"\n{passed_count}/{total} casos superados ({percentage}%)")

    if args.json_report:
        args.json_report.parent.mkdir(parents=True, exist_ok=True)
        args.json_report.write_text(
            json.dumps(
                {
                    "baseUrl": args.base_url,
                    "passed": passed_count,
                    "total": total,
                    "percentage": percentage,
                    "results": [result.__dict__ for result in results],
                },
                indent=4,
                ensure_ascii=False,
            ) + "\n",
            encoding="utf-8",
        )

    return 0 if passed_count == total else 1


if __name__ == "__main__":
    raise SystemExit(main())
