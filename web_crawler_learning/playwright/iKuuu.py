import asyncio
import sys
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
        context = await browser.new_context()
        page = await context.new_page()

        await page.goto("https://ikuuu.foo/auth/login")

        email_input = page.locator("//input[@id='email']")
        await email_input.fill(sys.argv[1])
        password_input = page.locator("//input[@id='password']")
        await password_input.fill(sys.argv[2])

        verify_btn = page.locator("//div[@aria-label='点击按钮开始验证']")
        await verify_btn.click()

        await asyncio.sleep(1)
        login_button = page.locator("//div[contains(@class,'form-group')][6]/button[contains(@class,'btn')]")
        await login_button.click()

        checkin_btn = page.locator("//div[@id='checkin-div']")
        if checkin_btn:
            check_txt = await checkin_btn.inner_text()
            print(f"签到按钮文本：{check_txt}")
            await checkin_btn.click()
            print("已点击签到按钮")

        await context.close()
        await browser.close()


if __name__ == "__main__":
    asyncio.run(main())