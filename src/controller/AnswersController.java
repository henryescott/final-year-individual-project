package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import model.Answer;
import model.Submission;
import util.CommonComponents.ActionButtonTableCell;
import util.ViewLoader.View;

public class AnswersController {
	/* ---------------- Fields ---------------- */
	@FXML
    private TableView<Answer> answersTable;
    @FXML
    private TableColumn<Answer, Integer>  studentNoColumn;
    @FXML
    private TableColumn<Answer, Integer> questionNoColumn;
    @FXML
    private TableColumn<Answer, Double> maxMarkColumn;
    @FXML
    private TableColumn<Answer, Double> markColumn;
    @FXML
    private TableColumn<Answer, String> feedbackColumn;
    @FXML
    private TableColumn<Answer, Button> viewResultsComparison;
    
    private RootLayoutController rootLayoutController;
    private Submission submission;
    
    /* -------------- Constructor -------------- */
    public AnswersController(RootLayoutController rootLayoutController, Submission submission) {
		this.rootLayoutController = rootLayoutController;
		this.submission = submission;
	}
    
    /* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {
    	// ------- Table Column Setup -------
        studentNoColumn.setCellValueFactory(cellData -> cellData.getValue().getSubmission().studentNumberProperty().asObject());
        questionNoColumn.setCellValueFactory(cellData -> cellData.getValue().getQuestion().questionNumberProperty().asObject());
        maxMarkColumn.setCellValueFactory(cellData -> cellData.getValue().getQuestion().maxMarksProperty().asObject());
        markColumn.setCellValueFactory(cellData -> cellData.getValue().markProperty());
        feedbackColumn.setCellValueFactory(cellData -> cellData.getValue().feedbackProperty());
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/22732013/javafx-tablecolumn-text-wrapping
         * ACCESSED: 30/04/2020
         */
        feedbackColumn.setCellFactory(tableCell -> {
            TableCell<Answer, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            text.setStyle("-fx-fill: -fx-text-background-color;");
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(feedbackColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });
        /* END REFERENCE */
        
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view
         * ACCESSED: 04/05/2020	
         */
        viewResultsComparison.setCellFactory(ActionButtonTableCell.<Answer>forTableColumn("View", (Answer answer) -> {
        	String tabName = submission.getStudentNumber() + " | Q" + answer.getQuestion().getQuestionNumber();
        	AnswerComparisonController controller = new AnswerComparisonController(answer);
        	rootLayoutController.addTab(View.ANSWER_COMPARISON, controller, tabName);
            return answer;
        }));      
        /* END REFERENCE */

        // ------- Initial Display -------
        answersTable.setItems(submission.answersProperty());
    }
}
