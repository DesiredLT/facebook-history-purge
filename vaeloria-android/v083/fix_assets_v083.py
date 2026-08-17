from pathlib import Path
from PIL import Image

RES = Path('app/src/main/res/drawable-nodpi')

# v0.8.2 accidentally shipped four files with .webp extensions whose bytes were
# not WebP images. Android packaged them, but BitmapFactory.decodeResource()
# returned null at runtime. Rebuild the four v0.8.x visual entry points from the
# last known-good generated artwork, keeping everything offline and bundled.
MAPPING = {
    'hero_einoras_v082.webp': 'hero_einoras.png',
    'scene_palace_v082.webp': 'scene_luminara.png',
    'quest_meridian_v082.webp': 'quest_meridian.png',
    'world_map_v082.webp': 'world_map_v060.png',
}

for dst_name, src_name in MAPPING.items():
    src = RES / src_name
    dst = RES / dst_name
    if not src.exists():
        raise SystemExit(f'Missing known-good source image: {src_name}')

    with Image.open(src) as im:
        im.load()
        if im.width < 300 or im.height < 180:
            raise SystemExit(f'Unexpected source dimensions for {src_name}: {im.size}')
        rgb = im.convert('RGB')
        rgb.save(dst, format='WEBP', quality=92, method=6)

    raw = dst.read_bytes()
    if len(raw) < 4096 or raw[:4] != b'RIFF' or raw[8:12] != b'WEBP':
        raise SystemExit(f'Generated file is not a real WebP: {dst_name}')

    with Image.open(dst) as check:
        check.load()
        if check.width < 300 or check.height < 180:
            raise SystemExit(f'Bad generated dimensions for {dst_name}: {check.size}')
        print(f'{dst_name}: OK {check.width}x{check.height}, {len(raw)} bytes')

print('v0.8.3 visual recovery complete')
