package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ConnectionSetting.class)
public abstract class ConnectionSetting_ {

	public static volatile SingularAttribute<ConnectionSetting, String> hostName;
	public static volatile SingularAttribute<ConnectionSetting, String> password;
	public static volatile SingularAttribute<ConnectionSetting, Integer> port;
	public static volatile SingularAttribute<ConnectionSetting, String> serviceName;
	public static volatile SingularAttribute<ConnectionSetting, Integer> connectionSettingId;
	public static volatile SingularAttribute<ConnectionSetting, String> username;

	public static final String HOST_NAME = "hostName";
	public static final String PASSWORD = "password";
	public static final String PORT = "port";
	public static final String SERVICE_NAME = "serviceName";
	public static final String CONNECTION_SETTING_ID = "connectionSettingId";
	public static final String USERNAME = "username";

}

