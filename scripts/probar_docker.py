"""Prueba las APIs en un servicio ya iniciado, sin dependencias de Python externas."""
import argparse
import json
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4


def pedir(base_url, metodo, ruta, esperado, datos=None):
    payload = None if datos is None else json.dumps(datos).encode("utf-8")
    request = Request(base_url + ruta, data=payload, method=metodo,
                      headers={"Content-Type": "application/json"})
    try:
        with urlopen(request, timeout=20) as response:
            status, body = response.status, response.read()
    except HTTPError as error:
        status, body = error.code, error.read()
    if status != esperado:
        raise AssertionError(f"{metodo} {ruta}: esperado {esperado}, recibido {status}: {body.decode()}")
    return json.loads(body) if body else None


def comprobar(base_url, estado):
    producto = pedir(base_url, "GET", f"/api/productos/{estado['producto']}", 200)
    assert producto["nombre"] == estado["nombre"]
    assert producto["stock"] == 28, producto
    venta = pedir(base_url, "GET", f"/api/ventas/{estado['venta']}", 200)
    assert venta["total"] == 10, venta
    assert venta["detalles"][0]["producto"]["nombre"] == estado["nombre"], venta
    assert venta["detalles"][0]["subtotal"] == 10, venta
    cliente = pedir(base_url, "GET", f"/api/v1/clientes/{estado['cliente']}", 200)
    assert cliente["dni"] == estado["dni"], cliente
    detalles = pedir(base_url, "GET", "/api/detalle-ventas", 200)
    assert any(d["id"] == estado["detalle"] and d["cantidad"] == 2 for d in detalles), detalles


def limpiar(base_url, estado):
    pedir(base_url, "DELETE", f"/api/ventas/{estado['venta']}", 204)
    producto = pedir(base_url, "GET", f"/api/productos/{estado['producto']}", 200)
    assert producto["stock"] == 30, producto
    pedir(base_url, "DELETE", f"/api/v1/clientes/{estado['cliente']}", 204)
    pedir(base_url, "DELETE", f"/api/productos/{estado['producto']}", 204)
    print("OK: se devolvió el stock y se limpiaron los registros de la prueba (producto desactivado).")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default="http://localhost:8081", help="URL base de la API")
    modo = parser.add_mutually_exclusive_group()
    modo.add_argument("--guardar", type=Path, help="Conservar datos e IDs para comprobarlos tras un reinicio")
    modo.add_argument("--verificar", type=Path, help="Verificar esos IDs después del reinicio y limpiar la prueba")
    args = parser.parse_args()
    base_url = args.url.rstrip("/")

    if args.verificar:
        estado = json.loads(args.verificar.read_text(encoding="utf-8"))
        comprobar(base_url, estado)
        print("OK: productos, clientes, venta, detalles y stock persistieron después del reinicio.")
        limpiar(base_url, estado)
        return

    nombre = "Docker prueba " + uuid4().hex[:12]
    producto = pedir(base_url, "POST", "/api/productos", 201, {
        "nombre": nombre, "descripcion": "Comprobación automática de Docker",
        "precio": 5, "stock": 30, "categoria": "Pruebas"
    })
    dni = f"{uuid4().int % 100000000:08d}"
    cliente = pedir(base_url, "POST", "/api/v1/clientes", 201, {
        "nombre": "Prueba Docker", "apellido": "Integración", "dni": dni,
        "email": "docker@example.com", "telefono": "987654321"
    })
    venta = pedir(base_url, "POST", "/api/ventas", 201, {
        "total": 0.01,
        "detalles": [{"producto": {"id": producto["id"]}, "cantidad": 2,
                      "precioUnitario": 0.01, "subtotal": 0.01}]
    })
    assert venta["total"] == 10, venta
    assert venta["detalles"][0]["producto"]["stock"] == 28, venta
    assert venta["detalles"][0]["precioUnitario"] == 5, venta

    for cantidad, status in [(29, 409), (0, 400)]:
        pedir(base_url, "POST", "/api/ventas", status, {
            "detalles": [{"producto": {"id": producto["id"]}, "cantidad": cantidad}]
        })
    estado = {"producto": producto["id"], "venta": venta["id"], "cliente": cliente["id"],
              "detalle": venta["detalles"][0]["id"], "nombre": nombre, "dni": dni}
    comprobar(base_url, estado)
    print("OK: los cuatro módulos responden; venta calculada, stock 30 -> 28 y rechazos 409/400 sin alterar stock.")
    if args.guardar:
        args.guardar.parent.mkdir(parents=True, exist_ok=True)
        args.guardar.write_text(json.dumps(estado, indent=2), encoding="utf-8")
        print(f"IDs conservados en {args.guardar}. Reinicia los contenedores y ejecuta --verificar.")
    else:
        limpiar(base_url, estado)


if __name__ == "__main__":
    main()
