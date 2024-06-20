from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
import time
from selenium.webdriver.firefox.options import Options
from selenium.webdriver.firefox.service import Service
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

options = Options()
options.profile = '.../Application Support/Firefox/Profiles/..'
driver = webdriver.Firefox(options=options)


def crawl_page(url: str, output_file: str):
    print(url)
    driver.get(url)
    print("opened")

    a_count = len(driver.find_elements(By.CSS_SELECTOR, "a"))
    print(a_count)

    buttons = [b for b in driver.find_elements(By.CLASS_NAME, "btn.btn-light") if 'показать больше' in b.text.lower()]
    print(len(buttons))
    while len(buttons) > 0:
        print("button")
        buttons[0].click()

        t = 0
        while a_count == len(driver.find_elements(By.CSS_SELECTOR, "a")) and t < 30:
            print("sleep")
            time.sleep(3)
            t = t + 1

        a_count = len(driver.find_elements(By.CSS_SELECTOR, "a"))
        print(a_count)

        html_content = driver.page_source
        with open(output_file, 'w', encoding='utf-8') as file:
            file.write(html_content)


        buttons = [b for b in driver.find_elements(By.CLASS_NAME, "btn.btn-light") if 'показать больше' in b.text.lower()]
        time.sleep(1)

    html_content = driver.page_source
    with open(output_file, 'w', encoding='utf-8') as file:
        file.write(html_content)
    time.sleep(2)
    print("saved")

if __name__ == '__main__':
    crawl_page("https://tgstat.ru/telegram", "telegram.html")
    crawl_page("https://tgstat.ru/instagram", "instagram.html")

    time.sleep(200)
