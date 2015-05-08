package goldpoisk_parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CurrentDatabase{
	
	Connection connection = null;
	final String DB_URL="https://web456.webfaction.com/phpPgAdmin/";
	final String DB_NAME="copy_goldpoisk";
	final String DB_USER="dumper";
	final String DB_PASSWORD="07IV6xp4jSBtJ";
	
	public CurrentDatabase(){
		/*Class.forName("org.postgresql.Driver");
		
		try {
 
			connection = DriverManager.getConnection(
					"jdbc:postgresql://"+DB_URL+"/"+DB_NAME, DB_USER,
					DB_PASSWORD);
 
		} catch (SQLException e) {
		}*/
	}
	
	public Ring getProduct(String article){
		Ring object = new Ring();
		
		return object;
	}
	

	
}