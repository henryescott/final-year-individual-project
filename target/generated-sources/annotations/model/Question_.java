package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Question.class)
public abstract class Question_ {

	public static volatile SingularAttribute<Question, YearGroup> yearGroup;
	public static volatile SingularAttribute<Question, Integer> questionId;
	public static volatile SingularAttribute<Question, String> modelAnswer;
	public static volatile SingularAttribute<Question, Double> maxMarks;
	public static volatile SingularAttribute<Question, Integer> questionNumber;

	public static final String YEAR_GROUP = "yearGroup";
	public static final String QUESTION_ID = "questionId";
	public static final String MODEL_ANSWER = "modelAnswer";
	public static final String MAX_MARKS = "maxMarks";
	public static final String QUESTION_NUMBER = "questionNumber";

}

