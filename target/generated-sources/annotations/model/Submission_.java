package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Submission.class)
public abstract class Submission_ {

	public static volatile SingularAttribute<Submission, YearGroup> yearGroup;
	public static volatile SingularAttribute<Submission, Integer> submissionId;
	public static volatile SingularAttribute<Submission, Integer> studentNumber;
	public static volatile ListAttribute<Submission, Answer> answers;
	public static volatile SingularAttribute<Submission, Double> mark;

	public static final String YEAR_GROUP = "yearGroup";
	public static final String SUBMISSION_ID = "submissionId";
	public static final String STUDENT_NUMBER = "studentNumber";
	public static final String ANSWERS = "answers";
	public static final String MARK = "mark";

}

