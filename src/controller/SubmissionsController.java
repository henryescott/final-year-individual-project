package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.controlsfx.dialog.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import model.Answer;
import model.AnswerDAO;
import model.Submission;
import model.SubmissionDAO;
import model.YearGroup;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import util.CommonComponents;
import util.CommonComponents.ActionButtonTableCell;
import util.ViewLoader;
import util.ViewLoader.View;

public class SubmissionsController {
	/* ---------------- Fields ---------------- */
	@FXML
    private ComboBox<YearGroup> selectYear;
	@FXML
    private TableView<Submission> submissionsTable;
    @FXML
    private TableColumn<Submission, Integer>  studentNoColumn;
    @FXML
    private TableColumn<Submission, Double> markColumn;
    @FXML
    private TableColumn<Submission, Button> viewSubmissionColumn;
    @FXML
    private TableColumn<Submission, Button> deleteSubmissionColumn;
    @FXML
    private TextField searchBox;
    @FXML
    private Button exportMarks;
    @FXML
    private Button markSubmissions;
    @FXML 
    private Label showingCountText;
    @FXML 
    private Label selectedCountText;
    @FXML
    private MenuButton slection;
    @FXML
    private MenuItem selectAll;
    @FXML
    private MenuItem selectUnmarked;
    @FXML
    private MenuItem deselectAll;
    
    private RootLayoutController rootLayoutController;
    private Stage mainStage;
    private ObservableList<YearGroup> allYearGroups;
    private FilteredList<Submission> filteredSubmissions;
    private IntegerProperty showingCount;
    
    /* -------------- Constructor -------------- */
    public SubmissionsController(RootLayoutController rootLayoutController, ObservableList<YearGroup> allYearGroups) {
    	this.rootLayoutController = rootLayoutController;
    	this.mainStage = rootLayoutController.getMainStage();
    	this.allYearGroups = allYearGroups;
    	showingCount = new SimpleIntegerProperty();
    	filteredSubmissions = new FilteredList<Submission>(FXCollections.observableArrayList());
	}
    
    /* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {
    	// ------- Table Column Setup -------
        studentNoColumn.setCellValueFactory(cellData -> cellData.getValue().studentNumberProperty().asObject());
        markColumn.setCellValueFactory(cellData -> cellData.getValue().markProperty());
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view
         * ACCESSED: 04/05/2020	
         */
        // View Column
        viewSubmissionColumn.setCellFactory(ActionButtonTableCell.<Submission>forTableColumn("View", (Submission submission) -> {
    		String tabName = submission.getStudentNumber() + " | Answers";
        	AnswersController controller = new AnswersController(rootLayoutController, submission);
    		rootLayoutController.addTab(View.ANSWERS, controller, tabName);
            return submission;
        }));   
        // Delete Column
        deleteSubmissionColumn.setCellFactory(ActionButtonTableCell.<Submission>forTableColumn("Delete", (Submission submission) -> {
    		String contentText = "for student " + submission.getStudentNumber(); 
        	Alert alert = CommonComponents.Alerts.deleteConfirmation("Submission", contentText);
        	alert.showAndWait();
        	if (alert.getResult() == ButtonType.OK) {
        		// Delete the submission from the ObserVable list
        	    submission.getYearGroup().getSubmissions().remove(submission);
        	    // Delete the submission from the DB 
        	    SubmissionDAO.deleteSubmission(submission);
        	}
        	return submission;
        }));   
        /* END REFERENCE */ 
        
        // ------- Bindings -------
        slection.disableProperty().bind(showingCount.isEqualTo(0));
        
        selectAll.disableProperty().bind(
        		Bindings.size(submissionsTable.getSelectionModel().getSelectedItems()).isEqualTo(showingCount)
        );
        deselectAll.disableProperty().bind(
        		Bindings.size(submissionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0)
        );
        exportMarks.disableProperty().bind(
            	Bindings.size(submissionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0)
        );
        markSubmissions.disableProperty().bind(
        		Bindings.size(submissionsTable.getSelectionModel().getSelectedItems()).isEqualTo(0)
        );
        selectYear.disableProperty().bind(Bindings.size(allYearGroups).isEqualTo(0));
        showingCountText.textProperty().bind(showingCount.asString());
        selectedCountText.textProperty().bind(Bindings.size(submissionsTable.getSelectionModel().getSelectedItems()).asString());
        
