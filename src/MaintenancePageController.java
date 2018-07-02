import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import maintenance.AllRequests;
import maintenance.Request;
import maintenance.RequestData;

public class MaintenancePageController {

	Stage stage;
	AllRequests requests;
	String[] responses;
	
	@FXML
	private Button backButton;
	
	@FXML
	private Button employeeDataButton;
	
	@FXML
	private Button employeeMasterDataButton;
	
	@FXML
	private Button employeeTimeDataButton;
	
	@FXML
	private Button employeeServiceButton;
	
	@FXML
	private Button tripSyncButton;
	
	@FXML
	private Button silentSyncButton;
	
	public void initializeScene(Stage stage){
		
		this.stage = stage;
		
		EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				System.out.println(((Button)event.getSource()).getId());
				String buttonId = ((Button)event.getSource()).getId();
				
				if(buttonId.equals("backButton")){
					
					try {
						FXMLLoader loader = new FXMLLoader(getClass().getResource("StartPage.fxml"));
						Parent root = loader.load();
						((StartPageController) loader.getController()).setStage(stage);
						stage.setTitle("PWC App Installs Reporting Tool");
						stage.setScene(new Scene(root, 400, 300));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else{
					showAlertForButton(buttonId);
				}
			}
		};
		
		backButton.setOnMouseClicked(mouseHandler);
		employeeDataButton.setOnMouseClicked(mouseHandler);
		employeeMasterDataButton.setOnMouseClicked(mouseHandler);
		employeeTimeDataButton.setOnMouseClicked(mouseHandler);
		employeeServiceButton.setOnMouseClicked(mouseHandler);
		tripSyncButton.setOnMouseClicked(mouseHandler);
		silentSyncButton.setOnMouseClicked(mouseHandler);
		
		Task<String[]> startRequests = startRequests();
		startRequests.setOnSucceeded(processResponses(startRequests));
		
		new Thread(startRequests).start();
		
	}
	
	protected void showAlertForButton(String buttonId) {
		// TODO Auto-generated method stub
		if(buttonId.equals("employeeDataButton")){
			showAlertForRequest(requests.employeeData, 0);
		}
		else if(buttonId.equals("employeeMasterDataButton")){
			showAlertForRequest(requests.employeeMasterData,1);
		}
		else if(buttonId.equals("employeeTimeDataButton")){
			showAlertForRequest(requests.employeeTimeData,2);
		}
		else if(buttonId.equals("employeeServiceButton")){
			showAlertForRequest(requests.employeeService,3);
		}
		else if(buttonId.equals("tripSyncButton")){
			showAlertForRequest(requests.tripSync,4);
		}
		else if(buttonId.equals("silentSyncButton")){
			showAlertForRequest(requests.silentSync,5);
		}
	}

	private void showAlertForRequest(RequestData data, int i) {
		// TODO Auto-generated method stub
		Alert alert = new Alert(AlertType.INFORMATION);
		StringBuilder builder = new StringBuilder();
		builder.append("URL: " + data.getUrl() + "\n");
		builder.append("Post body: " + data.getPostBody() + "\n");
		if(data.getHeaderName() != null){
			builder.append("Header Name: " + data.getHeaderName() + "\n");
			builder.append("Header Value: " + data.getHeaderValue() + "\n");
		}
		alert.setContentText(builder.toString());
		
		Label label = new Label("Response:");
		
		
		TextArea textArea = new TextArea(responses[i]);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
		
		alert.getDialogPane().setExpandableContent(expContent);
		
		alert.show();
	}

	private EventHandler<WorkerStateEvent> processResponses(Task<String[]> startRequests) {
		// TODO Auto-generated method stub
		return new EventHandler<WorkerStateEvent>() {
			
			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub
				Platform.runLater(processResponseAndEnableUI(startRequests.getValue()));
			}

			private Runnable processResponseAndEnableUI(String[] value) {
				// TODO Auto-generated method stub
				return new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						
						try {
							responses = value;
							employeeDataButton.setText((isRequestSuccessful(value[0])) ? "Pass" : "Fail");
							employeeDataButton.setDisable(false);
							employeeMasterDataButton.setText((isRequestSuccessful(value[1])) ? "Pass" : "Fail");
							employeeMasterDataButton.setDisable(false);
							employeeTimeDataButton.setText((isRequestSuccessful(value[2])) ? "Pass" : "Fail");
							employeeTimeDataButton.setDisable(false);
							employeeServiceButton.setText((isRequestSuccessful(value[3])) ? "Pass" : "Fail");
							employeeServiceButton.setDisable(false);

							tripSyncButton.setText((isRequestSuccessful(value[4])) ? "Pass" : "Fail");
							tripSyncButton.setDisable(false);
							
							silentSyncButton.setText((isRequestSuccessful(value[5])) ? "Pass" : "Fail");
							silentSyncButton.setDisable(false);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				};
			}
		};
	}

	private Task<String[]> startRequests(){
		
		return new Task<String[]>() {
			
			@Override
			protected String[] call() throws Exception {
				// TODO Auto-generated method stub
				requests = new AllRequests();
				String[] responses = null;
				try {
					String employeeDataResponse = Request.execute(requests.employeeData);
					String employeeMasterDataResponse = Request.execute(requests.employeeMasterData);
					String employeeTimeDataResponse = Request.execute(requests.employeeTimeData);
					String employeeServiceResponse = Request.execute(requests.employeeService);
					String tripSyncResponse = Request.execute(requests.tripSync);
					String silentSyncResponse = Request.execute(requests.silentSync);
					
					responses = new String[]{
							employeeDataResponse, employeeMasterDataResponse, 
							employeeTimeDataResponse, employeeServiceResponse,
							tripSyncResponse, silentSyncResponse
							};
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return responses;
			}
		};
	}
	
	private boolean isRequestSuccessful(String response) throws ParseException{
		JSONObject obj = (JSONObject) new JSONParser().parse(response);
		JSONObject status = (JSONObject) obj.get("Status");
		return ((String)status.get("status")).equals("OK") ? true : false;
	}
}
