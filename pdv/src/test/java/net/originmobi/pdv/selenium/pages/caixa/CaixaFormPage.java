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
    private By tipoSelect = By.id("caixatipo");
    private By valorAberturaInput = By.id("valorAbertura");
    private By descricaoInput = By.id("descricao");
    private By agenciaInput = By.id("agencia");
    private By contaInput = By.id("conta");
    private By saveButton = By.cssSelector("a.btn-abrir-caixa");
    private By cancelButton = By.cssSelector("a[href*='caixa']");

    // Form elements for closing caixa
    private By passwordInput = By.id("senha");
    private By closeButton = By.cssSelector("input[type='submit'][value='Fechar']");

    // Messages
    private By successMessage = By.cssSelector("div.alert-success span");
    private By errorMessage = By.cssSelector("div.alert-danger span");

    private By adminSenhaInput = By.id("admsenha");
    private By modalFecharCaixaButton = By.cssSelector(".btn-fechar-caixa");

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
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
            button.click();
        } catch (Exception e) {
            // Se não for possível clicar, submete o formulário via JS
            WebElement form = driver.findElement(By.id("form_caixa"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        }
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

    public void setAdminSenha(String senha) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(adminSenhaInput));
        input.clear();
        input.sendKeys(senha);
    }
    public void clickModalFecharCaixa() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(modalFecharCaixaButton));
        btn.click();
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
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, 3);
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).getText();
        } catch (Exception e) {
            return "";
        }
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