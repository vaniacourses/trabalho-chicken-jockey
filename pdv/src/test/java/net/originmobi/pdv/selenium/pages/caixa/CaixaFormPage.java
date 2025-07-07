package net.originmobi.pdv.selenium.pages.caixa;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CaixaFormPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Form elements for opening caixa
    private By formTitle = By.xpath("//h2[contains(text(),'Caixa')]");
    private By tipoSelect = By.id("tipo");
    private By valorAberturaInput = By.id("valor_abertura");
    private By descricaoInput = By.id("descricao");
    private By agenciaInput = By.id("agencia");
    private By contaInput = By.id("conta");
    private By saveButton = By.cssSelector("input[type='submit'][value='Salvar']");
    private By cancelButton = By.cssSelector("a[href*='caixa']");

    // Form elements for closing caixa
    private By passwordInput = By.id("senha");
    private By closeButton = By.cssSelector("input[type='submit'][value='Fechar']");

    // Messages
    private By successMessage = By.cssSelector("div.alert-success span");
    private By errorMessage = By.cssSelector("div.alert-danger span");

    public CaixaFormPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isFormLoaded() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(formTitle)).isDisplayed();
    }

    // Methods for opening caixa
    public void selectTipo(String tipo) {
        WebElement select = wait.until(ExpectedConditions.elementToBeClickable(tipoSelect));
        new Select(select).selectByVisibleText(tipo);
    }

    public void setValorAbertura(String valor) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(valorAberturaInput));
        input.clear();
        input.sendKeys(valor);
    }

    public void setDescricao(String descricao) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(descricaoInput));
        input.clear();
        input.sendKeys(descricao);
    }

    public void setAgencia(String agencia) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(agenciaInput));
        input.clear();
        input.sendKeys(agencia);
    }

    public void setConta(String conta) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(contaInput));
        input.clear();
        input.sendKeys(conta);
    }

    public void clickSave() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        button.click();
    }

    public void clickCancel() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        button.click();
    }

    // Methods for closing caixa
    public void setPassword(String password) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
        input.clear();
        input.sendKeys(password);
    }

    public void clickClose() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(closeButton));
        button.click();
    }

    // Form filling methods
    public void openCaixa(String tipo, String valorAbertura, String descricao) {
        selectTipo(tipo);
        setValorAbertura(valorAbertura);
        setDescricao(descricao);
        clickSave();
    }

    public void openCaixaBanco(String valorAbertura, String descricao, String agencia, String conta) {
        selectTipo("BANCO");
        setValorAbertura(valorAbertura);
        setDescricao(descricao);
        setAgencia(agencia);
        setConta(conta);
        clickSave();
    }

    public void closeCaixa(String password) {
        setPassword(password);
        clickClose();
    }

    // Message methods
    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).getText();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public boolean hasSuccessMessage() {
        try {
            return driver.findElement(successMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasErrorMessage() {
        try {
            return driver.findElement(errorMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
} 