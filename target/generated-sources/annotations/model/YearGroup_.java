package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(YearGroup.class)
public abstract class YearGroup_ {

	public static volatile SingularAttribute<YearGroup, Integer> academicYear;
	public static volatile ListAttribute<YearGroup, Submission> submissions;
	public static volatile ListAttribute<YearGroup, Question> questions;

	public static final String ACADEMIC_YEAR = "academicYear";
	public static final String SUBMISSIONS = "submissions";
	public static final String QUESTIONS = "questions";

}

