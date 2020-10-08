package model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.jasypt.hibernate5.type.EncryptedStringType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table(name = "connection_setting")
/* 
 * BEGIN REFERENCE 
 * URL: http://www.jasypt.org/hibernate.html
 * ACCESSED: accessed: 06/05/2020
 */	
@TypeDef(
	    name="encryptedString", 
	    typeClass=EncryptedStringType.class, 
	    parameters= {
	        @Parameter(name="encryptorRegisteredName", value="stringEncryptor")
	    }
)
/* END REFERENCE */
public class ConnectionSetting {
	/* -------------- Attributes --------------- */
	private IntegerProperty connectionSettingId;
	private StringProperty hostName;
	private IntegerProperty port;
	private StringProperty serviceName;
	private StringProperty username;
	private StringProperty password;
	
	/* -------------- Constructor -------------- */
	public ConnectionSetting() {
		this.connectionSettingId = new SimpleIntegerProperty();
		this.hostName = new SimpleStringProperty();
		this.port = new SimpleIntegerProperty();
		this.serviceName = new SimpleStringProperty();
		this.username = new SimpleStringProperty();
		this.password = new SimpleStringProperty();
	}
	
	/* ---------- Getters and Setters ---------- */
	// Connection ID
	@Id 
	@GeneratedValue
	public int getConnectionSettingId() {
		return connectionSettingId.get();
	}

	public void setConnectionSettingId(int connectionSettingId) {
		this.connectionSettingId.set(connectionSettingId);
	}
	
	public IntegerProperty connectionSettingIdProperty() {
		return connectionSettingId;
	}

	// Host Name 
	public String getHostName() {
		return hostName.get();
	}

	public void setHostName(String hostName) {
		this.hostName.set(hostName);
	}

	public StringProperty hostNameProperty() {
		return hostName;
	}

	// Port
	public int getPort() {
		return port.get();
	}

	public void setPort(int port) {
		this.port.set(port);
	}
	
	public IntegerProperty portProperty() {
		return port;
	}

	// Service Name
	public String getServiceName() {
		return serviceName.get();
	}

	public void setServiceName(String serviceName) {
		this.serviceName.set(serviceName);
	}
	
	public StringProperty serviceNameProperty() {
		return serviceName;
	}

	// Username
	public String getUsername() {
		return username.get();
	}

	public void setUsername(String username) {
		this.username.set(username);
	}
	
	public StringProperty usernameProperty() {
		return username;
	}

	// Password
	@Type(type="encryptedString")
	public String getPassword() {
		return password.get();
	}

	public void setPassword(String password) {
		this.password.set(password);
	}
	
	public StringProperty passwordProperty() {
		return password;
	}
}
