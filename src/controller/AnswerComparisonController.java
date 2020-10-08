package controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import model.Answer;
import model.AnswerDAO;
import model.SubmissionDAO;
import util.CommonComponents;

public class AnswerComparisonController {
	/* ---------------- Fields ---------------- */
    @FXML
    private TextArea studentAnswerQuery;
    @FXML
    private AnchorPane studentAnswerResults;
    @FXML
    private TextArea modelAnswerQuery;
    @FXML
    private AnchorPane modelAnswerResults;
    @FXML
    private TextField mark;
    @FXML 
    private Label maxMarksText;
    @FXML
    private TextArea feedback;
    @FXML
    private Button edit;
    @FXML
    private Button save;
    @FXML
    private TextField searchBox;
    @FXML
    private Button addFeedback;
    @FXML
    private TableView<String> feedbackLibraryTable;
    @FXML
    private TableColumn<String, String> feedbackColumn;
    
    private Answer answer;
    private BooleanProperty autoMarking;
    private Stage manualMarkingWindow;
   
    /* -------------- Constructors -------------- */
    public AnswerComparisonController(Answer answer) {
		this.answer = answer;
		this.autoMarking = new SimpleBooleanProperty(false);
	}
    
    public AnswerComparisonController(Answer answer, Stage manualMarkingWindow) {
		this.answer = answer;
		this.autoMarking = new SimpleBooleanProperty(true);
		this.manualMarkingWindow = manualMarkingWindow;
	}
        
