package net.originmobi.pdv.selenium.pages.recebimento;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RecebimentoListPage {
	private WebDriver driver;
	private WebDriverWait wait;

	private By pageHeader = By.xpath("//h1[contains(text(),'Receber')]");
	private By newRecebimentoButton = By.xpath("//div[@id='btn-padrao']/a[contains(text(),'Novo')]");

	public RecebimentoListPage(WebDriver driver, WebDriverWait wait) {
		this.driver = driver;
		this.wait = wait;
	}

	public boolean isPageLoaded() {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).isDisplayed()
				|| wait.until(ExpectedConditions.visibilityOfElementLocated(newRecebimentoButton)).isDisplayed();
	}

}