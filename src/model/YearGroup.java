package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "year_group", uniqueConstraints = @UniqueConstraint(columnNames = {"academicYear"}))
public class YearGroup {
	/* -------------- Attributes --------------- */
	private IntegerProperty academicYear;
    private ListProperty<Submission> submissions;
    private ListProperty<Question> questions;
    
    /* -------------- Constructor -------------- */
	public YearGroup() {
		this.academicYear = new SimpleIntegerProperty();
		this.submissions = new SimpleListProperty<Submission>(FXCollections.observableList(new ArrayList<Submission>()));
		this.questions = new SimpleListProperty<Question>(FXCollections.observableList(new ArrayList<Question>()));
	}

	/* ---------- Getters and Setters ---------- */
	// Academic Year
	@Id 
	public int getAcademicYear() {
		return academicYear.get();
	}

	public void setAcademicYear(int academicYear) {
		this.academicYear.set(academicYear);
	}
	
	public IntegerProperty academicYearProperty() {
		return academicYear;
	}
	
	// Submissions
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "yearGroup", orphanRemoval = true)
	@OrderBy("studentNumber")
	public List<Submission> getSubmissions() {
		return submissions.get();
	}

	public void setSubmissions(List<Submission> submissions) {
		this.submissions.set(FXCollections.observableList(submissions));
	}
	
	public ListProperty<Submission> submissionsProperty() {
		return submissions;
	}
	
	// Questions
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "yearGroup", orphanRemoval = true)
	public List<Question> getQuestions() {
		return questions.get();
	}

	public void setQuestions(List<Question> questions) {
		this.questions.set(FXCollections.observableList(questions));
	}
	
	public ListProperty<Question> questionsProperty() {
		return questions;
	}
}
