import asyncio
from playwright.async_api import async_playwright
from init import custom_browser_path

async def main():
    async with async_playwright() as p:
        # 启动 Chromium
        context = await p.chromium.launch_persistent_context(
            user_data_dir="./modescope",
            executable_path=custom_browser_path,
            headless=True,
            args=[
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-dev-shm-usage",
            ]
        )
        page = context.pages[0] if context.pages else await context.new_page()

        await page.goto("https://modelscope.cn/models/ZhipuAI/GLM-5.3")

        like_btn = page.locator('//*[@id="root"]/div/div/main/div[1]/div/div[1]/div[1]/div/div/div[1]/div/div/div[3]/div[1]/span[2]')
        for i in range(40):
            await like_btn.click()
            print(f"第{i + 1}次点击")
            await asyncio.sleep(1)


if __name__ == "__main__":
    asyncio.run(main())