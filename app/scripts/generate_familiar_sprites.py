#!/usr/bin/env python3
"""Generate 32x32 cute chibi familiar sprites (species x mood) into res/drawable-nodpi/."""
from __future__ import annotations

import struct
import zlib
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "src/main/res/drawable-nodpi"

# Softer pastel palettes: main, shadow, light_belly/cheek, accent, eye_dark, blush, white_highlight
PALETTES = {
    "bear": {
        "main": (196, 154, 120),
        "shadow": (140, 100, 78),
        "light": (235, 210, 188),
        "accent": (120, 82, 62),
        "eye": (55, 42, 38),
        "blush": (255, 188, 198),
        "shine": (255, 252, 248),
    },
    "wolf": {
        "main": (176, 186, 210),
        "shadow": (120, 132, 158),
        "light": (220, 228, 245),
        "accent": (100, 118, 150),
        "eye": (45, 55, 85),
        "blush": (255, 195, 210),
        "shine": (248, 250, 255),
    },
    "dragon": {
        "main": (130, 210, 175),
        "shadow": (75, 155, 120),
        "light": (200, 245, 220),
        "accent": (255, 200, 120),
        "eye": (40, 95, 72),
        "blush": (255, 200, 215),
        "shine": (255, 255, 255),
    },
}

MOODS = [
    "sparkling",
    "cozy",
    "watching",
    "fading",
    "curious",
    "dormant",
]


def png_bytes(rgba_rows: list[bytes]) -> bytes:
    w, h = len(rgba_rows[0]) // 4, len(rgba_rows)
    sig = b"\x89PNG\r\n\x1a\n"

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(
            ">I", zlib.crc32(tag + data) & 0xFFFFFFFF
        )

    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    raw = b""
    for row in rgba_rows:
        raw += b"\0" + row
    comp = zlib.compress(raw, 9)
    return sig + chunk(b"IHDR", ihdr) + chunk(b"IDAT", comp) + chunk(b"IEND", b"")


def solid_rgba(w: int, h: int, r: int, g: int, b: int, a: int = 255) -> list[bytes]:
    px = bytes([r, g, b, a])
    return [px * w for _ in range(h)]


def set_px(canvas: list[bytes], x: int, y: int, rgba: tuple[int, int, int, int]) -> None:
    w = len(canvas[0]) // 4
    h = len(canvas)
    if not (0 <= x < w and 0 <= y < h):
        return
    r, g, b, a = rgba
    row = bytearray(canvas[y])
    i = x * 4
    row[i : i + 4] = bytes([r, g, b, a])
    canvas[y] = bytes(row)


def fill_ellipse(
    canvas: list[bytes],
    cx: float,
    cy: float,
    rx: int,
    ry: int,
    rgba: tuple[int, int, int, int],
) -> None:
    rx = max(rx, 1)
    ry = max(ry, 1)
    rr = rx * rx * ry * ry
    for dy in range(-ry, ry + 1):
        for dx in range(-rx, rx + 1):
            if dx * dx * ry * ry + dy * dy * rx * rx <= rr:
                set_px(canvas, int(cx + dx), int(cy + dy), rgba)


def draw_cute_base(canvas: list[bytes], species: str, p: dict) -> None:
    """Big soft head + tiny body + species ears/horns."""
    # Chibi body (small pear under head)
    fill_ellipse(canvas, 16, 26, 6, 4, (*p["main"], 255))
    fill_ellipse(canvas, 16, 24, 5, 3, (*p["light"], 255))
    # Large round head
    fill_ellipse(canvas, 16, 14, 9, 8, (*p["main"], 255))
    # Lighter face / muzzle area
    fill_ellipse(canvas, 16, 16, 6, 5, (*p["light"], 255))
    # Subtle shadow under chin
    fill_ellipse(canvas, 16, 19, 7, 2, (*p["shadow"], 200))

    if species == "bear":
        # Round fluffy ears
        fill_ellipse(canvas, 10, 7, 4, 4, (*p["main"], 255))
        fill_ellipse(canvas, 22, 7, 4, 4, (*p["main"], 255))
        fill_ellipse(canvas, 10, 8, 2, 2, (*p["light"], 255))
        fill_ellipse(canvas, 22, 8, 2, 2, (*p["light"], 255))
        fill_ellipse(canvas, 10, 7, 2, 2, (*p["shadow"], 180))
        fill_ellipse(canvas, 22, 7, 2, 2, (*p["shadow"], 180))
    elif species == "wolf":
        # Fluffy tufts (softer than sharp triangles)
        fill_ellipse(canvas, 9, 6, 3, 5, (*p["main"], 255))
        fill_ellipse(canvas, 23, 6, 3, 5, (*p["main"], 255))
        fill_ellipse(canvas, 9, 7, 2, 3, (*p["light"], 255))
        fill_ellipse(canvas, 23, 7, 2, 3, (*p["light"], 255))
    else:  # dragon
        # Tiny nub horns + soft crest
        fill_ellipse(canvas, 8, 6, 3, 4, (*p["accent"], 255))
        fill_ellipse(canvas, 24, 6, 3, 4, (*p["accent"], 255))
        fill_ellipse(canvas, 16, 5, 8, 2, (*p["shadow"], 220))
        fill_ellipse(canvas, 16, 6, 6, 2, (*p["light"], 200))

    # Rosy cheeks (cute default)
    fill_ellipse(canvas, 9, 17, 2, 1, (*p["blush"], 200))
    fill_ellipse(canvas, 23, 17, 2, 1, (*p["blush"], 200))


