package net.originmobi.pdv.selenium.pages.venda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.originmobi.pdv.selenium.pages.login.LoginPage;

public class VendaServiceSeleniumTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected LoginPage loginPage;

    private static final String BASE_URL = "http://localhost:8080";

    private void criarTitulo() {
        try {
            driver.get(BASE_URL + "/titulos");
            WebElement botaoNovo = wait.until(
                    ExpectedConditions.elementToBeClickable(By.linkText("Novo")));
            botaoNovo.click();

            WebElement campoDescricao = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("descricao")));
            campoDescricao.clear();
            campoDescricao.sendKeys("selenium");

            WebElement botaoSalvar = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Salvar']")));
            botaoSalvar.click();

            // Pequena pausa para garantir que a ação foi processada
            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("Erro ao criar título: " + e.getMessage());
        }
    }

    private void abrirCaixa() {
        try {
            driver.get(BASE_URL + "/caixa");

            // Clica no botão "Abrir Novo"
            WebElement botaoAbrirNovo = wait.until(
                    ExpectedConditions.elementToBeClickable(By.linkText("Abrir Novo")));
            botaoAbrirNovo.click();

            // Preenche a observação
            WebElement campoObservacao = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("descricao")));
            campoObservacao.clear();
            campoObservacao.sendKeys("Teste Selenium");

            // Preenche o valor de abertura
            WebElement campoValorAbertura = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("valorAbertura")));
            campoValorAbertura.clear();
            campoValorAbertura.sendKeys("100");

            // Clica no botão "Abrir"
            WebElement botaoAbrir = wait.until(
                    ExpectedConditions.elementToBeClickable(By.linkText("Abrir")));
            botaoAbrir.click();

            // Valida se apareceu mensagem de sucesso
            WebElement mensagemSucessoCaixa = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success span")));
            assertTrue(mensagemSucessoCaixa.isDisplayed(), "Caixa não foi aberto com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao abrir caixa: " + e.getMessage());
        }
    }

    @BeforeEach
    public void setup() {
        driver = WebDriverManager.chromedriver().create();
        wait = new WebDriverWait(driver, 10);
        driver.manage().window().maximize();

        loginPage = new LoginPage(driver, wait);

        // Login
        loginPage.navigateTo(BASE_URL + "/login");
        assertTrue(loginPage.isLoginPageDisplayed(), "Página de Login não foi exibida.");
        loginPage.login("gerente", "123");

        // Verifica se o login funcionou verificando se foi redirecionado
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));

        // Criação de título e abertura de caixa
        criarTitulo();
        abrirCaixa();

        // Acessa a página de venda aberta
        driver.get(BASE_URL + "/venda/status/ABERTA");
        WebElement tituloPedidos = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.titulo-h1")));
        assertTrue(tituloPedidos.isDisplayed(), "Página de venda aberta não foi exibida corretamente!");
    }

    @Test
    public void criaEFechaVenda() throws InterruptedException {
        //Abertura da venda
        WebElement botaoNovoPedido = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Novo Pedido")));
        botaoNovoPedido.click();
        Thread.sleep(2000);
        WebElement selectCliente = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("cliente")));
        Select dropdown = new Select(selectCliente);
        dropdown.selectByIndex(1);
        Thread.sleep(2000);
        WebElement campoObservacao = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("observacao")));
        campoObservacao.clear();
        campoObservacao.sendKeys("teste selenium");
        Thread.sleep(2000);
        WebElement botaoSalvar = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("btn-salva")));
        botaoSalvar.click();
        Thread.sleep(3000);

        // Aqui é onde o produto é adicionado à venda. Nesse caso, foi escolhido o picolé
        WebElement botaoDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-id='codigoProduto']")));
        botaoDropdown.click();
        Thread.sleep(2000);
        try {
            WebElement opcaoPicole = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(@data-normalized-text, 'Picolé')]")));

            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", opcaoPicole);
        } catch (Exception e) {
            WebElement selectOriginal = driver.findElement(By.id("codigoProduto"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].selectedIndex = 1; arguments[0].dispatchEvent(new Event('change'));", selectOriginal);
        }
        Thread.sleep(2000);
        WebElement botaoInserir = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".js-addvenda-produto")));
        botaoInserir.click();
        Thread.sleep(3000);

        // Aqui é onde temos o fechamento da venda
        WebElement botaoGerarVenda = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("btn-venda")));
        botaoGerarVenda.click();
        Thread.sleep(2000);
        WebElement selectFormaPagamento = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("pagamento")));
        Select dropdownPagamento = new Select(selectFormaPagamento);
        dropdownPagamento.selectByIndex(1); // 0 geralmente é vazio, 1 é o primeiro
        Thread.sleep(2000);
        WebElement campoDesconto = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("desconto")));
        campoDesconto.clear();
        campoDesconto.sendKeys("1");
        Thread.sleep(2000);
        WebElement campoAcrescimo = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("acrescimo")));
        campoAcrescimo.clear();
        campoAcrescimo.sendKeys("1");
        Thread.sleep(2000);
        WebElement botaoPagar = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-pagamento")));
        botaoPagar.click();
        Thread.sleep(2000);
        WebElement mensagemSucesso = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success span")));
        assertTrue(mensagemSucesso.isDisplayed(), "Mensagem de sucesso não foi exibida!");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}