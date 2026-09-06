# -*- coding: utf-8 -*-
"""Genera Arquitectura_Base_de_Datos_v2.pdf a partir del Markdown como unica fuente de la verdad.

Uso: python gen_pdf_from_md.py [ruta.md] [ruta_salida.pdf]
"""
import re
import sys
from pathlib import Path

from fpdf import FPDF

SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_MD = SCRIPT_DIR / "Arquitectura_Base_de_Datos_v2.md"
DEFAULT_OUT = SCRIPT_DIR / "Arquitectura_Base_de_Datos_v2.pdf"

W = 210
H = 297
ML = 18
MR = 18
CW = W - ML - MR
BREAK_Y = H - 18 - 1

FONT = "arial"
MONO = "consola"

INLINE_RE = re.compile(r"(\*\*.+?\*\*|`[^`]+`)")

SEP_RE = re.compile(r"^\s*\|?[\s:|-]+\|?\s*$")
BULLET_RE = re.compile(r"^\s*[-*]\s+")
NUM_ITEM_RE = re.compile(r"^\s*\d+\.\s+")


# ---------------------------------------------------------------- PDF

class PDF_A4(FPDF):
    def header(self):
        if self.page_no() == 1:
            return
        self.set_font(FONT, "I", 7.5)
        self.set_text_color(120)
        self.set_y(12)
        self.cell(0, 5, "Arquitectura de Base de Datos v2 — SaaS B2B de Datos", align="L")
        self.set_draw_color(200, 205, 214)
        self.line(ML, 18, W - MR, 18)
        self.set_text_color(0)

    def footer(self):
        self.set_y(-14)
        self.set_font(FONT, "I", 7.5)
        self.set_text_color(120)
        self.cell(0, 5, f"Página {self.page_no()}", align="R")
        self.set_text_color(0)


pdf = PDF_A4(format="A4")
pdf.set_auto_page_break(auto=True, margin=18)
pdf.set_margins(ML, 16, MR)
pdf.add_font(FONT, "", r"C:\Windows\Fonts\arial.ttf")
pdf.add_font(FONT, "B", r"C:\Windows\Fonts\arialbd.ttf")
pdf.add_font(FONT, "I", r"C:\Windows\Fonts\ariali.ttf")
pdf.add_font(FONT, "BI", r"C:\Windows\Fonts\arialbi.ttf")
pdf.add_font(MONO, "", r"C:\Windows\Fonts\consola.ttf")


def heading(level, text):
    text = clean_md(text)
    pdf.ln(3)
    if level == 1:
        pdf.set_font(FONT, "B", 14)
        pdf.set_text_color(23, 55, 94)
        pdf.set_fill_color(222, 232, 244)
        pdf.cell(0, 9, text, new_x="LMARGIN", new_y="NEXT", fill=True)
    else:
        pdf.set_font(FONT, "B", 11.5)
        pdf.set_text_color(23, 55, 94)
        pdf.cell(0, 7, text, new_x="LMARGIN", new_y="NEXT")
    pdf.set_text_color(0)
    pdf.ln(1)


def clean_md(text):
    return text.replace("**", "").replace("`", "")


def inline_parts(text):
    parts = INLINE_RE.split(text)
    out = []
    for p in parts:
        if not p:
            continue
        if p.startswith("**") and p.endswith("**") and len(p) > 4:
            out.append(("B", p[2:-2]))
        elif p.startswith("`") and p.endswith("`") and len(p) > 2:
            out.append(("M", p[1:-1]))
        else:
            out.append(("N", p))
    return out


def write_inline(text, size):
    runs = inline_parts(text)
    if not runs:
        runs = [("N", "")]
    pdf.set_x(ML)
    for style, txt in runs:
        if style == "B":
            pdf.set_font(FONT, "B", size)
        elif style == "M":
            pdf.set_font(MONO, "", size - 0.3)
        else:
            pdf.set_font(FONT, "", size)
        pdf.write(5.0, txt)
    pdf.ln(5.2)


def para(text, italic=False, indent=0, size=9.5):
    if indent:
        pdf.set_x(ML + indent * 9)
    pdf.set_font(FONT, "I" if italic else "", size)
    if italic:
        write_inline(text, size)
    else:
        write_inline(text, size)


def bullet(text, size=9.5):
    write_inline("\u2022  " + text, size)
    pdf.ln(0.5)


def num_item(text, size=9.5):
    write_inline(text, size)
    pdf.ln(0.5)


