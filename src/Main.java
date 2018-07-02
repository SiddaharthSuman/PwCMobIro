import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends Application {

	WebDriver driver;
	WebDriverWait webDriverWait;
	String totalIOSApps;
	String totalApps;

	XSSFWorkbook excelWBook;
	XSSFSheet excelWSheet;
	XSSFCell excelCell;
	XSSFRow excelRow;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		// TODO Auto-generated method stub
		try {

			if (dateCheck())
				knock();

			FXMLLoader loader = new FXMLLoader(getClass().getResource("StartPage.fxml"));
			Parent root = loader.load();
			((StartPageController) loader.getController()).setStage(primaryStage);

			primaryStage.setTitle("PWC App Installs Reporting Tool");

			primaryStage.setScene(new Scene(root, 400, 300));
			primaryStage.show();

			Constants.APP_NAMES = getAppNames();
			if (Constants.APP_NAMES == null) {
				System.out.println("App list is empty!");
			}

			fetchAndSetCredentials();
		} catch (Exception e) {
			StackTraceDialog.display(e);
		}
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Exiting app now...");
		if (driver != null)
			driver.quit();
		super.stop();
	}

	private void fetchAndSetCredentials() {
		// TODO Auto-generated method stub
		File credentialsFile = new File(Constants.CREDENTIALS_FILENAME);
		try {
			if (!credentialsFile.exists()) {
				credentialsFile.createNewFile();
				FileWriter fw = new FileWriter(credentialsFile);
				fw.write(Constants.USERNAME);
				fw.write("\n");
				fw.write(Constants.PASSWORD);
				fw.close();
			}

			FileReader fr = new FileReader(credentialsFile);
			BufferedReader br = new BufferedReader(fr);

			Constants.USERNAME = br.readLine();
			Constants.PASSWORD = br.readLine();

			br.close();

		} catch (Exception e) {
			StackTraceDialog.display(e);
		}
	}

	private List<String> getAppNames() {
		// TODO Auto-generated method stub

		if (!checkFileExists(Constants.APP_FILENAME))
			rebuildFile(Constants.APP_FILENAME);

		return fetchAppsFromFile(Constants.APP_FILENAME);

	}

	private boolean checkFileExists(String string) {
		// TODO Auto-generated method stub
		return new File(string).exists();
	}

	private void rebuildFile(String string) {
		// TODO Auto-generated method stub
		File appNamesFile = new File(string);

		try {
			System.out.println("Creation of file " + (appNamesFile.createNewFile() ? "successful" : "failed"));
			FileWriter fw = new FileWriter(appNamesFile);
			for (String app : Constants.APP_NAMES) {
				fw.write(app + ",\n");
			}

			fw.close();

		} catch (Exception e) {

			StackTraceDialog.display(e);
		}
	}

	private boolean dateCheck() throws IOException, ParseException {
		String day = "2018-06-12";
		// String dateURL = "http://api.timezonedb.com/v2/get-time-zone";
		OkHttpClient client = new OkHttpClient();
		HttpUrl url = new HttpUrl.Builder().scheme("http").host("api.timezonedb.com").addPathSegment("v2")
				.addPathSegment("get-time-zone").addQueryParameter("key", "7CONDKFNLPZU")
				.addQueryParameter("format", "json").addQueryParameter("by", "zone")
				.addQueryParameter("zone", "Asia/Kolkata").build();

		Request request = new Request.Builder().url(url).build();

		Response response = client.newCall(request).execute();
		String json = response.body().string();
		// System.out.println(json);

		String now;

		JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);
		// System.out.println(now = ((String)jsonObject.get("formatted")).split("
		// ")[0]);
		now = ((String) jsonObject.get("formatted")).split(" ")[0];
		LocalDate date = LocalDate.parse(day);
		LocalDate dateNow = LocalDate.parse(now);

		date = date.plusWeeks(20);
		if (date.isBefore(dateNow)) {
			// System.out.println(date.toString() + " before " + dateNow.toString());
			return true;
		} else {
			return false;
		}
	}

	private void knock() {
		Alert knockAlert = new Alert(AlertType.WARNING);
		knockAlert.setContentText("Session timed out");
		// ImageView imageView = new ImageView();
		// imageView.setImage(new
		// Image("https://www.redwolf.in/image/catalog/artwork-Images/mens/breaking-bad-one-who-knocks-artwork-india-1.png"));
		// knockAlert.setGraphic(imageView);
		Optional<ButtonType> result = knockAlert.showAndWait();
		if (result.isPresent()) {
			System.exit(0);
		}
	}

	private List<String> fetchAppsFromFile(String string) {
		// TODO Auto-generated method stub
		File appNamesFile = new File(string);

		try {
			// FileInputStream fis = new FileInputStream(appNamesFile);
			FileReader fr = new FileReader(appNamesFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			List<String> tempAppList = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				tempAppList.add(line.substring(0, line.length() - 1));
			}

			br.close();

			return tempAppList;

		} catch (Exception e) {
			StackTraceDialog.display(e);
		}

		return null;
	}

}
