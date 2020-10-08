package controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import model.ConnectionSetting;
import model.ConnectionSettingDAO;
import util.CommonComponents;
import util.HibernateUtil;

public class SettingsController {
	/* ---------------- Fields ---------------- */
    @FXML
    private TextField hostName;
    @FXML
    private TextField port;
    @FXML
    private TextField serviceName;
    @FXML
    private TextField userName;
    @FXML
    private PasswordField password;
    @FXML
    private Button testConnection;
    @FXML
    private Button edit;
    @FXML
    private Button save;
    
    private ConnectionSetting connectionSetting;
    
    /* ---------------- Methods ---------------- */
    @FXML
    private void initialize() {
    	// ------- Initialize Variables -------
    	// Grab the existing connection setting from the database
    	connectionSetting = ConnectionSettingDAO.getConnectionSetting();
    	
    	// ------- Bindings -------
    	testConnection.disableProperty().bind(
    		    Bindings.isEmpty(hostName.textProperty())
    		    .or(Bindings.isEmpty(port.textProperty()))
    		    .or(Bindings.isEmpty(serviceName.textProperty()))
    		    .or(Bindings.isEmpty(userName.textProperty()))
    		    .or(Bindings.isEmpty(password.textProperty()))
    	);
    	
    	save.disableProperty().bind(
    			Bindings.not(edit.disabledProperty())
    		    .or(Bindings.isEmpty(hostName.textProperty()))
    		    .or(Bindings.isEmpty(port.textProperty()))
    		    .or(Bindings.isEmpty(serviceName.textProperty()))
    		    .or(Bindings.isEmpty(userName.textProperty()))
    		    .or(Bindings.isEmpty(password.textProperty()))
    	);
    	
    	// ------- Validation -------
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/15615890/recommended-way-to-restrict-input-in-javafx-textfield
         * ACCESSED: 05/05/2020
         */
    	TextFormatter<String> portFormatter = new TextFormatter<String>(change -> {
    		String input = change.getText();
    		String fieldValue = change.getControlNewText();
    		// Port number as a 6-bit unsigned integer, thus ranging from 0 to 65535
    		if (change.isDeleted() || (input.matches("^[0-9]{0,5}$")) && Double.parseDouble(fieldValue) <= 65535) {
    			return change;
    		}
    		return null;
    	});
    	port.setTextFormatter(portFormatter);
    	/* END REFERENCE */
    	
    	// ------- Event Handlers -------
    	// Test Connection
    	testConnection.setOnAction(actionEvent -> {
    		if(testConnection()) {
	    		Alert alert = new Alert (Alert.AlertType.INFORMATION);
	    		alert.setTitle("Test Connection");
	    		alert.setHeaderText("Connection Successful");
	    		alert.setContentText("A connection was successfully established using the entered settings");
	    		alert.showAndWait();
    		}
    	});
    	
    	// Save Button
    	save.setOnAction(actionEvent -> {
        		if(testConnection()) {
	        		if(connectionSetting == null) {
	        			connectionSetting = new ConnectionSetting();
	        		}
	        		connectionSetting.setHostName(hostName.getText().trim());
	        		connectionSetting.setPort(Integer.parseInt(port.getText().trim()));
	        		connectionSetting.setServiceName(serviceName.getText().trim());
	        		connectionSetting.setUsername(userName.getText().trim());
	        		connectionSetting.setPassword(password.getText().trim());
	        		ConnectionSettingDAO.upsertConnectionSetting(connectionSetting);
	        		viewState();
        		}
    	});
    	
    	// Edit Button
    	edit.setOnAction(actionEvent -> editState());
    	
    	// ------- Initial Display -------
    	// Check if a connection setting already exists
    	if(connectionSetting != null) {
    		// Display the existing connection string and make it non-editable
    		hostName.setText(connectionSetting.getHostName());
    		port.setText(Integer.toString(connectionSetting.getPort()));
    		serviceName.setText(connectionSetting.getServiceName());
    		userName.setText(connectionSetting.getUsername());
    		password.setText(connectionSetting.getPassword());
    	}
    	viewState();
    }
    
    private boolean testConnection() {
    	try {
        	HibernateUtil.testConnection(hostName.getText(), port.getText(), serviceName.getText(), 
        			userName.getText(), password.getText());
        	return true;
    	}
    	catch(Exception e) {
        	Alert alert = CommonComponents.Alerts.connectionFailure(e, "Test Connection", "Unable to Establish Connection");
        	alert.showAndWait();
        	return false;
    	}
    }
    
    private void editState() {
    	hostName.setEditable(true);
    	hostName.setDisable(false);
    	port.setEditable(true);
    	port.setDisable(false);
    	serviceName.setEditable(true);
    	serviceName.setDisable(false);
    	userName.setEditable(true);
    	userName.setDisable(false);
    	password.setEditable(true);
    	password.setDisable(false);
    	edit.setDisable(true);
    }
    
    private void viewState() {
    	hostName.setEditable(false);
    	hostName.setDisable(true);
    	port.setEditable(false);
    	port.setDisable(true);
    	serviceName.setEditable(false);
    	serviceName.setDisable(true);
    	userName.setEditable(false);
    	userName.setDisable(true);
    	password.setEditable(false);
    	password.setDisable(true);
    	edit.setDisable(false);
    }
}
