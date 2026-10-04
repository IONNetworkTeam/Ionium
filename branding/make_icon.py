"""Draws the Ionium icon: a comet with an ion tail, in the style of the Sodium family of icons (full square,
soft vertical gradient lighter at the bottom, one white shape). Run from this folder: python3 make_icon.py"""
import math
from PIL import Image, ImageDraw

S = 1024
TOP, BOTTOM = (58, 112, 228), (92, 196, 240)
W = 255
ANG = math.radians(135)                  # tail direction: towards bottom-left
UX, UY = math.cos(ANG), math.sin(ANG)
NX, NY = -UY, UX


def background():
    img = Image.new('RGBA', (S, S))
    d = ImageDraw.Draw(img)
    for y in range(S):
        t = y / (S - 1)
        d.line([(0, y), (S, y)], fill=tuple(round(TOP[k] + (BOTTOM[k] - TOP[k]) * t) for k in range(3)) + (255,))
    return img


def center_mask(m, target=0.70):
    """Scales and centres the drawn shape so its bounding box is `target` of the tile, like the reference icons."""
    box = m.getbbox()
    shape = m.crop(box)
    k = target * S / max(shape.size)
    shape = shape.resize((round(shape.width * k), round(shape.height * k)), Image.LANCZOS)
    out = Image.new('L', (S, S), 0)
    out.paste(shape, ((S - shape.width) // 2, (S - shape.height) // 2))
    return out


def compose(m):
    img = background()
    img.paste(Image.new('RGBA', (S, S), (255, 255, 255, 255)), (0, 0), center_mask(m))
    return img


def head(d, hx, hy, r, gap):
    d.ellipse((hx - r - gap, hy - r - gap, hx + r + gap, hy + r + gap), fill=0)
    d.ellipse((hx - r, hy - r, hx + r, hy + r), fill=W)


def streak_straight(d, bx, by, length, width):
    w = width / 2
    d.polygon([(bx + NX * w, by + NY * w), (bx + UX * length, by + UY * length), (bx - NX * w, by - NY * w)], fill=W)


def streak_curved(d, bx, by, length, width, bend):
    """Tapered streak along a slightly bent path (bend > 0 curves it clockwise)."""
    left, right = [], []
    n = 60
    for i in range(n + 1):
        t = i / n
        w = width / 2 * (1 - t) ** 0.9
        off = bend * length * t * t
        cx = bx + UX * length * t + NX * off
        cy = by + UY * length * t + NY * off
        left.append((cx + NX * w, cy + NY * w))
        right.append((cx - NX * w, cy - NY * w))
    d.polygon(left + right[::-1], fill=W)


def streak_round(d, bx, by, length, width):
    """Capsule-like streak: rounded tip, full width at the head."""
    w = width / 2
    tipx, tipy = bx + UX * length, by + UY * length
    tw = w * 0.42
    d.polygon([(bx + NX * w, by + NY * w), (tipx + NX * tw, tipy + NY * tw),
               (tipx - NX * tw, tipy - NY * tw), (bx - NX * w, by - NY * w)], fill=W)
    d.ellipse((tipx - tw, tipy - tw, tipx + tw, tipy + tw), fill=W)


def comet(kind):
    m = Image.new('L', (S, S), 0)
    d = ImageDraw.Draw(m)
    hx, hy, r = S * 0.62, S * 0.38, S * 0.17
    tails = ((-0.66, 0.40, 0.11), (0.0, 0.58, 0.15), (0.66, 0.40, 0.11))
    for off, length, width in tails:
        bx, by = hx + NX * r * off, hy + NY * r * off
        if kind == 1:
            streak_straight(d, bx, by, S * length, S * width)
        elif kind == 2:
            streak_curved(d, bx, by, S * length, S * width, bend=0.10 * (1 if off >= 0 else 0.6))
        else:
            streak_round(d, bx, by, S * length * 0.92, S * width)
    # gaps between the streaks so they read as separate (negative space like Sodium's cutouts)
    for off in (-0.33, 0.33):
        bx, by = hx + NX * r * off, hy + NY * r * off
        d.line([(bx, by), (bx + UX * S * 0.7, by + UY * S * 0.7)], fill=0, width=int(S * 0.028))
    head(d, hx, hy, r, S * 0.035)
    return compose(m)



icon = comet(3)
icon.save('icon-1024.png')
icon.resize((512, 512), Image.LANCZOS).save('icon-512.png')
icon.resize((256, 256), Image.LANCZOS).save('../forge189/src/main/resources/assets/ionium/icon.png')
print('ok')
