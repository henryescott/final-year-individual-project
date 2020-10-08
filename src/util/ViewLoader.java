package util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import main.Main;

public class ViewLoader {
	public enum View {
		ANSWER_COMPARISON("AnswerComparison"),
		ANSWERS("Answers"),
		IMPORT_SUBMISSIONS("ImportSubmissions", "Import Submissions"), 
		NEW_QUESTION_FORM("NewQuestion", "Add New Question"),
		QUESTIONS("Questions", "Questions"),
		ROOT_LAYOUT("RootLayout"),
		SETTINGS("Settings", "Database Settings"),
		SUBMISSIONS("Submissions", "Submissions"),
		YEAR_GROUPS("YearGroups", "Year Groups"),
		NEW_YEAR_GROUP("NewYearGroup", "Add New Year Group");
		
		private String viewName;
		private String tabName;
		
	    View(String viewName) {
	    	this.viewName = viewName;
	    }
	    
	    View(String viewName, String tabName) {
	    	this.viewName = viewName;
	    	this.tabName = tabName;
	    }
	    
	    public String getViewName() {
	    	return viewName;
	    }
	    
	    public String getTabName() {
	    	return tabName;
	    }
	}
	
	public static Pane loadFXML(View view, Object controller) {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/" + view.getViewName() + ".fxml"));
        loader.setController(controller);
		try {
			return loader.load();
		} catch (IOException e) {	
			Alert alert = new Alert (Alert.AlertType.ERROR);
        	alert.setTitle("Error Loading Content");
        	alert.setHeaderText("An unexpected error occured");
        	alert.setContentText("Unable to load the " + view.getViewName() + " tab.");
			alert.showAndWait();
		}
		return null;
    }
}