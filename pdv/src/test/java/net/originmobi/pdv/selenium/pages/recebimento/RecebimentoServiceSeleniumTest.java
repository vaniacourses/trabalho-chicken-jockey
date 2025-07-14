package net.originmobi.pdv.selenium.pages.recebimento;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.originmobi.pdv.selenium.pages.login.LoginPage;

public class RecebimentoServiceSeleniumTest {
    
    protected WebDriver driver;
	protected WebDriverWait wait;
	protected LoginPage loginPage;
    protected RecebimentoListPage recebimentoListPage;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
	public void setup() {
		driver = WebDriverManager.chromedriver().create();
		wait = new WebDriverWait(driver, 10);
		driver.manage().window().maximize();

		loginPage = new LoginPage(driver, wait);
        recebimentoListPage = new RecebimentoListPage(driver, wait);

		loginPage.navigateTo(BASE_URL + "/login");
		assertTrue(loginPage.isLoginPageDisplayed(), "Página de Login não foi exibida.");

		loginPage.login("gerente", "123");

		driver.get(BASE_URL + "/receber");
		assertTrue(recebimentoListPage.isPageLoaded(), "Página de Recebimentos não foi carregada após o login.");
    }

	@Test
	public void testBuscarRecebimento() {
        WebElement dropdown = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.dropdown-toggle.selectpicker.btn-default")));
        dropdown.click();

        WebElement searchBox = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".codcliente .bs-searchbox input")));
        assertTrue(searchBox.isDisplayed(), "Campo de busca deve estar visível.");
        searchBox.sendKeys("João Rafael Mendes Nogueira");
        WebElement option = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//li[contains(@class, 'active') and .//span[@class='text' and normalize-space(text())='João Rafael Mendes Nogueira']]")
            )
        );
        assertTrue(option.isDisplayed(), "Opção de cliente deve estar visível.");
        option.click();

        driver.findElement(By.cssSelector("a.btn-busca-cliente-receber")).click();

		WebElement tableBody = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.tabela-receber")));
		assertTrue(tableBody.getText().contains("João Rafael Mendes Nogueira"), "Recebimento do cliente deve aparecer após a busca.");
	}

	@Test
	public void testRealizarRecebimento() {
		WebElement dropdown = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.dropdown-toggle.selectpicker.btn-default")));
        dropdown.click();

        WebElement searchBox = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".codcliente .bs-searchbox input")));
        assertTrue(searchBox.isDisplayed(), "Campo de busca deve estar visível.");
        searchBox.sendKeys("João Rafael Mendes Nogueira");
        WebElement option = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//li[contains(@class, 'active') and .//span[@class='text' and normalize-space(text())='João Rafael Mendes Nogueira']]")
            )
        );
        assertTrue(option.isDisplayed(), "Opção de cliente deve estar visível.");
        option.click();

        driver.findElement(By.cssSelector("a.btn-busca-cliente-receber")).click();

		WebElement tableBody = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.tabela-receber")));
		assertTrue(tableBody.getText().contains("João Rafael Mendes Nogueira"), "Recebimento do cliente deve aparecer após a busca.");

		WebElement checkbox = tableBody.findElement(By.cssSelector("input[type='checkbox'].marcado"));
		checkbox.click();

        WebElement receberVariosBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-receber-varios")));
        receberVariosBtn.click();

        wait.until(ExpectedConditions.urlContains("/recebimento"));

        WebElement tituloSelect = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("titulo")));
        new Select(tituloSelect).selectByIndex(1); // Seleciona "Pagamento em dinheiro"

        WebElement valorInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("vlrecebido")));
        valorInput.clear();
        valorInput.sendKeys("1"); // Insere um valor de recebimento (1 centavo)

        WebElement receberBtn = driver.findElement(By.cssSelector(".btn-receber-titulo"));
        receberBtn.click();

		wait.until(ExpectedConditions.alertIsPresent());
		org.openqa.selenium.Alert alert = driver.switchTo().alert();
		assertTrue(alert.getText().contains("sucesso") || alert.getText().toLowerCase().contains("recebimento"),
				"Mensagem de sucesso deve aparecer após realizar recebimento.");
		alert.accept();
	}

	@Test
	public void testReceberSemSelecionarParcela() {
		// Garante que nenhum checkbox está selecionado
		WebElement table = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.tabela-receber")));
		for (WebElement checkbox : table.findElements(By.cssSelector("input[type='checkbox'].marcado"))) {
			if (checkbox.isSelected()) {
				checkbox.click();
			}
		}
		WebElement receberVariosBtn = wait.until(
				ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-receber-varios")));
		receberVariosBtn.click();

		assertTrue(driver.getCurrentUrl().contains("/receber"), "Deve permanecer na tela de receber se nenhuma parcela for selecionada.");
	}
}