def code_block(lines):
    pdf.set_font(MONO, "", 8)
    pdf.set_fill_color(240, 242, 246)
    pdf.set_draw_color(200, 205, 214)
    for line in lines:
        pdf.set_x(ML)
        pdf.multi_cell(CW, 4.8, "  " + line, border=0, fill=True, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(3)


def table(headers, rows):
    ncol = len(headers)
    lens = []
    for c in range(ncol):
        m = max([len(clean_md(r[c])) for r in rows] + [len(clean_md(headers[c]))])
        lens.append(max(min(m, 55), 6))
    total = sum(lens) or 1
    colw = [l / total * CW for l in lens]
    x0 = ML

    def set_cell_font(c):
        pdf.set_font(FONT, "", 8.2)

    if pdf.get_y() + 6.4 > BREAK_Y:
        pdf.add_page()
    y_cur = pdf.get_y()

    pdf.set_font(FONT, "B", 8.5)
    pdf.set_fill_color(23, 55, 94)
    pdf.set_text_color(255)
    pdf.set_draw_color(200, 205, 214)
    for c, htxt in enumerate(headers):
        pdf.set_xy(x0 + sum(colw[:c]), y_cur)
        pdf.cell(colw[c], 6.4, clean_md(htxt), fill=True, align="L", border=1)
    y_cur += 6.4
    pdf.set_font(FONT, "", 8.2)
    pdf.set_text_color(0)

    for row in rows:
        max_lines = 1
        for c, cell in enumerate(row):
            lines = pdf.multi_cell(max(colw[c] - 2.5, 6.0), 4.6, clean_md(cell), dry_run=True, output="LINES")
            max_lines = max(max_lines, len(lines) if isinstance(lines, list) else 1)
        rh = max_lines * 4.8 + 2.2
        if y_cur + rh > BREAK_Y:
            pdf.add_page()
            y_cur = pdf.get_y()
        for c, cell in enumerate(row):
            set_cell_font(c)
            pdf.set_xy(x0 + sum(colw[:c]), y_cur)
            pdf.multi_cell(colw[c], 4.6, clean_md(cell), border=1, new_x="RIGHT", new_y="TOP")
        y_cur += rh
    pdf.set_xy(x0, y_cur)
    pdf.ln(3)


# --------------------------------------------------- parseo del MD

def read_lines(path):
    with open(path, encoding="utf-8") as f:
        return f.read().split("\n")


def parse_body(lines):
    """Convierte las lineas del body en bloques: (tipo, datos)."""
    blocks = []
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i].rstrip()
        s = line.strip()

        if not s:
            i += 1
            continue

        if s.startswith("```"):
            j = i + 1
            code = []
            while j < n and not lines[j].strip().startswith("```"):
                code.append(lines[j].rstrip())
                j += 1
            blocks.append(("code", code))
            i = j + 1
            continue

        if s.startswith("#"):
            m = re.match(r"^(#{1,6})\s+(.*)$", s)
            blocks.append(("heading", m.group(1).count("#"), m.group(2)))
            i += 1
            continue

        if s == "---":
            i += 1
            continue

        if s.startswith("|"):
            rows = []
            while i < n and lines[i].strip().startswith("|"):
                cells = [c.strip() for c in lines[i].strip().strip("|").split("|")]
                rows.append(cells)
                i += 1
            # quitar fila separadora
            filtrado = [r for r in rows if not SEP_RE.match("|".join(r))]
            headers = filtrado[0]
            body = filtrado[1:]
            blocks.append(("table", headers, body))
            continue

        if s.startswith(">"):
            q = []
            while i < n and lines[i].strip().startswith(">"):
                q.append(lines[i].strip().lstrip(">").strip())
                i += 1
            blocks.append(("quote", " ".join(q)))
            continue

        if BULLET_RE.match(s):
            items = []
            while i < n:
                ls = lines[i].rstrip()
                if not ls.strip():
                    break
                if BULLET_RE.match(ls.strip()):
                    items.append(ls.strip()[1:].strip())
                elif ls.startswith(" "):
                    items[-1] += " " + ls.strip()
                else:
                    break
                i += 1
            for it in items:
                blocks.append(("bullet", it))
            continue

        if NUM_ITEM_RE.match(s):
            items = []
            while i < n:
                ls = lines[i].rstrip()
                if not ls.strip():
                    break
                if NUM_ITEM_RE.match(ls.strip()):
                    items.append(ls.strip())
                elif ls.startswith(" "):
                    items[-1] += " " + ls.strip()
                else:
                    break
                i += 1
            for it in items:
                m = re.match(r"^\s*\d+\.\s+(.*)$", it)
                blocks.append(("num", f"{m.group(1)}"))
            continue

        # párrafo (acumular líneas contiguas)
        para_lines = []
        while i < n:
            ls = lines[i].rstrip()
            if not ls.strip():
                break
            ss = ls.strip()
            if (ss.startswith("```") or ss.startswith("#") or ss == "---"
                    or ss.startswith("|") or ss.startswith(">")
                    or BULLET_RE.match(ss) or NUM_ITEM_RE.match(ss)):
                break
            para_lines.append(ss)
            i += 1
        blocks.append(("para", " ".join(para_lines)))

    return blocks