        // ------- Event Handlers -------
        // Export Marks Button
        exportMarks.setOnAction(actionEvent -> exportMarks());
        
        // Mark Submissions Button
        markSubmissions.setOnAction(actionEvent -> markSubmissions());
        
        // Search Box
        searchBox.textProperty().addListener((observable, oldValue, newValue) -> {
        	filteredSubmissions.setPredicate(submission -> 
				Integer.toString(submission.getStudentNumber()).contains(searchBox.getText().trim())
        	);
        	showingCount.set(filteredSubmissions.size());
        });

        // Year Group Combo Box
        selectYear.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        	if(newValue.submissionsProperty() != null) {
            	filteredSubmissions = new FilteredList<Submission>(newValue.submissionsProperty());
            	// Make sure text box filter is still applied
            	filteredSubmissions.setPredicate(submission -> 
    				Integer.toString(submission.getStudentNumber()).contains(searchBox.getText().trim())
            	);
            	newValue.submissionsProperty().addListener(
            			(ListChangeListener.Change<? extends Submission> submissions) -> {
            				// Get an exception when trying to run on main JavaFX thread
            				// so need to use run later
            				Platform.runLater(new Runnable() {
            					@Override
            					public void run() {
            						showingCount.set(filteredSubmissions.size());
            					}
            				});
            			}
            	);
                SortedList<Submission> sortableSubmissions = new SortedList<Submission>(filteredSubmissions);
                sortableSubmissions.comparatorProperty().bind(submissionsTable.comparatorProperty());
            	submissionsTable.setItems(sortableSubmissions);
            	showingCount.set(filteredSubmissions.size());
        	}
    	});
        
        // Select All
        selectAll.setOnAction(actionEvent -> submissionsTable.getSelectionModel().selectAll());
        
        // Select Unmarked
        selectUnmarked.setOnAction(actionEvent -> {
        	submissionsTable.getSelectionModel().clearSelection();
        	filteredSubmissions.filtered(submission -> submission.getMark() == null).forEach(submission ->
        			submissionsTable.getSelectionModel().select(submission)
        	);
        });
        
        // Select Unmarked
        submissionsTable.getSelectionModel().getSelectedItems().addListener(
        	(ListChangeListener.Change<? extends Submission> selectedSubmissions) -> {
        		FilteredList<Submission> allUnmarked = filteredSubmissions.filtered(submission -> submission.getMark() == null);
        		if(selectedSubmissions.getList().containsAll(allUnmarked) || selectedSubmissions.getList().size() == showingCount.get()) {
        			selectUnmarked.setDisable(true);
        		}
        		else {
        			selectUnmarked.setDisable(false);
        		}
        	}
        );

        // De-select All
        deselectAll.setOnAction(actionEvent -> submissionsTable.getSelectionModel().clearSelection());
        
    	// ------- Initial Display -------
        selectYear.setItems(allYearGroups);
        selectYear.getSelectionModel().selectFirst();
        submissionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    private List<Bag<Object>> createResultsList(List<Tuple> queryResults) {
    	List<Bag<Object>> resultsList = new ArrayList<Bag<Object>>();
		for (Tuple row : queryResults) {
			Bag<Object> rowBag = new HashBag<Object>();
			for (TupleElement<?> element : row.getElements()) {
				rowBag.add(row.get(element));
		    }
			resultsList.add(rowBag);
		}
		return resultsList;
    }

    private void markSubmissions() {
        /* 
         * BEGIN REFERENCE 
         * URL: https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
         * ACCESSED: 15/04/2020
         */	
        Task<List<Integer>> task = new Task<List<Integer>>() {
            @Override
            protected List<Integer> call() throws Exception {
            	ObservableList<Submission> submissionsToMark = submissionsTable.getSelectionModel().getSelectedItems();
            	int questionsAutoMarked = 0;
            	int questionsManuallyMarked = 0;
                int count = submissionsToMark.size();
                for(int i = 0; i < count; i++) {
                    Submission submissionToMark = submissionsToMark.get(i);
                    this.updateMessage("Marking submission for student " + submissionToMark.getStudentNumber());
                    Double totalMarks = 0.0;
                    for(Answer answer: submissionToMark.getAnswers()) {                    
                        double maxMark = answer.getQuestion().getMaxMarks();
                        StringBuffer feedback = new StringBuffer();
                        boolean autoMarked = false;
                        boolean correctResultSet = false;
                        if(answer.getQuery() == null ||answer.getQuery().isEmpty()) {
                            answer.setMark(0.0);
                            feedback.append("Question not answered.\n\nModel Answer:\n" + answer.getQuestion().getModelAnswer());
                            autoMarked = true;
                        }
                        else {
                            try {
                                // Run the student's query and model answer query and grab the results
                                List<Tuple> studentResults = AnswerDAO.runQuery(answer.getQuery());
                                List<Tuple> correctResults = AnswerDAO.runQuery(answer.getQuestion().getModelAnswer());
                                if (studentResults.size() == 0) {
                                    feedback.append("Query ran successfully but did not produce any results.\n\nModel Answer:\n"
                                    		+ answer.getQuestion().getModelAnswer()
                                    );
                                }
                                else {
                                    /* Create a List representation of the results - this is just the data i.e. no column names
                                     * This is because the query could produce the correct results, but not use the same column
                                     * aliases as the model answer, so we ignore these. We use a List to represent the result set 
                                     * to ensure that we're checking ordering (ORDER BY) but a Bag to represent each tuple because 
                                     * we don't care about column ordering
                                     */
                                    List<Bag<Object>> studentResultsList = createResultsList(studentResults);
                                    List<Bag<Object>> correctResultsList = createResultsList(correctResults);

                                    // Results check 1: checks for correct results set, including ordering
                                    if(studentResultsList.equals(correctResultsList)) {
                                        feedback.append("Query produced the expected results.");
                                        answer.setMark(maxMark);
                                        autoMarked = true;
                                        correctResultSet = true;
                                    }
                                    // Results check 2: checks for correct results set, discounting ORDER BY
                                    else if(CollectionUtils.isEqualCollection(studentResultsList, correctResultsList)) {
                                        feedback.append("Query produced the expected results, "
                                                + "however these were not ordered correctly.");
                                        correctResultSet = true;
                                    }
                                    // Try and figure out what went wrong if it hasn't been auto-marked:
                                    if(!autoMarked) {
                                    	try {
                                    		Statement studentAnswer = CCJSqlParserUtil.parse(answer.getQuery().replaceAll(";$", ""));
                                    		Statement modelAnswer = CCJSqlParserUtil.parse(answer.getQuestion().getModelAnswer().replaceAll(";$", ""));
                                    		Select studentSelect = (Select) studentAnswer;
                                    		Select modelSelect = (Select) modelAnswer;
                                    		PlainSelect studentPlainSelect = (PlainSelect) studentSelect.getSelectBody();
                                    		PlainSelect modelPlainSelect = (PlainSelect) modelSelect.getSelectBody();
                                    		// Check group by
                                    		if(modelPlainSelect.getGroupBy() != null) {
                                    			System.out.println("we're checking group by!");
                                    			// Check whether the student has include the group by
                                    			if(studentPlainSelect.getGroupBy() == null) {
                                    				feedback.append("GROUP BY statement is missing. Expected " +
                                    						modelPlainSelect.getGroupBy()
                                    				);
                                    			}
                                    		}
                                    		
                                    		// Check having
                                    		if(modelPlainSelect.getHaving() != null) {
                                    			System.out.println("we're checking group by!");
                                    			// Check whether the student has include the group by
                                    			if(studentPlainSelect.getHaving() == null) {
                                    				feedback.append("HAVING statement is missing. Expected " +
                                    						modelPlainSelect.getHaving()
                                    				);
                                    			}
                                    		}
                                    		
                                    		// Check order by
                                    		if(modelPlainSelect.getOrderByElements() != null) {
                                    			System.out.println("we're checking group by!");
                                    			// Check whether the student has include the group by
                                    			if(studentPlainSelect.getGroupBy() == null) {
                                    				String orderBy = modelPlainSelect
                                    								.getOrderByElements()
                                    								.toString()
                                    								.replaceAll("\\[", "")
                                    								.replaceAll("\\]","");
                                    				if(correctResultSet) {
                                    					feedback.append(" Expected ORDER BY " + orderBy + ".");
                                    				}
                                    				else {
                                    					feedback.append("ORDER BY statement is missing. Expected " + orderBy);
                                    				}
                                    			}
                                    		}
                                    		
                                    	}
                                    	// Catch and swallow any parser errors - we still proceed to the manual marking
                                        // if whether we can or can't automate the feedback
                                    	catch (Exception e) {
                                    		e.printStackTrace();
                                    	}
                                    } 
                                }  
                            }
                            // The same exception type is thrown for multiple exception sources
                            catch(PersistenceException e) {
                                // Catch the exception, find out whether it was the student's SQL that caused it
                                if(e.getCause().getCause() instanceof SQLSyntaxErrorException) {
                                    e.printStackTrace();
                                    feedback.append("Query failed to run.\n\nModel Answer:\n" + answer.getQuestion().getModelAnswer());
                                }
                                // If the student's SQL wasn't the cause, throw exception as normal
                                else {
                                    throw e;
                                }
                            }
                        }
                        
                        answer.setFeedback(feedback.toString());
                     
                        // If human validation is required, display the edit dialog
                        if(!autoMarked) {
                           	questionsManuallyMarked += 1;
        			        /* 
        			         * BEGIN REFERENCE 
        			         * URL: https://stackoverflow.com/questions/50214673/java-fx-new-modal-window-in-task?noredirect=1&lq=1
        			         * ACCESSED: 15/04/2020
        			         */
                            CompletableFuture.runAsync(() -> {
                            	// Clear any previously set mark
                            	answer.setMark(null);
                                Alert alert = new Alert (Alert.AlertType.INFORMATION);
                                alert.setTitle("Mark Submissions");
                                alert.setHeaderText("Manual Marking Required");
                                alert.setContentText("Please mark this question manually");
                                alert.showAndWait();
                                // Show the manual marking window
                                Stage manualMarkingWindow = new Stage(); 
                                manualMarkingWindow.initModality(Modality.APPLICATION_MODAL);
                                manualMarkingWindow.setTitle(answer.getSubmission().getStudentNumber() + 
                                		" | Q" + answer.getQuestion().getQuestionNumber());
                                manualMarkingWindow.setOnCloseRequest(windowEvent -> windowEvent.consume());
                                AnswerComparisonController controller = new AnswerComparisonController(answer, manualMarkingWindow);
                                Scene scene = new Scene(ViewLoader.loadFXML(View.ANSWER_COMPARISON, controller));
                                manualMarkingWindow.setScene(scene);
                                manualMarkingWindow.setMaximized(true);
                                manualMarkingWindow.showAndWait();
                            }, Platform::runLater).join();
                            /* END REFERENCE */
                        }
                        else {
                        	questionsAutoMarked += 1;
                        }
                        // Increment the overall submission marks with the mark has been set
                        totalMarks += answer.getMark();
                    }
                    submissionToMark.setMark(totalMarks);
                    SubmissionDAO.upsertSubmission(submissionToMark);
                    this.updateProgress(i, count);
                    Thread.sleep(100);
                }
                List<Integer> statistics = new ArrayList<Integer>();
                statistics.add(count);
                statistics.add(questionsAutoMarked);
                statistics.add(questionsManuallyMarked);
                return statistics; 
            }
        };
        /* END REFERENCE */

        // Handle success       
        task.setOnSucceeded(event -> {
        	List<Integer> statistics = task.getValue();
            Alert alert = new Alert (Alert.AlertType.INFORMATION);
            alert.setTitle("Mark Submissions");
            alert.setHeaderText("Marking Has Been Completed");
            alert.setContentText(statistics.get(0) + " submission(s) marked. " + statistics.get(1)
            	+ " question(s) automatically marked. " +  statistics.get(2) + " question(s) manually marked." 
            );
            alert.showAndWait();
        });

        // Handle failure
        task.setOnFailed(event -> {
        	task.getException().printStackTrace();
        	String alertTitle = "Mark Submissions";
    		String alertHeaderText = "Marking Failed";
    		Alert alert = CommonComponents.Alerts.connectionFailure(task.getException(), alertTitle, alertHeaderText);
    		alert.showAndWait();
        });

        ProgressDialog progressDialog = new ProgressDialog(task);
        progressDialog.setTitle("Mark Submissions");
        progressDialog.setHeaderText("Marking In Progress");
        progressDialog.setContentText("Please wait...");
        new Thread(task).start(); 
        progressDialog.showAndWait();
    }
    
    /* 
     * BEGIN REFERENCE 
     * URL: https://medium.com/@ssaurel/generating-microsoft-excel-xlsx-files-in-java-9508d1b521d9
     * ACCESSED: 10/04/2020
     */	
    private void exportMarks () {
    	Workbook workbook = new XSSFWorkbook();
        /* 
         * BEGIN REFERENCE 
         * URL: https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
         * ACCESSED: 15/04/2020
         */	
    	Task<Void> exportMarksTask = new Task<Void>(){
    		@Override
    		protected Void call() throws Exception {
    			// Set the sheet name to the academic year?
    			Sheet sheet = workbook.createSheet();
    			Font headerFont = workbook.createFont();
    			headerFont.setBold(true);
    			CellStyle headerCellStyle = workbook.createCellStyle();
    			headerCellStyle.setFont(headerFont);
    			Row headerRow = sheet.createRow(0);
    			List<String> rowHeaders = Arrays.asList("Student Number", "Mark", "Feedback");
    			for (int i = 0; i < rowHeaders.size(); i++) {
    				Cell cell = headerRow.createCell(i);
    				cell.setCellValue(rowHeaders.get(i));
    				cell.setCellStyle(headerCellStyle);
    				sheet.autoSizeColumn(i);
    			}
    			int count = submissionsTable.getSelectionModel().getSelectedItems().size();
    			int rowNum = 1;
    			CellStyle wrapTextCellStyle = workbook.createCellStyle();
    			wrapTextCellStyle.setWrapText(true);
    			for (int i = 0; i < count; i++) {
    				Submission submission = submissionsTable.getSelectionModel().getSelectedItems().get(i);
    				this.updateMessage("Export mark for for student " + submission.getStudentNumber());
    				Row row = sheet.createRow(rowNum++);
    				row.createCell(0).setCellValue(submission.getStudentNumber());
    				if (submission.getMark() != null) {
    					row.createCell(1).setCellValue(submission.getMark());
    				}
    				StringBuffer feedback = new StringBuffer();
    				for (Answer answer : submission.getAnswers()) {
    					feedback.append("Question " + answer.getQuestion().getQuestionNumber() + ":\n");
    					if (answer.getFeedback() != null) {
    						feedback.append(answer.getFeedback());
    					}
                        feedback.append("\n\nMark: " + answer.getMark() + "/" + answer.getQuestion().getMaxMarks()
                        		+ "\n\n");
    				}
    				row.createCell(2).setCellValue(feedback.toString().trim());
    				row.getCell(2).setCellStyle(wrapTextCellStyle);
    				sheet.autoSizeColumn(2);
    				this.updateProgress(i, count);
    				Thread.sleep(100);
    			}
    			return null;
    		}
    	};
    	/* END REFERENCE */
    	exportMarksTask.setOnSucceeded(event -> {
    		FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel Workbook (.xlsx)", "*.xlsx"));
			File file = fileChooser.showSaveDialog(mainStage);
			if (file != null) {
				try {
					FileOutputStream fileOut = new FileOutputStream(file);
					workbook.write(fileOut);
					fileOut.close();
					workbook.close();
					Alert alert = CommonComponents.Alerts.saveFileSuccess(file);
					alert.showAndWait();

				} catch (IOException exception) {
					Alert alert = CommonComponents.Alerts.saveFileFailure(file, exception);
					alert.showAndWait();
				}
			}
    	});
    	
    	ProgressDialog progressDialog = new ProgressDialog(exportMarksTask);
    	progressDialog.setTitle("Exporting Marks");
    	progressDialog.setHeaderText("Marks Export In Progress");
    	progressDialog.setContentText("Please wait...");
    	new Thread(exportMarksTask).start(); 
    	progressDialog.showAndWait();
    }
    /* END REFERENCE */
}
