# -*- coding: utf-8 -*-
"""Genera Diagrama_Secuencia_Consulta.pdf: diagrama de secuencia UML del flujo
consulta natural -> SQL -> JSON integrando el microservicio Python.

Uso: python generate_diagrama_secuencia.py
"""
from pathlib import Path

from fpdf import FPDF

SCRIPT_DIR = Path(__file__).resolve().parent
OUT = SCRIPT_DIR / "Diagrama_Secuencia_Consulta.pdf"

W = 297
H = 210
ML = 12
MR = 12
CW = W - ML - MR

FONT = "arial"
MONO = "consola"

C_BLUE = (23, 55, 94)
C_LIGHT = (222, 232, 244)
C_LLAMA = (255, 255, 255)
C_NOTE = (255, 248, 220)
C_EDGE = (130, 140, 155)

pdf = FPDF(orientation="L", unit="mm", format="A4")
pdf.set_auto_page_break(False)
pdf.add_font(FONT, "", r"C:\Windows\Fonts\arial.ttf")
pdf.add_font(FONT, "B", r"C:\Windows\Fonts\arialbd.ttf")
pdf.add_font(FONT, "I", r"C:\Windows\Fonts\ariali.ttf")


def wrap(text, size, max_w):
    """Parte text en lineas que caben en max_w mm."""
    pdf.set_font(FONT, "", size)
    words = text.split()
    lines = []
    cur = ""
    for w in words:
        trial = (cur + " " + w).strip()
        if pdf.get_string_width(trial) <= max_w:
            cur = trial
        else:
            if cur:
                lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)
    return lines


def text_center(lines, cx, y_top, size, line_h, color=(0, 0, 0)):
    pdf.set_font(FONT, "", size)
    pdf.set_text_color(*color)
    for i, ln in enumerate(lines):
        tw = pdf.get_string_width(ln)
        pdf.text(cx - tw / 2, y_top + i * line_h, ln)


def draw_actor_box(xc, y, w, h, title, subtitle):
    pdf.set_fill_color(*C_BLUE)
    pdf.set_draw_color(*C_EDGE)
    pdf.rect(xc - w / 2, y, w, h, "DF")
    t_lines = wrap(title, 9, w - 4)
    tw = pdf.get_string_width(title)
    if len(t_lines) == 1 and tw <= w - 4:
        text_center([title], xc, y + h / 2 - 2.2, 9, 4, (255, 255, 255))
    else:
        lines = wrap(title + "  " + subtitle, 8, w - 3)
        text_center(lines, xc, y + h / 2 - (len(lines) / 2) * 3.2, 8, 3.2, (255, 255, 255))
        return True
    text_center([subtitle], xc, y + h / 2 + 2.0, 6, 3, (215, 226, 240))
    return False


def arrow(x1, y, x2, solid):
    end = x2 if x2 > x1 else x1
    start = x1 if x2 > x1 else x2
    pdf.set_draw_color(40, 40, 40)
    pdf.set_line_width(0.5)
    if solid:
        pdf.line(start, y, end, y)
        arrow_head(2 if x2 == end else 1, end, y)
    else:
        pdf.set_dash_pattern(1.5, 1.2)
        pdf.line(start, y, end, y)
        pdf.set_dash_pattern()
        arrow_head(2 if x2 == end else 1, end, y)


def arrow_head(direction, x, y):
    import math
    pdf.set_fill_color(40, 40, 40)
    s = 1.6
    if direction == 2:  # apuntando a la derecha
        pts = [(x - s, y - s), (x + s, y), (x - s, y + s)]
    else:  # apuntando a la izquierda
        pts = [(x + s, y - s), (x - s, y), (x + s, y + s)]
    poly = [(px, py) for px, py in pts]
    pdf.polygon(poly, style="F")


