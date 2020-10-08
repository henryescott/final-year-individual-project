package model;

import org.hibernate.Session;
import org.hibernate.Transaction;

import util.HibernateUtil;

public class SubmissionDAO {
	public static void deleteSubmission(Submission submission) {		
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.remove(submission);
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
	
	public static void insertSubmission(Submission submission) {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.save(submission);
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
	
	public static void upsertSubmission(Submission submission) {		
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.saveOrUpdate(submission);
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
