package util;

import java.io.File;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.service.spi.ServiceException;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import model.AnswerDAO;
import model.YearGroup;

//Class containing all common UI components 
public class CommonComponents {
	public static ComboBox<YearGroup> yearGroupComboBox() {
		ComboBox<YearGroup> selectYear = new ComboBox<YearGroup>();
		/* 
		 * BEGIN REFERENCE 
		 * URL: https://stackoverflow.com/questions/20283940/javafx-combobox-with-custom-object-displays-object-address-though-custom-cell-fa
		 * ACCESSED: accessed: 04/05/2020
		 */	
		selectYear.setCellFactory(cellFactory -> new ListCell<YearGroup>() {
			@Override
			protected void updateItem(YearGroup item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setText(null);
				} else {
					int academicYearEnd = Integer.parseInt(String.valueOf(item.getAcademicYear()).substring(2, 4)) + 1;
					setText(item.getAcademicYear() + "/" + String.format("%02d",academicYearEnd));
				}
			}
		});
		selectYear.setButtonCell(selectYear.getCellFactory().call(null));
		return selectYear;
		/* END REFERENCE */
	}
	
	public static class ResultsTable {
		public static void createQueryResultsTable(String query, AnchorPane anchorPane) throws Exception {
	    	if (query == null || query.isEmpty()) {
	    		anchorPane.getChildren().add(createErrorMessageLabel("Unable to display results. No query provided."));
	    	}
	    	else {
	    		try {
	        		// Run the query and grab the results
	        		List<Tuple> results = AnswerDAO.runQuery(query);
	        		// Create a List representation of the tuples
	        		List<List<Object>> resultsMap = createResultsMap(results);
	        		// The query ran, but didn't produce any results
	        		if (results.size() == 0) {
	                		anchorPane.getChildren().add(createErrorMessageLabel("Unable to display results. The query "
	                				+ "ran successfully but did not produce any results."));
	                }
	        	    else {
	        	    	// Dynamically create the results table
	            		TableView<List<Object>> resultsTable = new TableView<List<Object>>();
	            		/* 
	            		 * BEGIN REFERENCE 
	            		 * URL: https://stackoverflow.com/questions/42970625/javafx-create-a-dynamic-tableview-with-generic-types
	            		 * ACCESSED: accessed: 30/04/2020
	            		 */	
	        	    	Tuple tuple = results.get(0);
	            		for (int i = 0; i < tuple.getElements().size(); i++) {
	            			int columnIndex = i;
	            			TableColumn<List<Object>, Object> column = new TableColumn<List<Object>, Object>(tuple.getElements().get(i).getAlias());
	            			column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(columnIndex)));
	            			column.setSortable(false);
	            			resultsTable.getColumns().add(column);
	            		}
	            		/* END REFERENCE */
	            		// Set items 
	            		resultsTable.getItems().setAll(resultsMap);
	            		// Alter display settings
	            		resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	            		anchorPane.getChildren().add(resultsTable);
	            		AnchorPane.setTopAnchor(resultsTable, 0.0);
	            		AnchorPane.setRightAnchor(resultsTable, 0.0);
	            		AnchorPane.setBottomAnchor(resultsTable, 0.0);
	            		AnchorPane.setLeftAnchor(resultsTable, 0.0);
	            		resultsTable.setPrefHeight(0.0);	
	        	    }
	        	}
	        	// The same exception type is thrown for multiple exception sources
	        	catch(PersistenceException e) {
	        		// Catch the exception, find out whether it was the student's SQL that caused it
	        		if(e.getCause().getCause() instanceof SQLSyntaxErrorException) {
	        			anchorPane.getChildren().add(createErrorMessageLabel("Unable to display results. "
	        					+ "Query failed to run."));
	        		}
	        		// If the student's SQL wasn't the cause, throw exception as normal
	        		else {
	        			throw e;
	        		}
	        	}
	    	}
	    }
		
		private static List<List<Object>> createResultsMap(List<Tuple> queryResults) {
	    	List<List<Object>> resultsMap = new ArrayList<List<Object>>();
			for (Tuple row : queryResults) {
				List<Object> rowMap = new ArrayList<Object>();
				for (TupleElement<?> element : row.getElements()) {
					rowMap.add(row.get(element.getAlias()).toString());
			    }
				resultsMap.add(rowMap);
			}
			return resultsMap;
	    }
	}
	
	public static Label createErrorMessageLabel(String errorMessage) {
    	Label errorMessageLabel = new Label(errorMessage);
    	errorMessageLabel.setAlignment(Pos.CENTER);
		AnchorPane.setTopAnchor(errorMessageLabel, 0.0);
    	AnchorPane.setRightAnchor(errorMessageLabel, 0.0);
    	AnchorPane.setBottomAnchor(errorMessageLabel, 0.0);
    	AnchorPane.setLeftAnchor(errorMessageLabel, 0.0);
    	return errorMessageLabel;
    }
	
	/* 
	 * BEGIN REFERENCE 
	 * URL: https://stackoverflow.com/questions/29489366/how-to-add-button-in-javafx-table-view
	 * ACCESSED: accessed: 04/05/2020
	 */	
	public static class ActionButtonTableCell<S> extends TableCell<S, Button> {

	    private final Button actionButton;

	    public ActionButtonTableCell(String label, Function<S, S> function) {
	        this.actionButton = new Button(label);
	        this.actionButton.setOnAction((ActionEvent e) -> {
	            function.apply(getCurrentItem());
	        });
	    }

	    public S getCurrentItem() {
	        return (S) getTableView().getItems().get(getIndex());
	    }

	    public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, Function< S, S> function) {
	        return param -> new ActionButtonTableCell<>(label, function);
	    }

	    @Override
	    public void updateItem(Button item, boolean empty) {
	        super.updateItem(item, empty);
	        if (empty) {
	            setGraphic(null);
	        } else {      
	        	setAlignment(Pos.CENTER);
	            setGraphic(actionButton);
	        }
	    }
	    
	}	
	/* END REFERENCE */
	
	public static class Alerts {
		public static Alert saveFileSuccess(File file) {
        	Alert alert = new Alert (Alert.AlertType.INFORMATION);
        	alert.setTitle("File Saved Successfully");
        	alert.setHeaderText("File saved");
        	alert.setContentText("The file was successfully saved to " + file.getPath());
			return alert;
		}
		
		public static Alert saveFileFailure(File file, Exception e) {
			Alert alert = new Alert (Alert.AlertType.ERROR);
        	alert.setTitle("Error Saving File");
        	alert.setHeaderText("Unable to save file");
        	alert.setContentText("An error occoured while attempting to save the file to " + file.getPath());
			return alert;
		}
		
		public static Alert connectionFailure(Throwable throwable, String alertTitle, String alertHeaderText) {
			String alertContentText;
        	if(throwable instanceof JDBCConnectionException) {
        		alertContentText = "Lost connection to database";
        	}
        	else if (throwable instanceof ServiceException) {
        		alertContentText = throwable.getCause().getCause().getMessage();
        	}
        	else if (throwable instanceof NullPointerException) {
        		alertContentText = "Unable to connect to database";
        	}
        	else {
        		alertContentText = "An unexpected error occurred";
        	}
        	Alert alert = new Alert (Alert.AlertType.ERROR);
        	alert.setTitle(alertTitle);
        	alert.setHeaderText(alertHeaderText);
        	alert.setContentText(alertContentText);
			return alert;
		}
		
		public static Alert deleteConfirmation(String itemType, String contentText) {
        	Alert alert = new Alert (Alert.AlertType.CONFIRMATION);
        	alert.setTitle("Delete " + itemType);
        	alert.setHeaderText("Are you sure you want to delete this item?");
        	alert.setContentText("Are you sure you want to delete " + itemType.toLowerCase() + " " + contentText + 
        			"? This cannot be undone.");
			return alert;
		}
	}
}
