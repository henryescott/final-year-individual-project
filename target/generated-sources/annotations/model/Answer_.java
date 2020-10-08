package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Answer.class)
public abstract class Answer_ {

	public static volatile SingularAttribute<Answer, Integer> answerId;
	public static volatile SingularAttribute<Answer, String> feedback;
	public static volatile SingularAttribute<Answer, Question> question;
	public static volatile SingularAttribute<Answer, String> query;
	public static volatile SingularAttribute<Answer, Submission> submission;
	public static volatile SingularAttribute<Answer, Double> mark;

	public static final String ANSWER_ID = "answerId";
	public static final String FEEDBACK = "feedback";
	public static final String QUESTION = "question";
	public static final String QUERY = "query";
	public static final String SUBMISSION = "submission";
	public static final String MARK = "mark";

}

