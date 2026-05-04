package org.tenea.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SeleniumAuthService {

    @Value("${tenea.base.url}")
    private String baseUrl;

    @Value("${tenea.username:}")
    private String defaultUsername;

    @Value("${tenea.password:}")
    private String defaultPassword;

    @Value("${tenea.user.agent}")
    private String userAgent;

    public BasicCookieStore authenticate(String username, String password) {
        // Usar parámetros si se proporcionan, si no usar valores por defecto
        String user = (username != null && !username.isEmpty()) ? username : defaultUsername;
        String pass = (password != null && !password.isEmpty()) ? password : defaultPassword;

        // Setup ChromeDriver automatically
        WebDriverManager.chromedriver().setup();

        System.out.println("[*] Iniciando motor Chrome (Headless)...");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-backgrounding");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=" + userAgent);
        options.setCapability("se:cdp", false);
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", java.util.Arrays.asList("enable-automation"));
        
        ChromeDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        BasicCookieStore cookieStore = new BasicCookieStore();

        try {
            System.out.println("[*] Navegando a Tenea Talent...");
            driver.get(baseUrl + "/GestionAccesos-3.2.4/base/Login?ReturnUrl=/GestionAccesos-3.2.4/ControlAccesos/CreateEmployee");

            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='text'], input[name='username']")));
            driver.findElement(By.cssSelector("input[type='text'], input[name='username']")).sendKeys(user);

            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password'], input[name='password']")));
            driver.findElement(By.cssSelector("input[type='password'], input[name='password']")).sendKeys(pass);

            driver.findElement(By.cssSelector("button[type='submit'], .common-components_button__primary")).click();

            wait.until(ExpectedConditions.urlContains("GestionAccesos"));
            Thread.sleep(2000);

            driver.manage().getCookies().forEach(cookie -> {
                BasicClientCookie newCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                newCookie.setDomain(cookie.getDomain());
                newCookie.setPath(cookie.getPath());
                cookieStore.addCookie(newCookie);
            });
            return cookieStore;
        } catch (Exception e) {
            System.err.println("❌ Error en Selenium: " + e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        } finally {
            driver.quit();
        }
    }
}

