package controller;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Submission;
import model.YearGroup;
import util.ViewLoader.View;

public class YearGroupsController {
	/* ---------------- Fields ---------------- */
	@FXML
    private TableView<YearGroup> yearGroupsTable;
    @FXML
    private TableColumn<YearGroup, String>  academicYearColumn;
    @FXML
    private TableColumn<YearGroup, Integer> submissionsCountColumn;
    @FXML
    private TableColumn<YearGroup, Long> submissionsMarkedColumn;
    @FXML
    private TableColumn<YearGroup, Double> maxMarkColumn;
    @FXML
    private TableColumn<YearGroup, String> averageMarkColumn;
    @FXML
    private Button newYearGroup;
    
    private RootLayoutController rootLayoutController;
    private ObservableList<YearGroup> allYearGroups;
 
    /* -------------- Constructor -------------- */
    public YearGroupsController(RootLayoutController rootLayoutController, ObservableList<YearGroup> allYearGroups) {
		this.rootLayoutController = rootLayoutController;
		this.allYearGroups = allYearGroups;
	}

    /* ---------------- Methods ---------------- */
    @FXML
    private void initialize () {
    	// ------- Table Column Setup -------
    	academicYearColumn.setCellValueFactory(cellData -> {
    		SimpleStringProperty academicYear = new SimpleStringProperty();
    		int academicYearEnd = Integer.parseInt(String.valueOf(cellData.getValue().getAcademicYear()).substring(2, 4)) + 1;
    		academicYear.setValue(cellData.getValue().getAcademicYear() + "/" + String.format("%02d",academicYearEnd));
			return academicYear;
    	});
    	submissionsCountColumn.setCellValueFactory(cellData -> cellData.getValue().submissionsProperty().sizeProperty().asObject());
    	submissionsMarkedColumn.setCellValueFactory(cellData -> {
            /* 
             * BEGIN REFERENCE 
             * URL: https://stackoverflow.com/questions/30915007/javafx-filteredlist-filtering-based-on-property-of-items-in-the-list
             * ACCESSED: 01/05/2020
             */
    		ObservableList<Submission> listWithExtractor =  FXCollections.observableArrayList(submission -> new Observable[]{submission.markProperty()});
    		/* END REFERENCE */
    		Bindings.bindContent(listWithExtractor, cellData.getValue().submissionsProperty());
    		return Bindings.createLongBinding(
    			    () -> listWithExtractor
    			    .stream()
	    			.filter(yearGroup -> yearGroup.getMark() != null)
	    			.count(), listWithExtractor).asObject();
    	});
    	maxMarkColumn.setCellValueFactory(cellData -> { 
    		return Bindings.createDoubleBinding(
    			    () -> cellData.getValue().questionsProperty()
    			    .stream()
	    			.mapToDouble(question -> question.getMaxMarks())
	    			.sum(), cellData.getValue().questionsProperty()).asObject();
    	});
    	averageMarkColumn.setCellValueFactory(cellData -> {
            /* 
             * BEGIN REFERENCE 
             * URL: https://stackoverflow.com/questions/30915007/javafx-filteredlist-filtering-based-on-property-of-items-in-the-list
             * ACCESSED: 01/05/2020
             */
    		ObservableList<Submission> listWithExtractor =  FXCollections.observableArrayList(submission -> new Observable[]{submission.markProperty()});
    		/* END REFERENCE */
    		Bindings.bindContent(listWithExtractor, cellData.getValue().submissionsProperty());
    		DoubleBinding average = Bindings.createDoubleBinding(
    			    () -> listWithExtractor
    			    .stream()
	    			.filter(yearGroup -> yearGroup.getMark() != null)
	    			.mapToDouble(yearGroup -> yearGroup.markProperty().getValue())
	    			.average()
	    			.orElse(Double.NaN), listWithExtractor);
    		ObjectProperty<Double> averageProperty = new SimpleObjectProperty<Double>();
    		averageProperty.bind(average.asObject());
    		// Show blank cell rather than NaN when average is unable to be calculated
    		return Bindings.when(averageProperty.isEqualTo(Double.NaN))
				      .then(new SimpleStringProperty())
				      .otherwise(averageProperty.asString("%.02f"));
    	});
    	
        // ------- Event Handlers -------
    	// New Year Group Button
    	newYearGroup.setOnAction((event) -> {
        	NewYearGroupController controller = new NewYearGroupController(rootLayoutController, allYearGroups);
        	rootLayoutController.addTab(View.NEW_YEAR_GROUP, controller, View.NEW_YEAR_GROUP.getTabName());
    	});
    	
    	// ------- Initial Display -------
        yearGroupsTable.setItems(allYearGroups);
    }
}
