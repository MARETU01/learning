import asyncio
import sys
from pathlib import Path
from playwright.async_api import async_playwright


venv_dir = Path(sys.executable).parent.parent
custom_browser_path = venv_dir / "ms-playwright" / "chromium-1234" / "chrome-win64" / "chrome.exe"

async def main():
    async with async_playwright() as p:
        # 启动 Chromium
        browser = await p.chromium.launch(
            executable_path=custom_browser_path,
            headless=True,
            args=[
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
            ]
        )
        page = await browser.new_page()

        await page.goto("https://bot.sannysoft.com/")

        # 截图保存
        await page.screenshot(path="example.png")
        print("截图已保存为 example.png")
        # a = input()

        await browser.close()


if __name__ == "__main__":
    asyncio.run(main())