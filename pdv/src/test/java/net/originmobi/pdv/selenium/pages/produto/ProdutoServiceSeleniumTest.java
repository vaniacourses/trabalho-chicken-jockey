package net.originmobi.pdv.selenium.pages.produto;

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

public class ProdutoServiceSeleniumTest {

	protected WebDriver driver;
	protected WebDriverWait wait;
	protected LoginPage loginPage;
	protected ProdutoListPage produtoListPage;

	private static final String BASE_URL = "http://localhost:8080";

	@BeforeEach
	public void setup() {
		driver = WebDriverManager.chromedriver().create();
		wait = new WebDriverWait(driver, 10);
		driver.manage().window().maximize();

		loginPage = new LoginPage(driver, wait);
		produtoListPage = new ProdutoListPage(driver, wait);

		loginPage.navigateTo(BASE_URL + "/login");
		assertTrue(loginPage.isLoginPageDisplayed(), "Página de Login não foi exibida.");

		loginPage.login("gerente", "123");

		driver.get(BASE_URL + "/produto");
		assertTrue(produtoListPage.isPageLoaded(), "Página de Produtos não foi carregada após o login.");
	}

	@Test
	public void testBuscarProduto() {

		WebElement searchInput = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Buscar produtos']")));
		assertTrue(searchInput.isDisplayed(), "Campo de busca deve estar visível.");

		searchInput.sendKeys("Biscoito");

		driver.findElement(By.cssSelector("button[type='submit'] i.glyphicon-search")).click();

		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table.table-striped tbody"),
				"Biscoito"));

		WebElement tableBody = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.table-striped tbody")));
		assertTrue(tableBody.getText().contains("Biscoito"), "Produto 'Biscoito' deve aparecer após a busca.");
	}

	@Test
	public void testCadastrarNovoProduto() {

		WebElement novoButton = wait
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div#btn-padrao a.btn-azul-padrao")));
		novoButton.click();

		WebElement formTitle = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Produto')]")));
		assertTrue(formTitle.isDisplayed(), "Título 'Produto' do formulário deve estar visível.");

		driver.findElement(By.id("descricao")).sendKeys("Produto Teste Selenium " + System.currentTimeMillis());
		driver.findElement(By.id("valorVenda")).sendKeys("50,00");
		driver.findElement(By.id("valorCusto")).sendKeys("25,00");
		driver.findElement(By.id("unidade")).sendKeys("UN");
		driver.findElement(By.id("ncm")).sendKeys("01234567");
		driver.findElement(By.id("cest")).sendKeys("0000001");

		new Select(driver.findElement(By.id("fornecedor"))).selectByIndex(0);
		new Select(driver.findElement(By.id("categoria"))).selectByIndex(0);
		new Select(driver.findElement(By.id("grupo"))).selectByIndex(0);
		new Select(driver.findElement(By.id("balanca"))).selectByValue("NAO");
		new Select(driver.findElement(By.id("ativo"))).selectByValue("ATIVO");
		new Select(driver.findElement(By.name("controla_estoque"))).selectByValue("SIM");
		new Select(driver.findElement(By.id("vendavel"))).selectByValue("SIM");
		new Select(driver.findElement(By.id("st"))).selectByValue("NAO");
		new Select(driver.findElement(By.id("tributacao"))).selectByIndex(0);
		new Select(driver.findElement(By.id("modbc"))).selectByIndex(0);

		driver.findElement(By.cssSelector("input[type='submit'][value='Salvar']")).click();

		WebElement successMessage = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success span")));
		assertTrue(successMessage.getText().contains("Produdo cadastrado com sucesso"),
				"Mensagem de sucesso deve aparecer.");
	}


	@AfterEach
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}
}