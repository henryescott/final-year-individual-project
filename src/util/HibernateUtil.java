package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate5.encryptor.HibernatePBEEncryptorRegistry;

import model.ConnectionSetting;
import model.ConnectionSettingDAO;
  
public class HibernateUtil {
	
	public enum Database {
		 H2,
		 ORACLE
	}
	
    private static final SessionFactory h2SessionFactory = buildSessionFactory(Database.H2);
    private static SessionFactory oracleSessionFactory = buildSessionFactory(Database.ORACLE);
	
    private static SessionFactory buildSessionFactory(Database database) {
        try { 	
        	switch (database) {
	            case H2:
	            	// Set up the jasyspt encryptor for the connection setting password. We have
	            	// to use encryption rather than hashing because we need to decrypt in
	            	// for the hibernate connection to the Oracle DB
	            	StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
	                strongEncryptor.setPassword("FPTUm6xuUhsvyzgmi3U03N2rX8#ls73itoNcYy1FDBqbCu&#rDwl");
	                HibernatePBEEncryptorRegistry registry = HibernatePBEEncryptorRegistry.getInstance();
	                registry.registerPBEStringEncryptor("stringEncryptor", strongEncryptor);
	            	return new Configuration().configure("/h2hibernate.cfg.xml").buildSessionFactory();
	            case ORACLE:
	            	ConnectionSetting connectionSetting = ConnectionSettingDAO.getConnectionSetting();
	        		Configuration configuration = new Configuration();
	        		configuration.setProperty("connection.driver_class", "oracle.jdbc.OracleDriver");
	        		configuration.setProperty(
	        			"hibernate.connection.url", "jdbc:oracle:thin:@" + 
	        			connectionSetting.getHostName() + ":" + 
	        			connectionSetting.getPort() + "/" + 
	        			connectionSetting.getServiceName());
	        		configuration.setProperty("hibernate.connection.username", connectionSetting.getUsername());
	        		configuration.setProperty("hibernate.connection.password", connectionSetting.getPassword());
	        		configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle12cDialect");
	                return configuration.buildSessionFactory();
        	}
        } catch (Exception e) {
        	// Deliberately swallow exceptions here - we handle them when the
        	// controllers call getSessionFactory() as the return value will be null 
        	// and throw an exception.
        }
		return null;
    }
  
    public static SessionFactory getSessionFactory(Database database) {
    	switch (database) {
        	case H2:
        		return h2SessionFactory;
	        case ORACLE:
	        	// If the connection is null, then try refreshing (setting may have updated, connection back etc.)
	        	if(oracleSessionFactory == null) {
	        		oracleSessionFactory = buildSessionFactory(Database.ORACLE);
	    		}
	        	return oracleSessionFactory;
    	}
		return null;
    }

    public static void closeSessionFactory(Database database) {
        getSessionFactory(database).close();
    }
    
    public static void testConnection(String hostName, String port, String serviceName, String userName, String password) throws Exception {
	    	Configuration configuration = new Configuration();
			configuration.setProperty("connection.driver_class", "oracle.jdbc.OracleDriver");
			configuration.setProperty("hibernate.connection.url", "jdbc:oracle:thin:@" + 
					hostName + ":" + port + "/" + serviceName);
			configuration.setProperty("hibernate.connection.username", userName);
			configuration.setProperty("hibernate.connection.password", password);
			configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle12cDialect");
			SessionFactory sessionFactory = configuration.buildSessionFactory();
			sessionFactory.close();
    }
}
