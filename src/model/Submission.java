package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "submission", uniqueConstraints = @UniqueConstraint(columnNames = {"studentNumber", "yearGroup"}))
public class Submission {
	/* -------------- Attributes --------------- */
	private IntegerProperty submissionId;
	private IntegerProperty studentNumber;
	private ObjectProperty<YearGroup> yearGroup;
	private ListProperty<Answer> answers;
	// Can't simply just use DoubleProperty as it isn't nullable (wraps double not Double)
	private SimpleObjectProperty<Double> mark;
	
	/* -------------- Constructor -------------- */
	public Submission() {
		this.submissionId = new SimpleIntegerProperty();
		this.studentNumber = new SimpleIntegerProperty();
		this.yearGroup = new SimpleObjectProperty<YearGroup>();
		this.answers = new SimpleListProperty<Answer>(FXCollections.observableList(new ArrayList<Answer>()));
		this.mark = new SimpleObjectProperty<Double>();
	}

	/* ---------- Getters and Setters ---------- */
	// Submission ID
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getSubmissionId() {
		return submissionId.get();
	}
	
	public void setSubmissionId(int submissionId) {
		this.submissionId.set(submissionId);
	}
	
	public IntegerProperty submissionIdProperty() {
		return submissionId;
	}
	
	// Student Number
	public int getStudentNumber() {
		return studentNumber.get();
	}

	public void setStudentNumber(int studentNumber) {
		this.studentNumber.set(studentNumber);
	}
	
	public IntegerProperty studentNumberProperty() {
		return studentNumber;
	}
	
	// Academic Year
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
	
	// Answers
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "submission", orphanRemoval = true)
	public List<Answer> getAnswers() {
		return answers.get();
	}
	
	public void setAnswers(List<Answer> answers) {
		this.answers.set(FXCollections.observableList(answers));
	}
	
	public ListProperty<Answer> answersProperty() {
		return answers;
	}
	
	// Mark
	public Double getMark() {
		return mark.get();
	}

	public void setMark(Double mark) {
		this.mark.set(mark);
	}
	
	public SimpleObjectProperty<Double> markProperty() {
		return mark;
	}
}
