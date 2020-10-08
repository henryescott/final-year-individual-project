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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "answer")
public class Answer {
	/* -------------- Attributes --------------- */
	private IntegerProperty answerId;
	private ObjectProperty<Submission> submission;
    private ObjectProperty<Question> question;
    private StringProperty query;
	// Can't simply just use DoubleProperty as it isn't nullable (wraps double not Double)
	private SimpleObjectProperty<Double> mark;
    private StringProperty feedback;
    
    /* -------------- Constructor -------------- */
	public Answer() {
		this.answerId = new SimpleIntegerProperty();
		this.submission = new SimpleObjectProperty<Submission>();
		this.question = new SimpleObjectProperty<Question>();
		this.query = new SimpleStringProperty();
		this.mark = new SimpleObjectProperty<Double>();
		this.feedback = new SimpleStringProperty();
	}

	/* ---------- Getters and Setters ---------- */
	// Answer ID
	@Id 
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getAnswerId() {
		return answerId.get();
	}

	public void setAnswerId(int answerId) {
		this.answerId.set(answerId);
	}
	
	public IntegerProperty answerIdProperty() {
		return answerId;
	}
	
	// Question
	@ManyToOne
	@JoinColumn(name = "questionId")
	public Question getQuestion() {
		return question.get();
	}

	public void setQuestion(Question question) {
		this.question.set(question);
	}
	
	public ObjectProperty<Question> questionProperty() {
		return question;
	}

	// Submission
	@ManyToOne
    @JoinColumn(name = "submissionId")
	public Submission getSubmission() {
		return submission.get();
	}

	public void setSubmission(Submission submission) {
		this.submission.set(submission);
	}
	
	public ObjectProperty<Submission> submissionProperty() {
		return submission;
	}
	
	// Query
	@Column(columnDefinition = "varchar(max)")
	public String getQuery() {
		return query.get();
	}
	
	public void setQuery(String query) {
		this.query.set(query);
	}
	
	public StringProperty queryProperty() {
		return query;
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
	
	// Feedback
	@Column(columnDefinition = "varchar(max)")
	public String getFeedback() {
		return feedback.get();
	}

	public void setFeedback(String feedback) {
		this.feedback.set(feedback);
	}

	public StringProperty feedbackProperty() {
		return feedback;
	}
}
