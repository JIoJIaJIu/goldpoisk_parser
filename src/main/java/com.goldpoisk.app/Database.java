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
	    static String DB_URL = "jdbc:mysql://localhost/";

	    static final String USER = "root";
	    static final String PASS = "";
	    
	    java.sql.Connection conn = null;
	    Statement stmt = null;
	    
	    public Database(String category){
	    	DB_URL+=category;
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
	    
	    boolean isExisted(String article){
	    	String select = "SELECT * FROM goldpoisk_entity WHERE article = ?";
	    	ResultSet rs = null;
	    	int count=0;
	    	boolean ret=true;
	    	try {
				PreparedStatement result = conn.prepareStatement(select);
				result.setString (1, article);
				rs=result.executeQuery();
				while (rs.next()) {
		    	    ++count;
		    	}
				if(count==0)
					ret=false;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
	    	
	    	return ret;
	    	
	    }
	    
	   /* void savePrice(Ring ring){
	    	String query = "INSERT into goldpoisk_other "+
	    					" (article,price,description)"+
	    					" VALUES "+
	    					" (?,?,?)";
	    	try {
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setString (1, ring.article);
				preparedStmt.setString (2, ring.price);
				preparedStmt.setString (3, ring.description);
				preparedStmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }*/
	    
	    void save(Ring ring){
	  
		    	String query="INSERT INTO goldpoisk_entity"+
		    			" (article,name,material,category,weight,url,proba,type,price,description,old_price,discount,count)"+
		    			" VALUES "+
		    			" (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		    	try {
					PreparedStatement preparedStmt = conn.prepareStatement(query);
					preparedStmt.setString (1, ring.article);
					preparedStmt.setString (2, ring.name);
					preparedStmt.setString (3, ring.material);
					preparedStmt.setString (4, ring.category);
					preparedStmt.setString (5, ring.weight);
					preparedStmt.setString (6, ring.url);
					preparedStmt.setString (7, ring.proba);
					preparedStmt.setString (8, ring.type);
					preparedStmt.setString (9, ring.price);
					preparedStmt.setString (10, ring.description);
					preparedStmt.setString (11, ring.old_price);
					preparedStmt.setString (12, ring.discount);
					preparedStmt.setString (13, ring.count);
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
		    	
		    	if(ring.type=="Watch"){
		    		String query_watch="INSERT INTO goldpoisk_watchdetails"+
			    			" (article,material,body_material,glass,type,mechanic)"+
			    			" VALUES "+
			    			" (?,?,?,?,?,?)";
		    		try {
						PreparedStatement preparedStmt = conn.prepareStatement(query_watch);
						preparedStmt.setString (1, ring.article);
						preparedStmt.setString (2, ring.watch_material);
						preparedStmt.setString (3, ring.watch_material_body);
						preparedStmt.setString (4, ring.watch_glass);
						preparedStmt.setString (5, ring.watch_type);
						preparedStmt.setString (6, ring.watch_mechanic);
						//preparedStmt.setBinaryStream(2, image, (int) image.available());
						preparedStmt.execute();
					} catch (SQLException e) {
						e.printStackTrace();
					}
		    	}
	    }
}
