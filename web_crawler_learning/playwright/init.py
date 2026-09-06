import sys
from pathlib import Path


venv_dir = Path(sys.executable).parent.parent
custom_browser_path = venv_dir / "ms-playwright" / "chromium-1234" / "chrome-win64" / "chrome.exe"
