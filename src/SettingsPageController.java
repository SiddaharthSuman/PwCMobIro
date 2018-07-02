import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class SettingsPageController {

	Stage stage;
	ObservableList<String> listViewAppsList;
	boolean settingsChanged;

	@FXML
	private TextField inputUsername;

	@FXML
	private TextField inputPassword;

	@FXML
	private ListView<String> listViewApps;

	public void initializeScene(Stage stage) {

		this.stage = stage;
		inputUsername.setText(Constants.USERNAME);
		inputPassword.setText(Constants.PASSWORD);

		listViewAppsList = FXCollections.observableArrayList(Constants.APP_NAMES);
		listViewApps.setItems(listViewAppsList);

	}

	@FXML
	protected void onAddClicked() {
		TextInputDialog inputDialog = new TextInputDialog();
		inputDialog.setTitle("Information");
		inputDialog.setHeaderText("Application name required");
		inputDialog.setContentText("Please enter the application name");
		Optional<String> result = inputDialog.showAndWait();

		if (result.isPresent()) {
			settingsChanged = true;
			listViewAppsList.add(result.get());
			listViewApps.scrollTo(listViewAppsList.size() - 1);
		}
	}

	@FXML
	protected void onDeleteClicked() {
		int selectedIndex = listViewApps.getSelectionModel().getSelectedIndex();
		if (selectedIndex == -1) {
			// show an alert that user has not made any selection
			Alert alert = new Alert(AlertType.INFORMATION, "Please select an application from the list to delete",
					ButtonType.OK);
			alert.setHeaderText("No application selected!");
			alert.show();

		} else {
			// display confirmation box before deleting
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("Are you sure?");
			alert.setContentText(
					"You are about to delete an application. Are you sure you want to delete this application?");
			ButtonType buttonYes = new ButtonType("Yes", ButtonData.OK_DONE);
			ButtonType buttonNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().clear();
			alert.getButtonTypes().addAll(buttonYes, buttonNo);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == buttonYes) {
				settingsChanged = true;
				listViewAppsList.remove(selectedIndex);
			}

		}
	}

	@FXML
	protected void onSaveClicked() throws Exception {
		// get all data and save it
		saveAllDetails();

		// Go back to previous page
		backToStart();
	}

	private void saveAllDetails() {
		// First save username and password
		File credentialsFile = new File(Constants.CREDENTIALS_FILENAME);
		try {

			FileWriter fw = new FileWriter(credentialsFile);
			fw.write(inputUsername.getText());
			fw.write("\n");
			fw.write(inputPassword.getText());
			fw.close();

			FileReader fr = new FileReader(credentialsFile);
			BufferedReader br = new BufferedReader(fr);

			Constants.USERNAME = br.readLine();
			Constants.PASSWORD = br.readLine();

			br.close();

		} catch (Exception e) {
			StackTraceDialog.display(e);
		}

		// Now save the applications list
		File appNamesFile = new File(Constants.APP_FILENAME);

		try {

			FileWriter fw = new FileWriter(appNamesFile);
			for (String app : listViewAppsList) {
				fw.write(app + ",\n");
			}

			fw.close();

		} catch (Exception e) {

			StackTraceDialog.display(e);
		}

		// Read back from the file
		try {
			FileReader fr = new FileReader(appNamesFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			Constants.APP_NAMES = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				Constants.APP_NAMES.add(line.substring(0, line.length() - 1));
			}

			br.close();

		} catch (Exception e) {
			StackTraceDialog.display(e);
		}
	}

	@FXML
	protected void onCancelClicked() throws IOException {
		if (!inputUsername.getText().equals(Constants.USERNAME) || !inputPassword.getText().equals(Constants.PASSWORD))
			settingsChanged = true;

		if (settingsChanged) {
			// show and wait with confirmation box
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText("There are unsaved changes, do you want to save them?");
			ButtonType buttonYes = new ButtonType("Yes", ButtonData.OK_DONE);
			ButtonType buttonNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().clear();
			alert.getButtonTypes().addAll(buttonYes, buttonNo);
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == buttonYes)
				saveAllDetails();
		}

		backToStart();
	}

	private void backToStart() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("StartPage.fxml"));
		Parent root = loader.load();
		((StartPageController) loader.getController()).setStage(stage);
		stage.setTitle("PWC App Installs Reporting Tool");
		stage.setScene(new Scene(root, 400, 300));
	}
}
