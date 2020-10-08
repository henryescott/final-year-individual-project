package model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "question", uniqueConstraints = @UniqueConstraint(columnNames = {"yearGroup", "questionNumber"}))
public class Question {
	/* -------------- Attributes --------------- */
	private IntegerProperty questionId;
    private ObjectProperty<YearGroup> yearGroup;
    private IntegerProperty questionNumber;
    private DoubleProperty maxMarks;
    private StringProperty modelAnswer;
    
	/* -------------- Constructor -------------- */
	public Question() {
		this.questionId = new SimpleIntegerProperty();
		this.yearGroup = new SimpleObjectProperty<YearGroup>();
		this.questionNumber = new SimpleIntegerProperty();
		this.maxMarks = new SimpleDoubleProperty();
		this.modelAnswer = new SimpleStringProperty();
	}

	/* ---------- Getters and Setters ---------- */
	// Question ID
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getQuestionId() {
		return questionId.get();
	}

	public void setQuestionId(int questionId) {
		this.questionId.set(questionId);
	}
	
	public IntegerProperty questionIdProperty() {
		return questionId;
	}

	// Year Group
	@ManyToOne
	@JoinColumn(name = "yearGroup")
	public YearGroup getYearGroup() {
		return yearGroup.get();
	}

	public void setYearGroup(YearGroup yearGroup) {
		this.yearGroup.set(yearGroup);
	}
	
	public ObjectProperty<YearGroup> yearGroupProperty() {
		return yearGroup;
	}

	// Question Number
	public int getQuestionNumber() {
		return questionNumber.get();
	}

	public void setQuestionNumber(int questionNumber) {
		this.questionNumber.set(questionNumber);
	}
	
	public IntegerProperty questionNumberProperty() {
		return questionNumber;
	}
	
	// Max Marks
	public double getMaxMarks() {
		return maxMarks.get();
	}

	public void setMaxMarks(double maxMarks) {
		this.maxMarks.set(maxMarks);
	}
	
	public DoubleProperty maxMarksProperty() {
		return maxMarks;
	}
	
	// Model Answer
	@Column(columnDefinition = "varchar(max)")
	public String getModelAnswer() {
		return modelAnswer.get();
	}

	public void setModelAnswer(String modelAnswer) {
		this.modelAnswer.set(modelAnswer);
	}
	
	public StringProperty modelAnswerProperty() {
		return modelAnswer;
	}
}