def draw_eye_pair(
    canvas: list[bytes],
    lx: int,
    ly: int,
    rx: int,
    ry: int,
    ew: int,
    eh: int,
    p: dict,
    alpha: int = 255,
    highlight: bool = True,
) -> None:
    """Big round eyes with white shine."""
    for ex, ey in ((lx, ly), (rx, ry)):
        fill_ellipse(canvas, ex, ey, ew, eh, (*p["eye"], alpha))
        if highlight and alpha > 160:
            set_px(canvas, ex - 1, ey - 1, (*p["shine"], 255))
            set_px(canvas, ex - 2, ey, (*p["shine"], 220))


def draw_mouth_smile(canvas: list[bytes], y: int, p: dict, wide: bool = False) -> None:
    if wide:
        for x in range(13, 20):
            set_px(canvas, x, y, (*p["shadow"], 255))
        set_px(canvas, 12, y - 1, (*p["shadow"], 255))
        set_px(canvas, 20, y - 1, (*p["shadow"], 255))
    else:
        set_px(canvas, 14, y, (*p["shadow"], 255))
        set_px(canvas, 15, y, (*p["shadow"], 255))
        set_px(canvas, 16, y + 1, (*p["shadow"], 255))
        set_px(canvas, 17, y, (*p["shadow"], 255))


def draw_face(species: str, mood: str) -> list[bytes]:
    p = PALETTES[species]
    canvas = solid_rgba(32, 32, 0, 0, 0, 0)
    draw_cute_base(canvas, species, p)

    # Eyes / expression by mood (positions tuned for chibi head center ~16,14)
    if mood == "sparkling":
        draw_eye_pair(canvas, 12, 15, 20, 15, 3, 4, p, 255, True)
        # Star glints
        set_px(canvas, 8, 10, (*p["accent"], 255))
        set_px(canvas, 24, 8, (*p["accent"], 255))
        set_px(canvas, 26, 12, (*p["shine"], 255))
        draw_mouth_smile(canvas, 22, p, wide=True)
    elif mood == "cozy":
        # Happy closed ^_^
        for x, y in ((11, 16), (12, 15), (13, 16)):
            set_px(canvas, x, y, (*p["shadow"], 255))
        for x, y in ((19, 16), (20, 15), (21, 16)):
            set_px(canvas, x, y, (*p["shadow"], 255))
        draw_mouth_smile(canvas, 22, p, wide=True)
    elif mood == "watching":
        draw_eye_pair(canvas, 12, 15, 20, 15, 2, 3, p, 255, True)
        set_px(canvas, 15, 22, (*p["shadow"], 255))
    elif mood == "fading":
        draw_eye_pair(canvas, 12, 16, 20, 16, 2, 2, p, 140, False)
        # Tiny sympathetic tear dot
        set_px(canvas, 21, 18, (160, 200, 255, 200))
        set_px(canvas, 15, 22, (*p["shadow"], 180))
        set_px(canvas, 16, 23, (*p["shadow"], 160))
    elif mood == "curious":
        draw_eye_pair(canvas, 11, 14, 21, 14, 4, 5, p, 255, True)
        set_px(canvas, 15, 22, (*p["accent"], 255))
        set_px(canvas, 16, 23, (*p["shadow"], 255))
        # Slight head tilt suggestion: extra cheek blush one side
        fill_ellipse(canvas, 8, 18, 2, 2, (*p["blush"], 180))
    elif mood == "dormant":
        draw_eye_pair(canvas, 12, 16, 20, 16, 2, 1, p, 120, False)
        set_px(canvas, 14, 22, (*p["shadow"], 150))
        set_px(canvas, 17, 22, (*p["shadow"], 150))
        # zzz
        set_px(canvas, 22, 6, (*p["accent"], 200))
        set_px(canvas, 24, 5, (*p["accent"], 220))
        set_px(canvas, 26, 4, (*p["accent"], 255))
    else:
        raise ValueError(mood)

    return canvas


def draw_fallback() -> list[bytes]:
    p = {
        "main": (160, 170, 190),
        "shadow": (110, 120, 140),
        "light": (220, 225, 235),
        "accent": (140, 150, 170),
        "eye": (50, 55, 70),
        "blush": (255, 190, 200),
        "shine": (255, 255, 255),
    }
    canvas = solid_rgba(32, 32, 0, 0, 0, 0)
    draw_cute_base(canvas, "wolf", p)
    draw_eye_pair(canvas, 12, 15, 20, 15, 2, 3, p, 255, True)
    draw_mouth_smile(canvas, 22, p)
    return canvas


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    for species in PALETTES:
        for mood in MOODS:
            name = f"familiar_{species}_{mood}.png"
            (OUT / name).write_bytes(png_bytes(draw_face(species, mood)))
            print("wrote", name)
    (OUT / "familiar_pixel_fallback.png").write_bytes(png_bytes(draw_fallback()))
    print("wrote familiar_pixel_fallback.png")


if __name__ == "__main__":
    main()
