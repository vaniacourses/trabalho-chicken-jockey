package net.originmobi.pdv.selenium.pages.produto;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProdutoListPage {
	private WebDriver driver;
	private WebDriverWait wait;

	private By pageHeader = By.xpath("//h1[contains(text(),'Produto')]");
	private By newProductButton = By.xpath("//div[@id='btn-padrao']/a[contains(text(),'Novo')]");

	public ProdutoListPage(WebDriver driver, WebDriverWait wait) {
		this.driver = driver;
		this.wait = wait;
	}

	public boolean isPageLoaded() {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).isDisplayed()
				|| wait.until(ExpectedConditions.visibilityOfElementLocated(newProductButton)).isDisplayed();
	}

}