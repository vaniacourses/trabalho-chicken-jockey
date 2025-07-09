package net.originmobi.pdv.selenium.pages.login;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
	private WebDriver driver;
	private WebDriverWait wait;

	private By usernameField = By.id("user");
	private By passwordField = By.id("password");
	private By loginButton = By.id("btn-login");

	private By loginHeader = By.xpath("//div[@id='painelLogin']");

	private By successMessage = By.cssSelector(".alert-success span");
	private By errorMessage = By.cssSelector(".alert-danger span");

	public LoginPage(WebDriver driver, WebDriverWait wait) {
		this.driver = driver;
		this.wait = wait;
	}

	public void navigateTo(String url) {
		driver.get(url);
		wait.until(ExpectedConditions.visibilityOfElementLocated(loginHeader));
	}

	public boolean isLoginPageDisplayed() {
		return driver.findElement(loginHeader).isDisplayed();
	}

	public void enterUsername(String username) {
		WebElement user = wait.until(ExpectedConditions.elementToBeClickable(usernameField));
		user.clear();
		user.sendKeys(username);
	}

	public void enterPassword(String password) {
		WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
		pass.clear();
		pass.sendKeys(password);
	}

	public void clickLoginButton() {
		WebElement button = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
		button.click();
	}

	public void login(String username, String password) {
		enterUsername(username);
		enterPassword(password);
		clickLoginButton();
	}

	public String getSuccessMessage() {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage)).getText();
	}

	public String getErrorMessage() {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
	}
}