def draw_message(x1, x2, y, label, solid):
    mid = (x1 + x2) / 2
    span = abs(x2 - x1)
    lines = wrap(label, 7, span - 6)
    text_center(lines, mid, y - 1.6 - (len(lines) - 1) * 2.6, 7, 2.6, (40, 40, 40))
    arrow(min(x1, x2), y, max(x1, x2), solid or False)





# ------------------------------------------------------------------ paginas

pdf.add_page()

# Portada pequeña: titulo + imagen? No, diagrama directo en una pagina con cabecera.
pdf.set_fill_color(*C_LIGHT)
pdf.set_draw_color(*C_BLUE)
pdf.rect(0, 0, W, 12, "F")
pdf.set_font(FONT, "B", 10)
pdf.set_text_color(*C_BLUE)
pdf.text(ML, 8, "Diagrama de Secuencia UML  ·  Consulta Natural -> SQL -> JSON  (SaaS B2B de Datos)")
pdf.set_text_color(0)

actors = [
    ("Frontend", "Usuario web"),
    ("Spring Boot", "Backend corporativo"),
    ("Caché", "Esquema / tenant"),
    ("Servicio Python", "Microservicio IA"),
    ("API de IA", "LLM -> SQL"),
    ("BD Cliente", "PostgreSQL tenant"),
    ("BD SaaS", "Historial / límite"),
]
n = len(actors)
xc = [ML + CW * (i + 0.5) / n for i in range(n)]

by = 16
bh = 15
for i, (t, s) in enumerate(actors):
    draw_actor_box(xc[i], by, 30, bh, t, s)

lifeline_top = by + bh + 2
lifeline_bottom = 200
for cx in xc:
    pdf.set_draw_color(170, 178, 190)
    pdf.set_line_width(0.35)
    pdf.line(cx, lifeline_top, cx, lifeline_bottom)

msgs = [
    (54, 0, 1, True,  "1. POST /consultas (JWT): pregunta natural + tenantId + usuarioId"),
    (72, 1, 2, True,  "2. Leer esquema del tenant desde caché"),
    (83, 2, 1, False, "esquema (tablas / columnas)"),
    (95, 1, 3, True,  "3. Delega: esquema + pregunta"),
    (108, 3, 4, True,  "4. Compone prompt maestro y llama a la API de IA"),
    (121, 4, 3, False, "query SQL"),
    (134, 3, 1, False, "SQL generado"),
    (150, 1, 5, True,  "5. Ejecuta query  (conexión read-only)"),
    (162, 5, 1, False, "registros planos"),
    (175, 1, 6, True,  "6. Valida límite y registra historial"),
    (187, 6, 1, False, "guardado ok"),
    (196, 1, 0, False, "7. Respuesta JSON: columnas + filas"),
]

for y, a, b, solid, label in msgs:
    draw_message(xc[a], xc[b], y, label, solid)

# Nota de seguridad junto a Spring Boot (lado izquierdo, sin cruzar lifelines)
pdf.set_fill_color(*C_NOTE)
pdf.set_draw_color(*C_EDGE)
nw, nh, ny = 33, 14, 137
pdf.rect(xc[1] - 3 - nw, ny, nw, nh, "DF")
pdf.set_draw_color(60, 50, 20)
pdf.set_line_width(0.3)
pdf.line(xc[1], ny + nh / 2, xc[1] - 3, ny + nh / 2)
note_lines = ["En Spring Boot:", "validar SQL", "(solo SELECT,", "sin ; ni --)", "-> 400 si inválido"]
text_center(note_lines, xc[1] - 3 - nw / 2, ny + nh / 2 - (len(note_lines) / 2) * 2.4, 6.2, 2.4, (60, 50, 20))

# Leyenda
ly = 200
pdf.set_font(FONT, "", 7)
pdf.set_draw_color(40, 40, 40)
pdf.set_line_width(0.5)
pdf.line(ML + 24, ly - 2, ML + 40, ly - 2)
arrow_head(2, ML + 40, ly - 2)
pdf.text(ML + 43, ly - 1, "Síncrono / llamada")
pdf.set_dash_pattern(1.5, 1.2)
pdf.line(ML + 24, ly + 5, ML + 40, ly + 5)
pdf.set_dash_pattern()
arrow_head(2, ML + 40, ly + 5)
pdf.text(ML + 43, ly + 6, "Retorno / respuesta")

