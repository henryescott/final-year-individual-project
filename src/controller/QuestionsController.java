package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Question;
import model.QuestionDAO;
import model.Submission;
import model.YearGroup;
import model.YearGroupDAO;
import util.CommonComponents;
import util.CommonComponents.ActionButtonTableCell;
import util.ViewLoader.View;

public class QuestionsController {
	/* ---------------- Fields ---------------- */
	@FXML
    private ComboBox<YearGroup> selectYear;
	@FXML
    private TableView<Question> questionsTable;
    @FXML
    private TableColumn<Question, Integer> questionNumberColumn;
    @FXML
    private TableColumn<Question, Double> maxMarkColumn;
    @FXML
    private TableColumn<Question, String> modelAnswerColumn;
    @FXML
    private TableColumn<Question, Button> viewResultsColumn;
    @FXML
    private TableColumn<Question, Button> deleteQuestionColumn;
    @FXML
    private Button newQuestion;
    @FXML
    private Button exportTemplate;
    
    private RootLayoutController rootLayoutController;
    private ObservableList<YearGroup> allYearGroups;
    private ListProperty<Submission> yearGroupSubmissons;
    private ListProperty<Question> displayedQuestions;
 
    /* -------------- Constructor -------------- */
    public QuestionsController(RootLayoutController rootLayoutController, ObservableList<YearGroup> allYearGroups) {
		this.rootLayoutController = rootLayoutController;
		this.allYearGroups = allYearGroups;
		yearGroupSubmissons = new SimpleListProperty<Submission>();
		displayedQuestions = new SimpleListProperty<Question>();
	}

    /* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {
    	// ------- Table Column Setup -------
        questionNumberColumn.setCellValueFactory(cellData -> cellData.getValue().questionNumberProperty().asObject());
        maxMarkColumn.setCellValueFactory(cellData -> cellData.getValue().maxMarksProperty().asObject());
        modelAnswerColumn.setCellValueFactory(cellData -> cellData.getValue().modelAnswerProperty());
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view
         * ACCESSED: 04/05/2020
         */
        // View Column
        viewResultsColumn.setCellFactory(ActionButtonTableCell.<Question>forTableColumn("View", (Question question) -> {
        	try {
    			Stage resultsWindow = new Stage(); 
        		resultsWindow.initModality(Modality.APPLICATION_MODAL);
        		resultsWindow.setTitle("Model Answer Results for Q" + question.getQuestionNumber());
        		AnchorPane resultsPane = new AnchorPane();
        		Scene scene = new Scene(resultsPane);
        		resultsWindow.setScene(scene);
    			CommonComponents.ResultsTable.createQueryResultsTable(question.getModelAnswer(), resultsPane);
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
            return question;
        }));   
        // Delete Column
        deleteQuestionColumn.setCellFactory(ActionButtonTableCell.<Question>forTableColumn("Delete", (Question question) -> {
    		String contentText = String.valueOf(question.getQuestionNumber()); 
        	Alert alert = CommonComponents.Alerts.deleteConfirmation("Question", contentText);
        	alert.showAndWait();
        	if (alert.getResult() == ButtonType.OK) {
        		// Delete the submission from the ObserVable list
        		question.getYearGroup().getQuestions().remove(question);
        	    // Delete the submission from the DB 
        	    QuestionDAO.deleteQuestion(question);
        	}
        	// Re-order the questions
        	for (int i = 0; i < displayedQuestions.size(); i++) {
        		displayedQuestions.get(i).setQuestionNumber(i+1);
        	}
        	YearGroupDAO.upsertYearGroup(selectYear.getSelectionModel().getSelectedItem());
        	return question;
        }));   
        /* END REFERENCE */
       
        // ------- Bindings -------
        selectYear.disableProperty().bind(
        		Bindings.size(allYearGroups).isEqualTo(0)
        );
        
        newQuestion.disableProperty().bind(
        		Bindings.size(allYearGroups).isEqualTo(0)
        		.or(Bindings.size(yearGroupSubmissons).isNotEqualTo(0))
        );
        
        exportTemplate.disableProperty().bind(
        		Bindings.size(allYearGroups).isEqualTo(0)
        		.or(Bindings.size(displayedQuestions).isEqualTo(0))
        );
        
        // ------- Event Handlers -------
    	// New Question Button
        newQuestion.setOnAction((event) -> {
        	NewQuestionController controller = new NewQuestionController(rootLayoutController, allYearGroups, selectYear.getSelectionModel().getSelectedItem());
        	rootLayoutController.addTab(View.NEW_QUESTION_FORM, controller, View.NEW_QUESTION_FORM.getTabName());
    	});
        
    	// Export Answers Template Button
        exportTemplate.setOnAction(event -> exportAnswerTemplate());
        
        // Year Group Combo Box
        selectYear.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        	yearGroupSubmissons.set(newValue.submissionsProperty());
        	displayedQuestions.setValue(newValue.questionsProperty());
        	questionsTable.setItems(displayedQuestions);
        });
        
        yearGroupSubmissons.addListener((observable, oldValue, newValue) -> {
        	// Get an exception when trying to run on main JavaFX thread
        	// so need to use run later
        	Platform.runLater(new Runnable(){
				@Override
				public void run() {
					if(newValue.isEmpty()) {
	                    deleteQuestionColumn.setVisible(true);
	            	}
	            	else {
	                    deleteQuestionColumn.setVisible(false);
	            	}
					questionsTable.refresh();
				}
        	});
        });
     

        // ------- Initial Display -------
        selectYear.setItems(allYearGroups);
        selectYear.getSelectionModel().selectFirst();
    }
    
    private void exportAnswerTemplate() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SQL Script (*.sql)", "*.sql"));
        StringBuffer questionNumbers = new StringBuffer();
        for (Question question : questionsTable.getItems()) {
        	questionNumbers.append("-- Q" + question.getQuestionNumber() + "\n\n\n");
        }
        File file = fileChooser.showSaveDialog(rootLayoutController.getMainStage()); 
        // Check that the user didn't select cancel
        if(file != null){
            try {
            	file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(questionNumbers.toString());
                writer.close();
                Alert alert = CommonComponents.Alerts.saveFileSuccess(file);
            	alert.showAndWait();
            }
            catch(IOException exception) {
             	Alert alert = CommonComponents.Alerts.saveFileFailure(file, exception);
            	alert.showAndWait();
            }
    	}
    }
}
