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
    public void testAbrirCaixa() {
        boolean success = false;
        try {
            caixaListPage.clickOpenCaixa();
            assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");
            String descricao = "Caixa Selenium " + System.currentTimeMillis();
            caixaFormPage.openCaixa("CAIXA", "100,00", descricao);
            assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após abrir caixa.");
            success = caixaListPage.hasSuccessMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(success, "Mensagem de sucesso deve aparecer ao abrir caixa.");
    }

    @Test
    public void testAbrirCaixa2() {
        boolean success = false;
        try {
            caixaListPage.clickOpenCaixa();
            assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");
            String descricao = "Caixa Selenium 2 " + System.currentTimeMillis();
            caixaFormPage.openCaixa("CAIXA", "120,00", descricao);
            assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após abrir caixa.");
            success = caixaListPage.hasSuccessMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(success, "Mensagem de sucesso deve aparecer ao abrir caixa.");
    }

    @Test
    public void testFecharCaixa() {
        boolean success = false;
        try {
            // Tenta acessar gerenciamento, se não houver caixa aberto, abre um primeiro
            try {
                caixaListPage.clickManageCaixa();
            } catch (Exception e) {
                caixaListPage.clickOpenCaixa();
                assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");
                String descricao = "Caixa Selenium Fechar " + System.currentTimeMillis();
                caixaFormPage.openCaixa("CAIXA", "130,00", descricao);
                assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após abrir caixa.");
                caixaListPage.clickManageCaixa();
            }
            caixaListPage.clickBtnFecharCaixa();
            caixaFormPage.setAdminSenha("123");
            caixaFormPage.clickModalFecharCaixa();
            assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após fechar caixa.");
            success = caixaListPage.hasSuccessMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(success, "Mensagem de sucesso deve aparecer ao fechar o caixa.");
    }

    @Test
    public void testAbrirCofre() {
        boolean success = false;
        try {
            caixaListPage.clickOpenCaixa();
            assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");
            String descricao = "Cofre Selenium " + System.currentTimeMillis();
            caixaFormPage.openCaixa("COFRE", "500,00", descricao);
            assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após abrir cofre.");
            success = caixaListPage.hasSuccessMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(success, "Mensagem de sucesso deve aparecer ao abrir cofre.");
    }

    @Test
    public void testBuscarCaixa() {
        boolean found = false;
        try {
            caixaListPage.clickOpenCaixa();
            assertTrue(caixaFormPage.isFormLoaded(), "Formulário de abertura de caixa não foi carregado.");
            String descricao = "Caixa Selenium Busca " + System.currentTimeMillis();
            caixaFormPage.openCaixa("CAIXA", "140,00", descricao);
            assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após abrir caixa.");
            caixaListPage.searchCaixa("Caixa");
            assertTrue(caixaListPage.waitForListPageLoaded(), "Página de listagem de caixas não carregou após busca.");
            found = caixaListPage.isCaixaInTable("Caixa");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(found, "Resultados da busca devem conter 'Caixa'");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
} 