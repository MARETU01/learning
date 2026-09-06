import asyncio
from playwright.async_api import async_playwright
from init import custom_browser_path

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