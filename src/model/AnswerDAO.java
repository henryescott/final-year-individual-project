package model;

import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class AnswerDAO {
	public static List<Tuple> runQuery(String sqlQuery) throws Exception {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.ORACLE).openSession();
		Transaction transaction = session.getTransaction();
		try {
			/* 
			 * Run the student answers within a transactions and roll it back - 
			 * in case someone submits an INSERT etc. that would alter the dataset
			 */
			transaction.begin();
			TypedQuery<Tuple> query = session.createNativeQuery(sqlQuery.replaceAll(";$", ""), Tuple.class);
			List<Tuple> results = query.getResultList();
			return results;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (transaction.isActive()) {
				transaction.rollback(); 
			}
			session.close();
		}	 
	}
	
	public static List<String> getAllFeedback(Question question) {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<Answer> answer = criteriaQuery.from(Answer.class);
		criteriaQuery.select(answer.get(Answer_.feedback))
			.where(criteriaBuilder.equal(answer.get(Answer_.QUESTION), question),
					criteriaBuilder.isNotNull(answer.get(Answer_.feedback))
			);
		criteriaQuery.distinct(true);		
		List<String> allFeedback = session.createQuery(criteriaQuery).getResultList();
		return allFeedback;	
	}
}