def clean_text_for_cover(t):
    return re.sub(r"[_*]|`", "", t)


def render_cover(head_block, meta_info):
    pdf.add_page()
    pdf.set_fill_color(23, 55, 94)
    pdf.rect(0, 0, W, 66, "F")
    pdf.set_fill_color(41, 98, 168)
    pdf.rect(0, 66, W, 3.5, "F")

    title, subtitle, date, estado, nota = meta_info["title"], meta_info["subtitle"], meta_info["date"], meta_info["estado"], meta_info["nota"]

    pdf.set_xy(ML, 22)
    pdf.set_font(FONT, "B", 22)
    pdf.set_text_color(255)
    pdf.cell(0, 10, title, align="L")
    pdf.set_xy(ML, 36)
    pdf.set_font(FONT, "", 14)
    pdf.cell(0, 8, subtitle, align="L")
    pdf.set_xy(ML, 50)
    pdf.set_font(FONT, "I", 9)
    pdf.cell(0, 6, f"Documento de revisión técnica · Fecha: {date}", align="L")
    pdf.ln(30)

    pdf.set_text_color(0)
    pdf.set_font(FONT, "B", 9.5)
    pdf.cell(0, 6, "Ámbito del documento", new_x="LMARGIN", new_y="NEXT")
    pdf.set_font(FONT, "", 9.5)
    pdf.multi_cell(0, 5.2, nota, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(2)
    pdf.set_font(FONT, "", 9.5)
    pdf.multi_cell(0, 5.2, estado, new_x="LMARGIN", new_y="NEXT")


def compute_cover_meta(lines):
    h1 = None
    for ln in lines:
        s = ln.strip()
        if s.startswith("# ") and h1 is None:
            h1 = s[2:].strip()
            break
    title, sub = "Arquitectura de Base de Datos", "SaaS B2B de Datos"
    if h1 and "—" in h1:
        parts = [p.strip() for p in h1.split("—")]
        title = parts[0]
        sub = re.sub(r"\(v\d+\)$", "", parts[1]).strip() if len(parts) > 1 else sub
    else:
        title = h1 or title

    head_text = "\n".join(lines[:30])
    date_m = re.search(r"Fecha:\s*(\d{4}-\d{2}-\d{2})", head_text)
    date = date_m.group(1) if date_m else ""
    subm = re.search(r"\(Documento de revisión técnica\)", head_text)
    doc_line = "Documento de revisión técnica"
    if re.search(r"\*\*Documento de revisión técnica\*\*", head_text):
        doc_line = "Documento de revisión técnica"

    nota_m = re.search(r"_Sustituye al modelo de datos descrito en [^_]+_\.?", head_text, re.S)
    nota = ""
    if nota_m:
        nota = clean_text_for_cover(nota_m.group(0)).split(". ")[0] + "."
    else:
        nota = "Sustituye al modelo de datos descrito en «Arquitectura Base de Datos SaaS.pdf»."

    estado_m = re.search(r"Fecha: [^\n]+", head_text)
    estado = clean_text_for_cover(estado_m.group(0)) if estado_m else ""

    return {"title": title, "subtitle": sub, "date": date, "doc_line": doc_line,
            "nota": nota, "estado": estado}


def split_cover_and_body(lines):
    for i, ln in enumerate(lines):
        if ln.strip() == "---" and i > 0:
            return lines[1:i], lines[i + 1:]
    return lines[1:], []


def main():
    md_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_MD
    out_path = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_OUT

    lines = read_lines(md_path)
    cover, body = split_cover_and_body(lines)
    meta = compute_cover_meta(cover)
    render_cover(cover, meta)

    first = True
    for block in parse_body(body):
        if block[0] == "heading":
            heading(block[1], block[2])
        elif block[0] == "para":
            t = block[1]
            if t.startswith("_") and t.endswith("_") and len(t) > 2:
                para(t.strip("_"), italic=True)
            else:
                para(t)
        elif block[0] == "bullet":
            bullet(block[1])
        elif block[0] == "num":
            num_item(block[1])
        elif block[0] == "quote":
            para(block[1], italic=True)
        elif block[0] == "code":
            if first and pdf.get_y() > H - 30:
                pass
            code_block(block[1])
        elif block[0] == "table":
            table(block[1], block[2])
        first = False

    pdf.output(out_path)
    print(f"PDF generado: {out_path} · páginas: {pdf.page_no()}")


if __name__ == "__main__":
    main()