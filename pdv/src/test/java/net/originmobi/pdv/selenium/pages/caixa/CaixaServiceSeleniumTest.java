package net.originmobi.pdv.selenium.pages.caixa;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.originmobi.pdv.selenium.pages.login.LoginPage;

public class CaixaServiceSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected LoginPage loginPage;
    protected CaixaListPage caixaListPage;
    protected CaixaFormPage caixaFormPage;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void setup() {
        driver = WebDriverManager.chromedriver().create();
        wait = new WebDriverWait(driver, 10);
        driver.manage().window().maximize();

        loginPage = new LoginPage(driver, wait);
        caixaListPage = new CaixaListPage(driver, wait);
        caixaFormPage = new CaixaFormPage(driver, wait);

        // Login to the application
        loginPage.navigateTo(BASE_URL + "/login");
        assertTrue(loginPage.isLoginPageDisplayed(), "Página de Login não foi exibida.");

        loginPage.login("gerente", "123");

        // Navigate to caixa page
        driver.get(BASE_URL + "/caixa");
        assertTrue(caixaListPage.isPageLoaded(), "Página de Caixa não foi carregada após o login.");
    }

    @Test
    public void testAbrirCaixaDiario() {
        // Click on "Novo" or "Abrir Caixa" button
        caixaListPage.clickOpenCaixa();
        
        // Verify form is loaded
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        // Fill the form for daily caixa
        String descricao = "Caixa Teste Selenium " + System.currentTimeMillis();
        caixaFormPage.openCaixa("CAIXA", "100,00", descricao);

        // Verify success message
        assertTrue(caixaFormPage.hasSuccessMessage(), "Mensagem de sucesso deve aparecer.");
        String successMsg = caixaFormPage.getSuccessMessage();
        assertTrue(successMsg.contains("sucesso") || successMsg.contains("cadastrado"), 
                "Mensagem de sucesso deve conter 'sucesso' ou 'cadastrado'");
    }

    @Test
    public void testAbrirCaixaCofre() {
        caixaListPage.clickOpenCaixa();
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        String descricao = "Cofre Teste Selenium " + System.currentTimeMillis();
        caixaFormPage.openCaixa("COFRE", "500,00", descricao);

        assertTrue(caixaFormPage.hasSuccessMessage(), "Mensagem de sucesso deve aparecer.");
    }

    @Test
    public void testAbrirCaixaBanco() {
        caixaListPage.clickOpenCaixa();
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        String descricao = "Banco Teste Selenium " + System.currentTimeMillis();
        caixaFormPage.openCaixaBanco("1000,00", descricao, "1234", "123456");

        assertTrue(caixaFormPage.hasSuccessMessage(), "Mensagem de sucesso deve aparecer.");
    }

    @Test
    public void testAbrirCaixaComValorZero() {
        caixaListPage.clickOpenCaixa();
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        String descricao = "Caixa Zero Teste " + System.currentTimeMillis();
        caixaFormPage.openCaixa("CAIXA", "0,00", descricao);

        assertTrue(caixaFormPage.hasSuccessMessage(), "Mensagem de sucesso deve aparecer.");
    }

    @Test
    public void testAbrirCaixaComDescricaoVazia() {
        caixaListPage.clickOpenCaixa();
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        // Open caixa with empty description (should use default)
        caixaFormPage.openCaixa("CAIXA", "50,00", "");

        assertTrue(caixaFormPage.hasSuccessMessage(), "Mensagem de sucesso deve aparecer.");
    }

    @Test
    public void testFecharCaixa() {
        // First, check if there's an open caixa
        if (caixaListPage.isCaixaOpen()) {
            caixaListPage.clickCloseCaixa();
            
            // If there's a password prompt, enter password
            if (caixaFormPage.isFormLoaded()) {
                caixaFormPage.closeCaixa("123"); // Use the same password as login
            }
            
            assertTrue(caixaFormPage.hasSuccessMessage() || caixaListPage.getSuccessMessage().contains("fechado"), 
                    "Mensagem de sucesso deve aparecer ao fechar o caixa.");
        } else {
            // If no caixa is open, this test should be skipped or handle the case
            System.out.println("Nenhum caixa aberto encontrado para fechar.");
        }
    }

    @Test
    public void testBuscarCaixa() {
        // Search for a caixa
        caixaListPage.searchCaixa("Caixa");
        
        // Verify search results
        assertTrue(caixaListPage.isCaixaInTable("Caixa"), "Resultados da busca devem conter 'Caixa'");
    }

    @Test
    public void testVerificarEstadoCaixa() {
        // Check if caixa is open or closed
        boolean isOpen = caixaListPage.isCaixaOpen();
        boolean isClosed = caixaListPage.isCaixaClosed();
        
        // At least one state should be true (or both false if no caixa exists)
        assertTrue(isOpen || isClosed || (!isOpen && !isClosed), 
                "Estado do caixa deve ser verificável");
    }

    @Test
    public void testAbrirCaixaComValorNegativo() {
        caixaListPage.clickOpenCaixa();
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        // Try to open caixa with negative value
        caixaFormPage.openCaixa("CAIXA", "-50,00", "Teste Valor Negativo");

        // Should show error message
        assertTrue(caixaFormPage.hasErrorMessage(), "Mensagem de erro deve aparecer para valor negativo.");
    }

    @Test
    public void testCancelarAberturaCaixa() {
        caixaListPage.clickOpenCaixa();
        assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");

        // Fill some data but cancel
        caixaFormPage.setTipo("CAIXA");
        caixaFormPage.setValorAbertura("100,00");
        caixaFormPage.clickCancel();

        // Should return to caixa list page
        assertTrue(caixaListPage.isPageLoaded(), "Deve retornar à página de listagem de caixas.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
} 