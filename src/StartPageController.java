import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class StartPageController {

	WebDriver driver;
	WebDriverWait webDriverWait;
	String totalIOSApps;
	String totalApps;

	XSSFWorkbook excelWBook;
	XSSFSheet excelWSheet;
	XSSFCell excelCell;
	XSSFRow excelRow;

	Stage stage;

	@FXML
	private Text statusText;

	@FXML
	private Button btnStart;

	@FXML
	private MenuItem menuItemSettings;
	
	@FXML
	private MenuItem menuItemMaintenance;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML
	protected void onSettingsClicked(ActionEvent event) throws IOException {
		statusText.setText("Settings clicked!");
		statusText.setVisible(true);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsPage.fxml"));
		Parent root = loader.load();
		((SettingsPageController) loader.getController()).initializeScene(stage);
		stage.setScene(new Scene(root, 400, 400));

		// Alert alert = new Alert(AlertType.INFORMATION, "Information Alert
		// Dialog", ButtonType.OK);
		// alert.show();

	}
	
	@FXML
	protected void onMaintenanceClicked(ActionEvent event) throws IOException{
		//System.out.println("Maintenance Clicked");
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MaintenancePage.fxml"));
		Parent root = loader.load();
		((MaintenancePageController) loader.getController()).initializeScene(stage);
		stage.setScene(new Scene(root, 400, 400));
	}

	@FXML
	protected void onExitClicked(ActionEvent event) {

		if (driver != null)
			driver.quit();

		System.exit(0);
	}

	@FXML
	protected void onStartClicked(ActionEvent event) throws Exception {

		btnStart.setDisable(true);
		menuItemSettings.setDisable(true);
		statusText.setText("Initializing. Please wait...");
		statusText.setVisible(true);

		Task<LinkedHashMap<String, Long>> countTask = countTask();
		countTask.setOnSucceeded(onCountSuccess(countTask));

		new Thread(countTask).start();

	}

	private EventHandler<WorkerStateEvent> onCountSuccess(Task<LinkedHashMap<String, Long>> task) {
		return new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub

				// FileChooser fileChooser = new FileChooser();
				// fileChooser.setTitle("Save Excel File");
				// fileChooser.getExtensionFilters().add(new
				// ExtensionFilter("Excel Document", "*.xlsx"));
				// File appCountFile = fileChooser.showSaveDialog(stage);
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
				File appCountFile = new File("Download_Counts_" + dateFormat.format(new Date()) + ".xlsx");
				new Thread(saveTask(task.getValue(), appCountFile)).start();
			}
		};
	}

	private Task<LinkedHashMap<String, Long>> countTask() throws Exception {

		return new Task<LinkedHashMap<String, Long>>() {

			@Override
			protected LinkedHashMap<String, Long> call() throws Exception {
				// TODO Auto-generated method stub
				try {

					return getAllInstallCounts(Constants.APP_NAMES);

					// saveCount(allInstallCounts);

				} catch (Exception e) {
					if (driver != null) {
						try {
							File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
							FileUtils.copyFile(scrFile, new File("ErrorScreen.png"));
							File pageSource = new File("ErrorPageSource.html");
							pageSource.createNewFile();
							FileWriter writer;
							writer = new FileWriter(pageSource);
							writer.write(driver.getPageSource());
							writer.close();
						} catch (IOException e1) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									StackTraceDialog.display(e1);
								}
							});
						}
					}
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							StackTraceDialog.display(e);
						}
					});

				} finally {
					if (driver != null)
						driver.quit();
				}
				return null;

			}
		};

	}

	private Task<Void> saveTask(LinkedHashMap<String, Long> appData, File file) {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				// TODO Auto-generated method stub

				saveCount(appData, file);
				return null;
			}
		};
	}

	private LinkedHashMap<String, Long> getAllInstallCounts(List<String> appList) throws Exception {
		// TODO Auto-generated method stub
		setup();

		LinkedHashMap<String, Long> appsData = new LinkedHashMap<>();

		// String[] apps = { "MobileTime" };

		for (String app : Constants.APP_NAMES) {
			statusText.setText("Fetching install counts for: " + app + "...");
			long count = getAppCountFor(app);
			appsData.put(app, count);
		}

		statusText.setText("Fetched install counts for all apps. Saving now...");
		return appsData;
	}

	/*
	 * This method is used to set up the browser by logging in with proper
	 * credentials and navigating to the correct page and applying the correct
	 * search filters.
	 */
	private void setup() throws Exception {
		// TODO: Uncomment below if need to check in chrome browser

//		 String webdriverPath = "C:\\SeleniumChromeDriver\\chromedriver.exe";
//		 System.setProperty("webdriver.chrome.driver", webdriverPath);
//		 ChromeOptions options = new ChromeOptions();
//		 options.addArguments("--start-maximized");
//		 driver = new ChromeDriver(options);

		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setJavascriptEnabled(true);
		caps.setCapability("takesScreenshot", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "phantomjs.exe");
		driver = new PhantomJSDriver(caps);

		driver.get("https://mobileregadmin.pwcinternal.com/mifs/login.jsp");
		//driver.get("https://wcmobileregadmin.pwcinternal.com/mifs/login.jsp");
		driver.manage().window().maximize(); // TODO: Comment this when using
												// the Chrome driver

		WebElement username = driver.findElement(By.id("username"));
		WebElement password = driver.findElement(By.id("password"));

		username.sendKeys(Constants.USERNAME);
		password.sendKeys(Constants.PASSWORD);

		driver.findElement(By.id("login-btn")).click();

		webDriverWait = new WebDriverWait(driver, 30);

		WebElement appsLink = webDriverWait
				.until(ExpectedConditions.elementToBeClickable(By.id("w-simplemenuItem-1002")));
		appsLink.click();

		WebElement appCount = webDriverWait
				.until(ExpectedConditions.visibilityOfElementLocated(By.className("count_number")));
		Thread.sleep(2000);
		String allAppCount = appCount.getText();
		totalApps = appCount.getText();

		WebElement iOSRadioBtn = webDriverWait
				.until(ExpectedConditions.elementToBeClickable(By.id("radiofield-1313-inputEl")));
		iOSRadioBtn.click();

		// Wait until the specific app count has appeared
		webDriverWait
				.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.className("count_number"), allAppCount)));
		totalIOSApps = appCount.getText();
		System.out.println("total ios app count : " + totalIOSApps);
	}

	public long getAppCountFor(String appName) throws Exception {
		// TODO Auto-generated method stub

		WebElement searchBox = webDriverWait
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("textfield-1304-inputEl")));
		searchBox.sendKeys(appName);

		WebElement totalApps = driver.findElement(By.className("count_number"));
		String totalAppsString = totalApps.getText();
		// totalIOSApps = (totalIOSApps != null) ? totalIOSApps :
		// totalAppsString;
		// System.out.println(totalAppsString);

		WebElement searchButton = driver.findElement(By.id("mibutton-1320-btnInnerEl"));
		searchButton.click();

		// while (totalApps.getText().equals(totalAppsString)) {
		// // System.out.println("Num of apps on screen:
		// // "+totalApps.getText());
		// System.out.println("=============================");
		// Thread.sleep(1000);
		// }

		webDriverWait.until(
				ExpectedConditions.not(ExpectedConditions.textToBe(By.className("count_number"), totalAppsString)));

		webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadmask-2010")));
		Thread.sleep(1000);

		WebElement table = webDriverWait.until(ExpectedConditions.elementToBeClickable(By.id("tableview-1345"))); // .findElements(By.tagName("div")).get(1)

		List<WebElement> rows = table.findElements(By.tagName("table"));

		// Take screenshot for app
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		scrFile.createNewFile();
		String filename;