# ------------------------------------------------------------------ pagina 2: onboarding

pdf.add_page()

pdf.set_fill_color(*C_LIGHT)
pdf.set_draw_color(*C_BLUE)
pdf.rect(0, 0, W, 12, "F")
pdf.set_font(FONT, "B", 10)
pdf.set_text_color(*C_BLUE)
pdf.text(ML, 8, "Diagrama de Secuencia UML  ·  Alta self-serve + preparacion del esquema  (SaaS B2B de Datos)")
pdf.set_text_color(0)

actors2 = [
    ("Cliente web", "Registro + verificacion email"),
    ("Spring Boot", "Backend corporativo"),
    ("BD SaaS", "Tenant/usuario/suscripcion/conexion"),
    ("BD Cliente", "PostgreSQL tenant"),
    ("Caché", "Esquema / tenant"),
]
n2 = len(actors2)
xc2 = [ML + CW * (i + 0.5) / n2 for i in range(n2)]

for i, (t, s) in enumerate(actors2):
    draw_actor_box(xc2[i], by, 34, bh, t, s)

for cx in xc2:
    pdf.set_draw_color(170, 178, 190)
    pdf.set_line_width(0.35)
    pdf.line(cx, lifeline_top, cx, lifeline_bottom)

msgs2 = [
    (24, 0, 1, True,  "1. POST /self-serve/registro  (cliente + usuario + conexion)"),
    (37, 1, 2, True,  "2. valida slug/email unicos y localiza el plan gratuito"),
    (37, 2, 1, False, "sin duplicados / plan free OK"),
    (49, 1, 2, True,  "3. crea  Tenant ACTIVE + Usuario ADMIN + Suscripcion  (una transaccion)"),
    (61, 1, 2, True,  "4. cifra credenciales de BD (AES-GCM) y guarda TenantDbConnection"),
    (73, 1, 3, True,  "5. AFTER_COMMIT: extrae esquema vía information_schema (read-only)"),
    (73, 3, 1, False, "tablas / columnas / PK"),
    (85, 1, 4, True,  "6. cachea EsquemaTenant por tenantId"),
    (98, 1, 0, False, "7. 201: tenantId + usuarioId + suscripcionId + dbConnectionId + emailVerificacionUrl"),
    (114, 0, 1, True,  "8. GET /self-serve/verificar-email?token=..."),
    (126, 1, 2, True,  "9. valida firma HMAC + expiracion; marca email como verificado"),
    (138, 1, 0, False, "10. 200 OK: email verificado"),
]

for y, a, b, solid, label in msgs2:
    draw_message(xc2[a], xc2[b], y, label, solid)

# Nota de seguridad junto a Spring Boot (página 2)
pdf.set_fill_color(*C_NOTE)
pdf.set_draw_color(*C_EDGE)
nw, nh, ny = 36, 20, 88
pdf.rect(xc2[1] - 3 - nw, ny, nw, nh, "DF")
pdf.set_draw_color(60, 50, 20)
pdf.set_line_width(0.3)
pdf.line(xc2[1], ny + nh / 2, xc2[1] - 3, ny + nh / 2)
nota2 = ["En Spring Boot:", "rate limit por IP", "(429 al exceder 5/min),", "credenciales nunca",
         "en respuestas;", "clave AES-GCM en .env"]
text_center(nota2, xc2[1] - 3 - nw / 2, ny + nh / 2 - (len(nota2) / 2) * 2.4, 6.2, 2.4, (60, 50, 20))

pdf.output(OUT)
print(f"PDF generado: {OUT} · paginas: {pdf.page_no()}")