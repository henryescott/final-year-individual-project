package controller;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.Tuple;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import model.AnswerDAO;
import model.Question;
import model.QuestionDAO;
import model.YearGroup;
import util.CommonComponents;
import util.ViewLoader.View;

public class NewQuestionController {
	/* ---------------- Fields ---------------- */
    @FXML
    private ComboBox<YearGroup> selectYear;
    @FXML
    private TextField questionNumber;
    @FXML
    private TextField maxMark;
    @FXML
    private TextArea modelAnswer;
    @FXML
    private Button save;
    @FXML
    private Button testQuery;
    
    private RootLayoutController rootLayoutController;
    private ObservableList<YearGroup> allYearGroups;
    private ObjectProperty<YearGroup> selectedYearGroup;
    private YearGroup defaultYearGroup;
    
    /* -------------- Constructor -------------- */
	public NewQuestionController(RootLayoutController rootLayoutController, ObservableList<YearGroup> allYearGroups, YearGroup selectedYearGroup) {
		this.allYearGroups = allYearGroups;
		this.rootLayoutController = rootLayoutController; 
		this.selectedYearGroup = new SimpleObjectProperty<YearGroup>();
		this.defaultYearGroup = selectedYearGroup;
	}

	/* ---------------- Methods ---------------- */
    @FXML
    private void initialize() {
    	// ------- Initial Display Setup -------  
        selectYear.setItems(allYearGroups);
    	selectYear.getSelectionModel().select(defaultYearGroup);
    	
    	// ------- Form Validation -------  
    	// Max Mark Text Field
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/15615890/recommended-way-to-restrict-input-in-javafx-textfield
         * ACCESSED: 05/05/2020
         */
    	TextFormatter<String> doubleFormatter = new TextFormatter<>(change -> {
    		String input = change.getText();
    		String fieldValue = change.getControlNewText();
    		if ((input.matches("^[0-9.]$")) && fieldValue.matches("^[0-9]*.?[0-9]?$") || change.isDeleted()) {
    			return change;
    		}
    		return null;
    	});
    	maxMark.setTextFormatter(doubleFormatter);
    	/* END REFERENCE */
    	
    	// ------- Event Handlers -------  
    	// Save Button
    	save.setOnAction(actionEvent -> saveQuestion());
    	
    	// Test Query
    	testQuery.setOnAction(actionEvent -> {
    		try {
    			Stage resultsWindow = new Stage(); 
        		resultsWindow.initModality(Modality.APPLICATION_MODAL);
        		resultsWindow.setTitle("Test Model Answer");
        		AnchorPane resultsPane = new AnchorPane();
        		Scene scene = new Scene(resultsPane);
        		resultsWindow.setScene(scene);
    			CommonComponents.ResultsTable.createQueryResultsTable(modelAnswer.getText().trim(), resultsPane);
    			resultsWindow.setHeight(400);
    			resultsWindow.setWidth(700);
    			resultsWindow.showAndWait();
    		}
    		// Handle unable to connect to database
    		catch(Exception e) {
    			String alertTitle = "Test Model Answer";
    			String alertHeaderText = "Error Loading Results";
    			Alert alert = CommonComponents.Alerts.connectionFailure(e, alertTitle, alertHeaderText);
    			alert.showAndWait();
    		}
    	});
   
    	// ------- Bindings -------  	
    	save.disableProperty().bind(
    			Bindings.isEmpty(maxMark.textProperty())
    		    .or(Bindings.isEmpty(modelAnswer.textProperty()))
    	);
    	
    	testQuery.disableProperty().bind(
    			Bindings.isEmpty(modelAnswer.textProperty())
    	);
    	
    	questionNumber.textProperty().bind(Bindings.size(
    			selectYear.getSelectionModel().selectedItemProperty().get().questionsProperty()).add(1).asString()		
    	);
    	
    	selectedYearGroup.bind(selectYear.getSelectionModel().selectedItemProperty());
    }
    
    private void saveQuestion() {   
    	try {
    		try {
    			// Check that the SQL entered actually runs
        		List<Tuple> results = AnswerDAO.runQuery(modelAnswer.getText().trim());
        		if (results.size() == 0) {
        			Alert alert = new Alert(Alert.AlertType.ERROR);
        			alert.setTitle("Add New Question");
        			alert.setHeaderText("Unable to Add Question");
        			alert.setContentText("The model answer ran successfully but did not produce any results.");
        			alert.showAndWait();
        		}
        		else {
        			Question question = new Question();
                	question.setYearGroup(selectedYearGroup.get());
                	question.setQuestionNumber(Integer.parseInt(questionNumber.getText().trim()));
                	question.setMaxMarks(Double.parseDouble(maxMark.getText().trim()));
                	question.setModelAnswer(modelAnswer.getText().trim());
                	QuestionDAO.insertQuestion(question);
                	selectedYearGroup.get().getQuestions().add(question);
                	ButtonType addAnotherButton = new ButtonType("Add Another Question", ButtonData.YES);
        			ButtonType noButton = new ButtonType("No", ButtonData.CANCEL_CLOSE);
        			Alert alert = new Alert (Alert.AlertType.CONFIRMATION, "Question added successfully. Would you like to add another question?", addAnotherButton, noButton);
                	alert.setTitle("Add New Question");
                	alert.setHeaderText("Add another question?");
        			alert.showAndWait().ifPresent((btnType) -> {
        				if (btnType.getButtonData() == ButtonData.YES) {
        					maxMark.clear();
        					modelAnswer.clear();
        				} 
        				else {
        					rootLayoutController.removeTab(View.NEW_QUESTION_FORM.getTabName());
        				}
        			});	
        		}	
    		}
    		// The same exception type is thrown for multiple exception sources
        	catch(PersistenceException e) {
        		// Catch the exception, find out whether it was the SQL that caused it
        		if(e.getCause().getCause() instanceof SQLSyntaxErrorException) {
        			Alert alert = new Alert(Alert.AlertType.ERROR);
        			alert.setTitle("Add New Question");
        			alert.setHeaderText("Unable to Add Question");
        			alert.setContentText("The model answer failed to run.");
        			alert.showAndWait();
        		}
        		// If the student's SQL wasn't the cause, throw exception as normal
        		else {
        			throw e;
        		}
        	}
    	}
    	// If the SQL wasn't the cause, throw exception as normal
    	catch(Exception e) {
    		String alertTitle = "Test Model Answer";
			String alertHeaderText = "Error Saving";
			Alert alert = CommonComponents.Alerts.connectionFailure(e, alertTitle, alertHeaderText);
			alert.showAndWait();
    	}
    }
}
