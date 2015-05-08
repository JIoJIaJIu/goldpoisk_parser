package goldpoisk_parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CurrentDatabase{
	
	Connection connection = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null; 
	
	final String DB_URL = Parser.config.postgresql_url;
	final String DB_NAME = Parser.config.postgresql_db;
	final String DB_USER = Parser.config.postgresql_user;
	final String DB_PASSWORD = Parser.config.postgresql_password;
	final String DB_SCHEMA = Parser.config.postgresql_schema;
	
	public CurrentDatabase(){
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(
					"jdbc:postgresql://"+DB_URL+"/"+DB_NAME, 
					DB_USER,
					DB_PASSWORD);
			statement = connection.createStatement(); 
			try { 
				statement.execute("set search_path to '" + DB_SCHEMA + "'"); 
			} 
			finally { 
				statement.close(); 
			}
		} catch (Exception e) {
			Parser.logger.error("Error while connect to PostgreSQL Database" + e.getMessage());
		}
	}
	
	public Ring getProduct(String article){
		Ring ring = new Ring();
		
		String sql_query="SELECT * FROM product_product pp"
				+ " LEFT JOIN product_item pi ON pi.id=pp.id"
				+ " WHERE pp.number = ?";
		
		preparedStatement = connection.prepareStatement(sql_query);
		preparedStatement.setString(0, article);
		
		try{
			resultSet = preparedStatement.executeQuery();
		}catch(Exception e){
			Parser.logger.error("Error while execute query in CurrentDatabase.getProduct method");
		}
		
		try{
			ring.article=resultSet.getString("number");
			ring.price=resultSet.getString("cost");
			ring.count=resultSet.getString("quantity");
		}catch(Exception e){
			Parser.logger.error("Error while get data from PostgreSQL in CurrentDatabase.getProduct method");
		}
		
		resultSet.close();
		
		return ring;
	}
	
	public boolean existProduct(String article){
		boolean result=false;
		
		String sql_query="SELECT COUNT(*) as count FROM product_product pp"
				+ " WHERE pp.number = ?";
		
		preparedStatement = connection.prepareStatement(sql_query);
		preparedStatement.setString(0, article);
		
		try{
			resultSet = preparedStatement.executeQuery();
		}catch(Exception e){
			Parser.logger.error("Error while execute query in CurrentDatabase.getProduct method");
		}
		
		try{
			result = resultSet.getInt("count") == 0 ? false : true;
		}catch(Exception e){
			Parser.logger.error("Error while get data from PostgreSQL in CurrentDatabase.getProduct method");
		}
		
		resultSet.close();
		
		return result;
	}

	
}