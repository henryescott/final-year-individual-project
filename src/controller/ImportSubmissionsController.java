package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.controlsfx.dialog.ProgressDialog;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import model.Answer;
import model.Question;
import model.Submission;
import model.SubmissionDAO;
import model.YearGroup;
import util.CommonComponents.ActionButtonTableCell;
import util.ViewLoader.View;

public class ImportSubmissionsController {
	/* ---------------- Fields ---------------- */
	@FXML
    private ComboBox<YearGroup> selectYear;
	@FXML
    private TableView<Submission> importPreviewTable;
    @FXML
    private TableColumn<Submission, Integer> studentNoColumn;
    @FXML
    private TableColumn<Submission, String> academicYearColumn;
    @FXML
    private TableColumn<Submission, Integer> questionsFoundColumn;
    @FXML
    private TableColumn<Submission, Button> removeColumn;
    @FXML
    private Button loadFile;
    @FXML
    private Button loadDirectory;
    @FXML
    private Button importSubmissions;
    @FXML 
    private Label loadedCountText;
    
    private RootLayoutController rootLayoutController;
    private Stage mainStage;
    private ObservableList<YearGroup> allYearGroups;
    private YearGroup selectedYearGroup;
    private ObservableList<Submission> selectedYearGroupSubmissions;
    private ObservableList<Submission> submissionsToImport;
    private ObservableMap<String, File> loadedFiles;
    
    /* -------------- Constructor -------------- */
    public ImportSubmissionsController(RootLayoutController rootLayoutController, ObservableList<YearGroup> allYearGroups) {
    	this.rootLayoutController = rootLayoutController;
    	this.mainStage = rootLayoutController.getMainStage();
    	this.allYearGroups = allYearGroups;
    	submissionsToImport = FXCollections.observableArrayList(); 
    	loadedFiles = new SimpleMapProperty<String, File>(FXCollections.observableMap(new HashMap<String, File>()));
	}
    
	/* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {
    	 // ------- Table Column Setup -------
    	studentNoColumn.setCellValueFactory(cellData -> cellData.getValue().studentNumberProperty().asObject());
    	academicYearColumn.setCellValueFactory(cellData -> {
    		SimpleStringProperty academicYear = new SimpleStringProperty();
    		int academicYearEnd = Integer.parseInt(String.valueOf(cellData.getValue().getYearGroup().getAcademicYear()).substring(2, 4)) + 1;
    		academicYear.setValue(cellData.getValue().getYearGroup().getAcademicYear() + "/" + String.format("%02d",academicYearEnd));
			return academicYear;
    	});
    	questionsFoundColumn.setCellValueFactory(cellData -> {
	    	FilteredList<Answer> nonNullAnswers = new FilteredList<Answer>(cellData.getValue().answersProperty());
	    	nonNullAnswers.setPredicate(answer -> answer.queryProperty().isNotNull().get());
			return Bindings.size(nonNullAnswers).asObject();
    	}); 
        /* 
         * BEGIN REFERENCE 
         * URL: https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view
         * ACCESSED: 04/05/2020
         */	
    	removeColumn.setCellFactory(ActionButtonTableCell.<Submission>forTableColumn("Remove", (Submission submission) -> {
    		submissionsToImport.remove(submission);
    		loadedFiles.remove(String.valueOf(submission.getStudentNumber()));
            return submission;
        }));      
    	/* END REFERENCE */
    	
        // ------- Bindings -------
        selectYear.disableProperty().bind(
        		Bindings.size(allYearGroups).isEqualTo(0)
        		.or(Bindings.isNotEmpty(submissionsToImport))
        );
        
        loadFile.disableProperty().bind(Bindings.size(allYearGroups).isEqualTo(0));
        loadDirectory.disableProperty().bind(Bindings.size(allYearGroups).isEqualTo(0));
    	importSubmissions.disableProperty().bind(Bindings.isEmpty(submissionsToImport));
    	loadedCountText.textProperty().bind(Bindings.size(loadedFiles).asString());
    	
    	// ------- Event Handlers ------- 
    	// Import File Button
        loadFile.setOnAction(actionEvent -> {
        	FileChooser fileChooser = new FileChooser();
        	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SQL Script (*.sql)", "*.sql"));
        	fileChooser.setTitle("Select File(s)");
        	List<File> selectedFiles = fileChooser.showOpenMultipleDialog(mainStage);
        	// Check that the user didn't select cancel
        	if(selectedFiles != null) {
        		loadSubmissions(selectedFiles);
        	}
        });
        
