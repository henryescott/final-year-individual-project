package model;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

import util.HibernateUtil;

public class YearGroupDAO {	
	public static List<YearGroup> getAll() {		
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<YearGroup> criteriaQuery = criteriaBuilder.createQuery(YearGroup.class);
		Root<YearGroup> academicYear = criteriaQuery.from(YearGroup.class);
		criteriaQuery.orderBy(criteriaBuilder.desc(academicYear.get(YearGroup_.academicYear)));
		List<YearGroup> allYearGroups = session.createQuery(criteriaQuery).getResultList();
		// Deliberately don't close session to allow lazy loading
		// session.close();
		return allYearGroups;		
	}

	public static void upsertYearGroup(YearGroup yearGroup) {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.saveOrUpdate(yearGroup);
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
