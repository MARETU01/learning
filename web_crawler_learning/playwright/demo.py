import asyncio
from playwright.async_api import async_playwright
from init import custom_browser_path

async def main():
    async with async_playwright() as p:
        # 启动 Chromium（使用你安装的浏览器）
        browser = await p.chromium.launch(executable_path=custom_browser_path, headless=False)  # headless=False 显示窗口
        page = await browser.new_page()

        # 访问百度
        await page.goto("https://www.baidu.com")

        # 获取页面标题
        title = await page.title()
        print(f"页面标题: {title}")

        # 截图保存
        await page.screenshot(path="example.png")
        print("截图已保存为 example.png")

        # 等待2秒以便观察
        await asyncio.sleep(2)

        await browser.close()


if __name__ == "__main__":
    asyncio.run(main())