//		char[] appNameArray = appName.toCharArray();
//		int size = appNameArray.length;
//		for(int i = 0; i<size; i++){
//			if(!Character.isLetterOrDigit(appNameArray[i]))
//				appNameArray[i]=Character.MIN_VALUE;
//		}
//		String newAppName = appNameArray.toString();
		if (appName.length() > 10 || appName.contains(":"))
			filename = appName.replace(":", "").substring(0, 10);
		else
			filename = appName;
		FileUtils.copyFile(scrFile, new File("screenshots/" + filename + ".png"));

		long total = 0;

		// System.out.println("Number of rows: " + rows.size());
		for (WebElement row : rows) {
			String appNameInRow = row.findElements(By.tagName("td")).get(2).findElement(By.tagName("span")).getText();
			if (appName.equalsIgnoreCase(appNameInRow)) {
				try {
					WebElement spanElement = row.findElements(By.tagName("td")).get(6).findElement(By.tagName("span"));
					total += Long.parseLong(spanElement.getText());
				} catch (NoSuchElementException e) {
					total += 0; // For any apps that are newly published
								// the tag is changed from span to div
				}

				System.out.println(appNameInRow);
			}
		}
		// System.out.println("Total installs for " + appName + ": " + total);

		refreshData();

		return total;

	}

	private void refreshData() {
		// TODO Auto-generated method stub
		System.out.println("=================================================");

		WebElement resetButton = driver.findElement(By.id("button-1319-btnInnerEl"));
		resetButton.click();

		webDriverWait.until(ExpectedConditions.textToBe(By.className("count_number"), totalApps));

		WebElement iOSRadioBtn = webDriverWait
				.until(ExpectedConditions.elementToBeClickable(By.id("radiofield-1313-inputEl")));
		iOSRadioBtn.click();

		// Wait until app count has fully reset
		webDriverWait.until(ExpectedConditions.textToBe(By.className("count_number"), totalIOSApps));

	}

	private void saveCount(LinkedHashMap<String, Long> appData, File file) throws Exception {

		// TODO: Ask user to choose file to save as

		// File appCountFile = new File("C:\\AppCount.xlsx");
		// appCountFile.createNewFile();
		if (!file.exists())
			file.createNewFile();

		FileOutputStream fos = new FileOutputStream(file);

		excelWBook = new XSSFWorkbook();

		excelWSheet = excelWBook.createSheet();

		excelWSheet.createRow(0).createCell(0).setCellValue("Application Name");
		excelWSheet.getRow(0).createCell(1).setCellValue("Count");

		int numApps = appData.size();
		Object[] appNames = appData.keySet().toArray();

		for (int i = 0; i < numApps; i++) {
			excelWSheet.createRow(i + 1).createCell(0).setCellValue((String) appNames[i]);
			excelWSheet.getRow(i + 1).createCell(1).setCellValue(appData.get(appNames[i]));
		}

		excelWBook.write(fos);

		fos.close();

		statusText.setText("App counts saved successfully in " + file.getAbsolutePath());
		btnStart.setDisable(false);
		menuItemSettings.setDisable(false);

	}
}
