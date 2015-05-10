package goldpoisk_parser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;


@SuppressWarnings("deprecation")
public class CurrentDatabase{
	
	Connection connection = null;
	Statement statement = null;
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null; 
	SessionFactory sessionFactory = null;
	Session session = null;
	
	final String DB_URL = Parser.config.postgresUrl;
	final String DB_NAME = Parser.config.postgresDB;
	final String DB_USER = Parser.config.postgresName;
	final String DB_PASSWORD = Parser.config.postgresPassword;
	final String DB_SCHEMA = Parser.config.postgresSchema;
	
	public CurrentDatabase(){
		
		//Class.forName("org.postgresql.Driver");
		
		try {
				File f = new File("hibernate.cfg.xml");
				AnnotationConfiguration conf = new AnnotationConfiguration().configure(f);
				// conf.setProperty("hibernate.connection.url", "jdbc:postgresql://"+DB_URL+DB_NAME);
				// Parser.logger.error(DB_URL+DB_NAME);
				conf.setProperty("hibernate.connection.username", DB_USER);
				conf.setProperty("hibernate.connection.password", DB_PASSWORD);
				StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(conf.getProperties());
				sessionFactory = conf.buildSessionFactory(ssrb.build());
				session = sessionFactory.openSession();
			   	Parser.logger.error("Successfully connect to Postgres Database");
		    } catch (Throwable ex) {
		       	Parser.logger.error("Error while connect to Postgres Database");
		    	throw new ExceptionInInitializerError(ex);
		    }
	}
	
	public Product getProduct(String article) throws SQLException {
		Product product = new Product();
		
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
			product.article=resultSet.getString("number");
			product.price=resultSet.getString("cost");
			product.count=resultSet.getString("quantity");
		}catch(Exception e){
			Parser.logger.error("Error while get data from PostgreSQL in CurrentDatabase.getProduct method");
		}
		
		resultSet.close();
		
		return product;
	}
	
	public boolean existProduct(String article) throws SQLException {
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
