#!/usr/bin/env python3
"""Decode newline-chunked base64 from stdin into a generated source image."""

from __future__ import annotations

import base64
import sys
from pathlib import Path


def main() -> None:
    output = Path(sys.argv[1])
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("wb") as handle:
        for line in sys.stdin:
            value = line.strip()
            if value == "END":
                break
            if value:
                handle.write(base64.b64decode(value, validate=True))


if __name__ == "__main__":
    main()
