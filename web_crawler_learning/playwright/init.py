import os, sys
from pathlib import Path

def setup():
    venv_dir = Path(sys.executable).parent.parent
    os.environ.setdefault(
        "PLAYWRIGHT_BROWSERS_PATH",
        str(venv_dir / "ms-playwright"),
    )
