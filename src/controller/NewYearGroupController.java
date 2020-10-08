package controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import model.YearGroup;
import model.YearGroupDAO;
import util.ViewLoader.View;

public class NewYearGroupController {
	/* ---------------- Fields ---------------- */
    @FXML
    private TextField academicYear;
    @FXML
    private Button save;
    @FXML 
    private Label yearEnd;
    
    private RootLayoutController rootLayoutController;
    private ObservableList<YearGroup> allYearGroups;

    /* -------- Constructor -------- */
    public NewYearGroupController(RootLayoutController rootLayoutController, ObservableList<YearGroup> allYearGroups) {
    	this.rootLayoutController = rootLayoutController;
		this.allYearGroups = allYearGroups;
	}

	/* ---------------- Methods ---------------- */
    @FXML
    private void initialize() {
    	// ------- Event Handlers -------  
    	// Save Button
    	save.setOnAction((event) -> {
    		int academicYearEntered = Integer.parseInt(academicYear.getText().trim());
    		List<Integer> allAcademicYears = allYearGroups.stream()
    				.map(YearGroup::getAcademicYear)
    				.collect(Collectors.toList());
    		if(allAcademicYears.contains(academicYearEntered)) {
    			int academicYearEnd = Integer.parseInt(String.valueOf(academicYearEntered).substring(2, 4)) + 1;
        		Alert alert = new Alert (Alert.AlertType.ERROR);
            	alert.setTitle("Add New Year Group");
            	alert.setHeaderText("Unable to save year group");
            	alert.setContentText("Year group for academic year " + academicYearEntered + "/" + 
            			academicYearEnd + " already exists"
            	);
            	alert.showAndWait();
    		}
    		else {
        		YearGroup yearGroup = new YearGroup();
            	yearGroup.setAcademicYear(academicYearEntered);
            	YearGroupDAO.upsertYearGroup(yearGroup);
            	allYearGroups.add(yearGroup);
            	allYearGroups.sort(Comparator.comparing(YearGroup::getAcademicYear).reversed());
            	rootLayoutController.removeTab(View.NEW_YEAR_GROUP.getTabName());	
    		}
    	});
    	
        // Year End Label 
    	academicYear.textProperty().addListener((observable, oldValue, newValue) -> {
    		if(newValue.length() == 4) {
        		int newYearEnd = Integer.parseInt(String.valueOf(newValue).substring(2, 4)) + 1;
        		yearEnd.textProperty().setValue(String.format("%02d", newYearEnd));
    		}
    		else {
    			yearEnd.textProperty().setValue(null);
    		}
        });
    	
    	// ------- Bindings -------    	
    	save.disableProperty().bind(
    			Bindings.isEmpty(academicYear.textProperty())
    			.or(Bindings.notEqual(academicYear.textProperty().length(), 4))
    	);
    	
    	
    	// ------- Form Validation -------  
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/15615890/recommended-way-to-restrict-input-in-javafx-textfield
         * ACCESSED: 05/05/2020
         */
    	TextFormatter<String> academicYearFormatter = new TextFormatter<>(change -> {
    		String input = change.getText();
    		String fieldValue = change.getControlNewText();
     	    if ( change.isDeleted() || (input.matches("^[0-9]$")) && fieldValue.length() <= 4) {
     	        return change;
     	    }
     	    return null;
    	});
    	/* END REFERENCE */
    	
        // ------- Initial Display -------
    	// ObservableList is already ordered descendingly so we can just call get(0)
    	// Set the default value to the next year if at least 1 year group already exist
    	if (allYearGroups.size() > 0) {
    		academicYear.setText(String.valueOf(allYearGroups.get(0).getAcademicYear() + 1));
    	}
    	// Need to add the validator after we've set the default value so it updates
    	academicYear.setTextFormatter(academicYearFormatter);
    }
}