        // Import Directory Button
        loadDirectory.setOnAction(actionEvent -> {
        	DirectoryChooser directoryChooser = new DirectoryChooser();
        	File selectedDirectory = directoryChooser.showDialog(mainStage);
        	// Check that the user didn't select cancel
        	if(selectedDirectory != null) {
        		loadSubmissions(Arrays.asList(selectedDirectory.listFiles()));
        	}
        });
        	
    	// Import Submissions Button
    	importSubmissions.setOnAction(actionEvent -> importSubmissions()); 
    	
        selectYear.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        	selectedYearGroup = newValue;
        	selectedYearGroupSubmissions = newValue.submissionsProperty();
    	});
        
        // ------- Initial Display -------
        selectYear.setItems(allYearGroups);
        selectYear.getSelectionModel().selectFirst();
    }
    
    private void loadSubmissions(List<File> selectedFiles) {
        /* 
         * BEGIN REFERENCE 
         * URL: https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
         * ACCESSED: 15/04/2020
         */	
    	Task<Void> task = new Task<Void>() {
    		@Override
    		protected Void call() throws Exception {
    			int count = selectedFiles.size();
    	    	for (int i = 0; i < selectedFiles.size(); i++) {
    	    		File file = selectedFiles.get(i);
    	    		this.updateMessage("Loading file " + file.getName());
    	    		if (file.isFile() && file.getName().endsWith(".sql")) {
    	    			// Check whether the file has already been loaded
    	    			if(loadedFiles.containsValue(file)) {
        			        /* 
        			         * BEGIN REFERENCE 
        			         * URL: https://stackoverflow.com/questions/50214673/java-fx-new-modal-window-in-task?noredirect=1&lq=1
        			         * ACCESSED: 15/04/2020
        			         */
    	    				CompletableFuture.runAsync(() -> {
        	    				ButtonType skipButton = new ButtonType("Skip", ButtonData.OK_DONE);
        						ButtonType cancelButton = new ButtonType("Cancel file import", ButtonData.CANCEL_CLOSE);
        						Alert alert = new Alert (Alert.AlertType.CONFIRMATION, "Unable to load file " + file.getName() + 
        								". File has already been loaded.", skipButton, cancelButton);
    	    	            	alert.setTitle("File Unable to Be Loaded");
    	    	            	alert.setHeaderText("Failed to Load File");
    	    	            	alert.showAndWait().ifPresent((buttonType) -> {
        							if (buttonType.getButtonData() == ButtonData.CANCEL_CLOSE) {
        								this.cancel();
        							} 
    	    	            	});
        	    			}, Platform::runLater).join();
    	    				/* END REFERENCE */
    	    			}
    	    			// Otherwise try and load the file
    	    			else {
        	    			submissionsToImport.add(readFile(file));
        	    			// Required as the size of loadedFiles is bound to
        	    			// the count label, so can't run this on the main JavaFX thread
        	    			CompletableFuture.runAsync(() -> {
        	    				loadedFiles.put(file.getName().replaceFirst("[cC]", "").replaceFirst(".sql", ""), file);
        	    			}, Platform::runLater).join();
    	    			}
    	    		}
    	    		else {
    			        /* 
    			         * BEGIN REFERENCE 
    			         * URL: https://stackoverflow.com/questions/50214673/java-fx-new-modal-window-in-task?noredirect=1&lq=1
    			         * ACCESSED: 15/04/2020
    			         */
    	    			CompletableFuture.runAsync(() -> {
    	    				ButtonType skipButton = new ButtonType("Skip", ButtonData.OK_DONE);
    						ButtonType cancelButton = new ButtonType("Cancel file import", ButtonData.CANCEL_CLOSE);
    						Alert alert = new Alert (Alert.AlertType.CONFIRMATION, "Unable to load file " + file.getName() + 
    								". Only .sql files can be imported.", skipButton, cancelButton);
	    	            	alert.setTitle("File Unable to Be Loaded");
	    	            	alert.setHeaderText("Failed to Load File");
	    	            	alert.showAndWait().ifPresent((buttonType) -> {
    							if (buttonType.getButtonData() == ButtonData.CANCEL_CLOSE) {
    								this.cancel();
    							} 
	    	            	});
    	    			}, Platform::runLater).join();
    	    			/* END REFERENCE */
    	    		}
    	    		this.updateProgress(i, count);
    				Thread.sleep(100);
    	    	}
    			return null;
    		}
    	};
    	/* END REFERENCE */
    		
    	task.setOnSucceeded(event -> {
    		submissionsToImport.sort(Comparator.comparing(Submission::getStudentNumber));
	    	importPreviewTable.setItems(submissionsToImport);
    		Alert alert = new Alert (Alert.AlertType.INFORMATION);
        	alert.setTitle("Load Files");
        	alert.setHeaderText("Files load completed");
        	alert.setContentText("The files have been loaded successfully");
        	alert.showAndWait();
    	});
    	
    	// Handle failure
    	task.setOnFailed(event -> {
    		Alert alert = new Alert (Alert.AlertType.ERROR);
        	alert.setTitle("Load Files");
        	alert.setHeaderText("Files load failed");
        	alert.setContentText("The files were unable to be loaded");
        	alert.showAndWait();
    	});
    	
    	task.setOnCancelled(workerStateEvent -> {
    		submissionsToImport.sort(Comparator.comparing(Submission::getStudentNumber));
	    	importPreviewTable.setItems(submissionsToImport);
    		Alert alert = new Alert (Alert.AlertType.INFORMATION);
        	alert.setTitle("Load Files");
        	alert.setHeaderText("The file load was cancelled");
        	alert.setContentText("Any files loaded prior to cancelling will still have been added");
        	alert.showAndWait();
    	});
    	
    	ProgressDialog progressDialog = new ProgressDialog(task);
    	progressDialog.setTitle("Load Files");
    	progressDialog.setHeaderText("File Load In Progress");
    	progressDialog.setContentText("Loading Files");
    	new Thread(task).start(); 
    	progressDialog.showAndWait();
    	progressDialog.close();
    }

    private Submission readFile(File file) throws Exception {
    	Scanner inFile;
    	Submission submission = new Submission();
    	String studentNo = file.getName().replaceFirst("[cC]", "").replaceFirst(".sql", "");
    	// Add in error checking to make sure file is named correctly 
    	submission.setStudentNumber(Integer.parseInt(studentNo));
    	submission.setYearGroup(selectedYearGroup);
    	List<Question> questions = selectedYearGroup.getQuestions();
    	List<Question> questionsAnswered = new ArrayList<Question>();
    	try {
    		inFile = new Scanner(file);
    		Answer answer = null;
    		StringBuffer query = new StringBuffer();
    		while(inFile.hasNextLine()) {
    			String line = inFile.nextLine().replaceAll("\\s+$", ""); // Performs the equivalent of an RTRIM
    			if (line.matches("-- [Qq][0-9]*") || !inFile.hasNextLine()) {
    				if(answer != null && answer.getQuestion() != null) {
    					answer.setQuery(query.toString().trim());
    					submission.answersProperty().add(answer);
    				}
    				if(inFile.hasNextLine()) {
    					answer = new Answer();
    					query = new StringBuffer();
    					Question question = questions.stream()
    							.filter(q -> q.getQuestionNumber() == Integer.valueOf(line.replaceAll("\\D+","")))
    							.findFirst()
    							.orElse(null);
    					// Check that the question number corresponds to an actual question
    					// that the module leader has set
    					if(question != null) {
    						answer.setQuestion(question);
    						answer.setSubmission(submission);
    						questionsAnswered.add(question);
    					}
    				}
    			}
    			else {
    				query.append(line + "\n");
    			} 
    		}
    		// Add any questions where an answer wasn't found
    		if(!questionsAnswered.containsAll(questions)) {
    			questions.removeAll(questionsAnswered);
    			for (Question question : questions) {
    				answer = new Answer();
    				answer.setQuestion(question);
					answer.setSubmission(submission);
					submission.answersProperty().add(answer);
    			}
    		}

    		inFile.close();
    	} catch (Exception e) {
    		throw e;
		}
		return submission; 
    }
  
    private void importSubmissions() {
        /* 
         * BEGIN REFERENCE 
         * URL: https://docs.oracle.com/javase/8/javafx/api/javafx/concurrent/Task.html
         * ACCESSED: 15/04/2020
         */	
    	Task<Void> task = new Task<Void>() {
    		@Override
    		protected Void call() throws Exception {
    			int count = submissionsToImport.size();
    			for(int i = 0; i < submissionsToImport.size(); i++) {
    				Submission newSubmission = submissionsToImport.get(i);
    				this.updateMessage("Importing submission for student " + newSubmission.getStudentNumber());
    				// Create a list of studentNumbers associated with the existing submissions
    				 List<Integer> studentNumbers = selectedYearGroupSubmissions.stream()
    			        		.map(submission -> submission.getStudentNumber())
    			        		.collect(Collectors.toList());
    				// Check whether there's already an existing submission for that studentNumber and academic year
    				if(studentNumbers.contains(newSubmission.getStudentNumber())) {
    			        /* 
    			         * BEGIN REFERENCE 
    			         * URL: https://stackoverflow.com/questions/50214673/java-fx-new-modal-window-in-task?noredirect=1&lq=1
    			         * ACCESSED: 15/04/2020
    			         */
    					CompletableFuture.runAsync(() -> {
    						ButtonType overwriteButton = new ButtonType("Overwrite", ButtonData.OK_DONE);
    						ButtonType skipButton = new ButtonType("Skip", ButtonData.CANCEL_CLOSE);
    						Alert alert = new Alert (Alert.AlertType.CONFIRMATION, "A submisson for student " + 
    								newSubmission.getStudentNumber() + " already exists for the academic year " + newSubmission.getYearGroup().getAcademicYear()
    								+ ". Do you wish to overwrite the existing submission or skip this file?", overwriteButton, skipButton);
        	            	alert.setTitle("Submission already exists");
        	            	alert.setHeaderText("Overwirte or Skip Submission");
    						alert.showAndWait().ifPresent((btnType) -> {
    							if (btnType.getButtonData() == ButtonData.OK_DONE) {
    								Submission submissionToOverwrite= selectedYearGroupSubmissions.stream()
    										.filter(submission -> submission.getStudentNumber() == newSubmission.getStudentNumber())
    										.findFirst()
    										.orElse(null);
    								// Spent far too long trying to get hibernate to update the answers by calling clear() and addAll()
    								// and then upsertSubmisison() but couldn't get it to work, so settled for doing this manually
    								SubmissionDAO.deleteSubmission(submissionToOverwrite);
    								SubmissionDAO.insertSubmission(newSubmission);
    								selectedYearGroupSubmissions.remove(submissionToOverwrite);
    								selectedYearGroupSubmissions.add(newSubmission);
    							} 
    						});
    					}, Platform::runLater).join();
    					/* END REFERENCE */
    				}
    				else {
    					SubmissionDAO.insertSubmission(newSubmission);
    					selectedYearGroupSubmissions.add(newSubmission);
    				}
    				this.updateProgress(i, count);
    				Thread.sleep(100);
    			}
    			return null; 
    		}
    	};
    	/* END REFERENCE */
    		
    	task.setOnSucceeded(event -> {
    		// Re-sort the submissions list
    		selectedYearGroupSubmissions.sort(Comparator.comparing(Submission::getStudentNumber));
    		Alert alert = new Alert (Alert.AlertType.INFORMATION);
        	alert.setTitle("Import Submissions");
        	alert.setHeaderText("Import completed successfully");
        	alert.setContentText("The import has been completed and the submisisons have been added successfully");
        	alert.showAndWait();
        	rootLayoutController.removeTab(View.IMPORT_SUBMISSIONS.getTabName());
    	});
    	
    	// Handle failure
    	task.setOnFailed(event -> {
    		Alert alert = new Alert (Alert.AlertType.ERROR);
        	alert.setTitle("Import Submissions");
        	alert.setHeaderText("Import failed");
        	alert.setContentText("An unexpected error occoured and the import has been cancelled");
        	alert.showAndWait();
    	});
    	
    	ProgressDialog progressDialog = new ProgressDialog(task);
    	progressDialog.setTitle("Import Submissions");
        progressDialog.setHeaderText("Importing In Progress");
        progressDialog.setContentText("Please wait...");
    	new Thread(task).start(); 
    	progressDialog.showAndWait();
    	progressDialog.close();
    }
}
