package model;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import util.HibernateUtil;

public class ConnectionSettingDAO {
	public static ConnectionSetting getConnectionSetting() {
		try {
			Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<ConnectionSetting> criteriaQuery = criteriaBuilder.createQuery(ConnectionSetting.class);
			criteriaQuery.from(ConnectionSetting.class);
			ConnectionSetting connectionSetting = session.createQuery(criteriaQuery).getSingleResult();
			session.close();
			return connectionSetting;	
		}
		catch(NoResultException e) {
			return null;
		}
	}
	
	public static void upsertConnectionSetting(ConnectionSetting connection) {
		Session session = HibernateUtil.getSessionFactory(HibernateUtil.Database.H2).openSession();
		Transaction transaction = session.getTransaction();
		try {
			transaction.begin();
			session.saveOrUpdate(connection);
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