package net.originmobi.pdv.selenium.pages.caixa;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CaixaListPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Page elements
    private By pageHeader = By.xpath("//h1[contains(text(),'Caixa')]");
    private By openCaixaButton = By.xpath("//a[contains(text(),'Abrir Caixa') or contains(text(),'Novo')]");
    private By closeCaixaButton = By.xpath("//a[contains(text(),'Fechar Caixa')]");
    private By searchInput = By.cssSelector("input[placeholder*='Buscar']");
    private By searchButton = By.cssSelector("button[type='submit'] i.glyphicon-search");
    private By caixaTable = By.cssSelector("table.table-striped tbody");
    private By successMessage = By.cssSelector("div.alert-success span");
    private By errorMessage = By.cssSelector("div.alert-danger span");

    public CaixaListPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isPageLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).isDisplayed()
                || wait.until(ExpectedConditions.visibilityOfElementLocated(openCaixaButton)).isDisplayed();
    }

    public void clickOpenCaixa() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(openCaixaButton));
        button.click();
    }

    public void clickCloseCaixa() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(closeCaixaButton));
        button.click();
    }

    public void searchCaixa(String searchTerm) {
        WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        searchField.clear();
        searchField.sendKeys(searchTerm);
        
        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        searchBtn.click();
    }

    public boolean isCaixaInTable(String caixaInfo) {
        WebElement tableBody = wait.until(ExpectedConditions.visibilityOfElementLocated(caixaTable));
        return tableBody.getText().contains(caixaInfo);
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).getText();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public boolean isCaixaOpen() {
        // Check if there's an open caixa indicator on the page
        try {
            return driver.findElement(By.xpath("//span[contains(text(),'Caixa Aberto') or contains(text(),'Aberto')]")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCaixaClosed() {
        // Check if there's a closed caixa indicator on the page
        try {
            return driver.findElement(By.xpath("//span[contains(text(),'Caixa Fechado') or contains(text(),'Fechado')]")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
} 