	/* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {
    	// ------- Initialize Variables -------
        ObservableList<String> answerFeedback = FXCollections.observableList(AnswerDAO.getAllFeedback(answer.getQuestion()));
        FilteredList<String> filteredFeedback = new FilteredList<String>(answerFeedback);
        feedbackLibraryTable.setItems(filteredFeedback);
                
        // ------- Table Column Setup -------
        feedbackColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/22732013/javafx-tablecolumn-text-wrapping
         * ACCESSED: 30/04/2020
         */
        feedbackColumn.setCellFactory(tableCell -> {
            TableCell<String, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            text.setStyle("-fx-fill: -fx-text-background-color;");
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(feedbackColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        /* END REFERENCE */
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/12324464/how-to-javafx-hide-background-header-of-a-tableview?rq=1
         * ACCESSED: 09/05/2020
         */
        feedbackLibraryTable.widthProperty().addListener((observable, oldValue, newValue) -> {
                Pane header = (Pane) feedbackLibraryTable.lookup("TableHeaderRow");
                if(header!=null && header.isVisible()) {
                  header.setMaxHeight(0);
                  header.setMinHeight(0);
                  header.setPrefHeight(0);
                  header.setVisible(false);
                  header.setManaged(false);
                }
        });
        /* END REFERENCE */
        
        // ------- Form Validation -------  
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/15615890/recommended-way-to-restrict-input-in-javafx-textfield
         * ACCESSED: 05/05/2020
         */
    	TextFormatter<String> markFormatter = new TextFormatter<String>(change -> {
    		String input = change.getText();
    		String fieldValue = change.getControlNewText();
    		if ((input.matches("^[0-9.]$") && fieldValue.matches("^[0-9]*.?[0-9]?$") 
    				&& Double.parseDouble(fieldValue) <= answer.getQuestion().getMaxMarks()) || change.isDeleted()) {
    			return change;
    		}
    		return null;
    	});
        /* END REFERENCE */

    	// ------- Bindings -------  
    	studentAnswerQuery.textProperty().bind(answer.queryProperty());
    	modelAnswerQuery.textProperty().bind(answer.getQuestion().modelAnswerProperty());
    	maxMarksText.textProperty().bind(answer.getQuestion().maxMarksProperty().asString());
    	save.disableProperty().bind(
    			Bindings.not(edit.disabledProperty())
    			.or(Bindings.isEmpty(mark.textProperty()))
    		    .or(Bindings.isEmpty(feedback.textProperty()))
    	);
    	feedback.disableProperty().bind(	
    		autoMarking.not()
    		.and(
    			edit.disabledProperty().not()
    			.or(answer.markProperty().isNull())
    		)
    	);
    	
    	mark.disableProperty().bind(	
    		autoMarking.not()
        	.and(
        		edit.disabledProperty().not()
        		.or(answer.markProperty().isNull())
        	)
        );
    	
        edit.visibleProperty().bind(autoMarking.not());
        
        addFeedback.disableProperty().bind(	
        	autoMarking.not()
            .and(
            	edit.disabledProperty().not()
            	.or(answer.markProperty().isNull())
            )
            .or(feedbackLibraryTable.getSelectionModel().selectedItemProperty().isNull())
        );
        
        if(!autoMarking.get()) {
        	bindMarkField();
            feedback.textProperty().bind(answer.feedbackProperty());
        }
        else {
        	feedback.setText(answer.getFeedback());
        	mark.setTextFormatter(markFormatter);
        }
    	
    	// ------- Event Handlers -------  
    	// Save Button
    	save.setOnAction(actionEvent -> {
    		double oldAnswerMark = 0;
    		if(answer.getMark() != null) {
    			oldAnswerMark = answer.getMark();
    		}
			answer.setMark(Double.parseDouble(mark.getText()));
			answer.setFeedback(feedback.getText());
    			if(autoMarking.get()) {
    				// Close the manual marking stage
    				manualMarkingWindow.close();
    			}
    			else {
    				// Need to update the overall submission mark aswell
        			double markDifference = answer.getMark() - oldAnswerMark;
        			double currentSubmissionMark = answer.getSubmission().getMark();
        			answer.getSubmission().setMark(currentSubmissionMark + markDifference);
            		SubmissionDAO.upsertSubmission(answer.getSubmission());
                	edit.setDisable(false);
    			}		
    	});
    	
    	// Edit Button
    	edit.setOnAction(actionEvent -> edit.setDisable(true));

        // Search Box
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> 
        	filteredFeedback.setPredicate(
        			feedback -> feedback.toLowerCase().contains(searchBox.getText().toLowerCase().trim())
        	)
        );
        
        // Add Feedback Button
        addFeedback.setOnAction(actionEvent -> {
        	if (!feedback.getText().isEmpty()) {
        		feedback.setText(feedback.getText() + "\n" + feedbackLibraryTable.getSelectionModel().getSelectedItem());
        	}
        	else {
        		feedback.setText(feedback.getText() + feedbackLibraryTable.getSelectionModel().getSelectedItem());
        	}
        });
    	
        // Mark
        mark.disableProperty().addListener((observable, oldValue, newValue) -> {
        	if(newValue) {
        		bindMarkField();
        	}
        	else {
        		mark.textProperty().unbind();
        		if(answer.getMark() != null) {
            		mark.setText(answer.getMark().toString());
        		}
            	mark.setTextFormatter(markFormatter);
        	}
        });
        
        // Feedback
        feedback.disableProperty().addListener((observable, oldValue, newValue) -> {
    		if(newValue) {
    			feedback.textProperty().bind(answer.feedbackProperty());
    		}
    		else {
    			feedback.textProperty().unbind();
    			feedback.setText(answer.getFeedback());
    		}
        });
        
        // Check for external changes to the answer's mark (automarking)
        answer.markProperty().addListener((observable, oldValue, newValue) -> {
        	if(newValue == null) {
        		edit.setDisable(true);
        	}
        	else {
        		edit.setDisable(false);
        	}
        });
        
    	// ------- Initial Display -------
        if(answer.getMark() == null) {
    		edit.setDisable(true);
    	}
        else {
        	edit.setDisable(false);
        }
    	loadResults();
    }
    
    private void loadResults() {
    	try {
    		CommonComponents.ResultsTable.createQueryResultsTable(answer.getQuery(), studentAnswerResults);
    		CommonComponents.ResultsTable.createQueryResultsTable(answer.getQuestion().getModelAnswer(), modelAnswerResults);
    	}
    	// Handle unable to connect to database
    	catch(Exception e) {
    		String errorMessage = "Unable to display results. Connection to database failed.";
    		if(studentAnswerResults.getChildren().isEmpty()) {
    			studentAnswerResults.getChildren().add(CommonComponents.createErrorMessageLabel(errorMessage));
    		}
        	modelAnswerResults.getChildren().add(CommonComponents.createErrorMessageLabel(errorMessage));
    		String alertTitle = "View Q" + answer.getQuestion().getQuestionNumber();
    		String alertHeaderText = "Error Loading Results";
    		Alert alert = CommonComponents.Alerts.connectionFailure(e, alertTitle, alertHeaderText);
    		alert.showAndWait();
    	}
    }
    
    private void bindMarkField() {
    	mark.setTextFormatter(null);
		mark.textProperty().bind(Bindings.when(answer.markProperty().isNull())
			.then(new SimpleStringProperty())
			.otherwise(answer.markProperty().asString()));
    }
}
