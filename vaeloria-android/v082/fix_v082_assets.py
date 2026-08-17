from pathlib import Path
from PIL import Image, ImageEnhance, ImageFilter, ImageDraw

RES = Path('app/src/main/res/drawable-nodpi')
RES.mkdir(parents=True, exist_ok=True)

# v0.8.2 repository blobs were not valid WebP images. Rebuild the four
# premium assets from the already-generated, known-good v0.8.1 PNG artwork.
# This keeps the app fully offline and guarantees Android can decode them.
SOURCES = {
    'hero_einoras_v082.webp': 'hero_einoras.png',
    'scene_palace_v082.webp': 'scene_luminara.png',
    'quest_meridian_v082.webp': 'quest_meridian.png',
    'world_map_v082.webp': 'world_map_v060.png',
}


def rebuild(dst_name: str, src_name: str) -> None:
    src = RES / src_name
    dst = RES / dst_name
    if not src.exists():
        raise SystemExit(f'Missing known-good source image: {src}')

    with Image.open(src) as im:
        im.load()
        base = im.convert('RGB')

        # Slight v0.8.2 polish while preserving the known-good artwork.
        if dst_name.startswith('hero_'):
            base = ImageEnhance.Contrast(base).enhance(1.05)
            base = ImageEnhance.Sharpness(base).enhance(1.08)
        elif dst_name.startswith('scene_'):
            base = ImageEnhance.Contrast(base).enhance(1.07)
            base = ImageEnhance.Color(base).enhance(0.92)
        elif dst_name.startswith('quest_'):
            base = ImageEnhance.Contrast(base).enhance(1.08)
        elif dst_name.startswith('world_map_'):
            base = ImageEnhance.Contrast(base).enhance(1.04)
            base = ImageEnhance.Sharpness(base).enhance(1.05)

        base.save(dst, format='WEBP', quality=92, method=6)

    # Real decode validation, not merely a file-size check.
    with Image.open(dst) as check:
        check.verify()
    with Image.open(dst) as check:
        w, h = check.size
        fmt = check.format
    if fmt != 'WEBP' or w < 300 or h < 180 or dst.stat().st_size < 10000:
        raise SystemExit(f'Invalid rebuilt asset {dst_name}: format={fmt} size={w}x{h} bytes={dst.stat().st_size}')
    print(f'{dst_name}: WEBP {w}x{h} {dst.stat().st_size} bytes OK')


for dst_name, src_name in SOURCES.items():
    rebuild(dst_name, src_name)

print('v0.8.2 image hotfix complete')
