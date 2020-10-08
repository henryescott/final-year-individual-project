package model;

import org.hibernate.Session;
import org.hibernate.Transaction;

import util.HibernateUtil;

public class QuestionDAO {
	public static void insertQuestion(Question question) {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.save(question);
			transaction.commit();
		}
		catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback(); 
			}
			throw e;
		}
		finally {
			session.close();
		}
	}
	
	public static void deleteQuestion(Question question) {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.remove(question);
			transaction.commit();
		}
		catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback(); 
			}
			throw e;
		}
		finally {
			session.close();
		}
	}
}


