package maintenance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AllRequests {
	
	public RequestData employeeData;
	public RequestData employeeMasterData;
	public RequestData employeeTimeData;
	public RequestData employeeService;
	public RequestData tripSync;
	public RequestData silentSync;

	public AllRequests(){
		LocalDate now = LocalDate.now();
		LocalDate prev = now.minusMonths(1);
		LocalDate next = now.plusMonths(1);
		
		
		employeeData = new RequestData();
		employeeMasterData = new RequestData();
		employeeTimeData = new RequestData();
		employeeService = new RequestData();
		tripSync = new RequestData();
		silentSync = new RequestData();
		
		employeeData.setUrl("http://MATLKPMDMWSP008:9080/mobileTimeWebServices/mt/employeedata"); 
		employeeMasterData.setUrl("http://MATLKPMDMWSP008:9080/mobileTimeWebServices/mt/employeemasterdata"); 
		employeeTimeData.setUrl("http://MATLKPMDMWSP008:9080/mobileTimeWebServices/mt/employeetimedata"); 
		employeeService.setUrl("http://MATLKPMDMWSP008:9080/mobileExpenseServices/me/employeedata"); 
		tripSync.setUrl("http://MATLKPMDMWSP008:9080/mobileExpenseServices/me/tripsync"); 
		silentSync.setUrl("http://MATLKPMDMWSP008:9080/mobileExpenseServices/me/silentsync");
		
		//date format is yyyy-mm-dd || yyyymmdd
		employeeData.setPostBody("{}"); 
		employeeMasterData.setPostBody("{\"employeePersNum\":\"00177731\",\"fromDate\":\""+ prev.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +"\",\"toDate\":\""+ next.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +"\",\"employeeProfile\":\"USPROFEE\"}"); 
		employeeTimeData.setPostBody("{\"fromDate\":\""+ prev.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +"\",\"sign\":\"I\",\"selEmployeeHigh\":\"00177731\",\"selEmployeeLow\":\"00177731\",\"toDate\":\""+ next.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +"\",\"option\":\"BT\",\"textFormat\":\"ITF\",\"longTextFlag\":\"X\"}"); 
		employeeService.setPostBody("{\"SapLoginID\":\"00300177731\"}"); 
		tripSync.setPostBody("{\"Tbl_TripList\":null,\"ToDate\":\""+ next.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +"\",\"CostCenter\":null,\"Tbl_Mileage\":null,\"PersNum\":\"00177731\",\"CompCode\":null,\"ChangeFromDate\":\""+ prev.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +"\",\"FromDate\":\""+ prev.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +"\",\"Tbl_CA_Receipts\":null,\"Tbl_Receipts\":null,\"Tbl_CA_Trip\":null,\"SapLoginID\":\"00300177731\",\"CoArea\":null,\"Tbl_CA_Mileage\":null}"); 
		silentSync.setPostBody("{\"PersNum\":\"00177731\",\"ChangeFromDate\":\""+ prev.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +"\",\"FromDate\":\""+ prev.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +"\",\"ToDate\":\""+ next.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +"\",\"SapLoginID\":\"00300177731\"}");
		
		employeeData.setHeaderName("SAPID"); 
		employeeMasterData.setHeaderName("SAPID"); 
		employeeTimeData.setHeaderName("SAPID"); 
		
		employeeData.setHeaderValue("00300177731"); 
//		employeeData.setHeaderValue("00300337056");
		employeeMasterData.setHeaderValue("00300177731"); 
		employeeTimeData.setHeaderValue("00300177731"); 
	}
}
