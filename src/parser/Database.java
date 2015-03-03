package goldpoisk_parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	 	
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	    static final String DB_URL = "jdbc:mysql://localhost/java";

	    static final String USER = "root";
	    static final String PASS = "";
	    
	    java.sql.Connection conn = null;
	    Statement stmt = null;
	    
	    public Database(){
	    	try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection(DB_URL,USER,PASS);
				stmt = conn.createStatement();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	
	    }
	    
	    void save(Ring ring){
	    	
	    	String select = "SELECT * FROM goldpoisk_entity WHERE article = ?";
	    	ResultSet rs = null;
	    	int count=0;
	    	try {
				PreparedStatement result = conn.prepareStatement(select);
				result.setString (1, ring.article);
				rs=result.executeQuery();
				while (rs.next()) {
		    	    ++count;
		    	}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

	    	
	    	
	    	if(count==0){
		    	String query="INSERT INTO goldpoisk_entity"+
		    			" (article,name,material,category,weight,url,type)"+
		    			" VALUES "+
		    			" (?,?,?,?,?,?,'Rings')";
		    	try {
					PreparedStatement preparedStmt = conn.prepareStatement(query);
					preparedStmt.setString (1, ring.article);
					preparedStmt.setString (2, ring.name);
					preparedStmt.setString (3, ring.material);
					preparedStmt.setString (4, ring.category);
					preparedStmt.setString (5, ring.weight);
					preparedStmt.setString (6, ring.url);
					preparedStmt.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		    	
		    	for(int i=0;i<ring.kamni.size();i++){
		    		String query_kamni="INSERT INTO goldpoisk_kamni"+
			    			" (article,name,color,weight,size)"+
			    			" VALUES "+
			    			" (?,?,?,?,?)";
			    	try {
						PreparedStatement preparedStmt = conn.prepareStatement(query_kamni);
						preparedStmt.setString (1, ring.article);
						preparedStmt.setString (2, ring.kamni.get(i));
						preparedStmt.setString (3, i<ring.kamniColor.size()?ring.kamniColor.get(i):"");
						preparedStmt.setString (4, i<ring.kamniWeight.size()?ring.kamniWeight.get(i):"");
						preparedStmt.setString (5, i<ring.kamniSize.size()?ring.kamniSize.get(i):"");
						preparedStmt.execute();
					} catch (SQLException e) {
						e.printStackTrace();
					}
		    	}
		    	
		    	for(int i=0;i<ring.images.size();i++){
		    		ByteArrayOutputStream image = ring.images.get(i);
		    		String query_kamni="INSERT INTO goldpoisk_entity_images"+
			    			" (article,image)"+
			    			" VALUES "+
			    			" (?,?)";
			    	try {
						PreparedStatement preparedStmt = conn.prepareStatement(query_kamni);
						preparedStmt.setString (1, ring.article);
						ByteArrayInputStream bais = new ByteArrayInputStream(image.toByteArray());
						preparedStmt.setBlob(2, bais);
						//preparedStmt.setBinaryStream(2, image, (int) image.available());
						preparedStmt.execute();
					} catch (SQLException e) {
						e.printStackTrace();
					}
		    	}
	    	}
	    }
}
