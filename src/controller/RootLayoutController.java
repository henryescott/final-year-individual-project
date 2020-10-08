package controller;

import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import model.YearGroup;
import model.YearGroupDAO;
import util.ViewLoader;
import util.ViewLoader.View;

public class RootLayoutController {
	/* ---------------- Fields ---------------- */
    @FXML
    private MenuItem importSubmissions;
    @FXML
    private MenuItem close;
	@FXML
    private MenuItem yearGroups;
    @FXML
    private MenuItem questions;
    @FXML
    private MenuItem submissions;
    @FXML
    private MenuItem settings;
    @FXML
    private TabPane tabPane;
    
    private Stage mainStage;
    private Map<String, Tab> openTabs;
    private ObservableList<YearGroup> allYearGroups;
    
    /* -------------- Constructor -------------- */
    public RootLayoutController(Stage mainStage) {
    	this.mainStage = mainStage;
    	this.openTabs = new HashMap<String, Tab>();
	}
    
    /* -------- Accessors and Mutators -------- */
	public Stage getMainStage() {
		return mainStage;
	}
        
    /* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {  
    	allYearGroups = FXCollections.observableArrayList(YearGroupDAO.getAll());
    	
    	// ------- Event Handlers -------    
    	// Import Submissions Menu Item
    	importSubmissions.setOnAction(actionEvent -> {
    		ImportSubmissionsController controller = new ImportSubmissionsController(this, allYearGroups);
    		addTab(View.IMPORT_SUBMISSIONS, controller, View.IMPORT_SUBMISSIONS.getTabName());
    	});
    	
    	// Import Submissions Menu Item
    	close.setOnAction(actionEvent -> System.exit(0));
    	
    	// Year Groups Menu Item
    	yearGroups.setOnAction(actionEvent -> {
    		YearGroupsController controller = new YearGroupsController(this, allYearGroups);
    		addTab(View.YEAR_GROUPS, controller, View.YEAR_GROUPS.getTabName());
    	});
    	
    	// Questions Menu Item
    	questions.setOnAction(actionEvent -> {
    		QuestionsController controller = new QuestionsController(this, allYearGroups);
    		addTab(View.QUESTIONS, controller, View.QUESTIONS.getTabName());
    	});
    	
    	// Submissions Menu Item
    	submissions.setOnAction(actionEvent -> {
    		SubmissionsController controller = new SubmissionsController(this, allYearGroups);
    		addTab(View.SUBMISSIONS, controller, View.SUBMISSIONS.getTabName());
    	});
        	
    	// Settings Menu Item
    	settings.setOnAction(actionEvent -> {
    		SettingsController controller = new SettingsController();
    		addTab(View.SETTINGS, controller, View.SETTINGS.getTabName());
    	});
    	
    	// ------- Initial Display -------
    	// Simulate submissions button being clicked 
    	submissions.fire();
    }
    
    public void addTab(View view, Object controller, String tabName) {
    	Tab tab = new Tab();
    	// Check if the tab is already open
    	if(openTabs.containsKey(tabName)) {
    		tab = openTabs.get(tabName);
    	}
    	else {
    		tab.setText(tabName);
    		tab.setContent(ViewLoader.loadFXML(view, controller));
    		tabPane.getTabs().add(tab);
    		tab.setOnClosed(event -> openTabs.remove(tabName));	
    		openTabs.put(tabName, tab);
    	}
		// Switch to the tab
		tabPane.getSelectionModel().select(tab);
    }
    
    public void removeTab(String tabName) {
    	// Check if the tab is already open
    	if(openTabs.containsKey(tabName)) {
    		Tab tab = openTabs.get(tabName);
    		tabPane.getTabs().remove(tab);
    		// Closing the tab programmatically as above doesn't trigger the onClosed event handler
    		// for some reason, so need to manually remove from our openTabs List
    		openTabs.remove(tabName);    		
    	}	
    }
}
