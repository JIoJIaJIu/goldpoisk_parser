package goldpoisk_parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;

import org.apache.commons.codec.binary.Hex;

public class Database {
	    
		String sql_query="";
	
	    public Database(){
	    	sql_query="";
	    	openDatabaseStructure();
	    }
	    
	    boolean isExisted(String article){
	    	String select = "SELECT * FROM goldpoisk_entity WHERE article = ?";
	    	return false;
	    }
	    
	    void openDatabaseStructure(){
            try (BufferedReader buf = new BufferedReader(new FileReader("files/goldpoisk_template.sql")) ) {
                String line = "";

                while ((line = buf.readLine()) != null) {
                     sql_query += line;
                }
            } catch (Exception e) {}
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
	    
	    void update(Ring ring){
	    	String query="UPDATE goldpoisk_entity SET price='"+ring.price+"' WHERE article='"+ring.article+"';";
	    	sql_query+=query;
	    }
	    
	    void save(Ring ring){
	  
		    	String query="INSERT INTO goldpoisk_entity"+
		    			" (article,name,material,category,weight,url,proba,type,price,description,old_price,discount,count)"+
		    			" VALUES "+
		    			" ('"+ring.article+"','"+ring.name+"','"+ring.material+"','"+ring.category+"','"+ring.weight+"',+"+
		    			"'"+ring.url+"','"+ring.proba+"','"+ring.type+"','"+ring.price+"','"+ring.description+"','"+ring.old_price+
		    			"','"+ring.discount+"','"+ring.count+"'); ";
		    	
		    	for(int i=0;i<ring.kamni.size();i++){
		    		String query_kamni="INSERT INTO goldpoisk_kamni"+
			    			" (article,name,color,weight,size)"+
			    			" VALUES "+
			    			" ('"+ring.article+"','"+ring.kamni.get(i)+"','"+(i<ring.kamniColor.size()?ring.kamniColor.get(i):"")+
			    			"','"+(i<ring.kamniWeight.size()?ring.kamniWeight.get(i):"")+"','"+(i<ring.kamniSize.size()?ring.kamniSize.get(i):"")+"'); ";
		    		query+=" "+query_kamni;
		    	}
		    	
		    	for(int i=0;i<ring.images.size();i++){
		    		ByteArrayOutputStream image = ring.images.get(i);
		    		String query_kamni="INSERT INTO goldpoisk_entity_images"+
			    			" (article,image)"+
			    			" VALUES "+
			    			" ('"+ring.article+"','"+Hex.encodeHexString(image.toByteArray())+"'); ";
		    		query+=" "+query_kamni;
		    	}
		    	
		    	if(ring.type=="Watch"){
		    		String query_watch="INSERT INTO goldpoisk_watchdetails"+
			    			" (article,material,body_material,glass,type,mechanic)"+
			    			" VALUES "+
			    			" ('"+ring.article+"','"+ring.watch_material+"','"+ring.watch_material_body+"','"+ring.watch_glass+"','"+ring.watch_type+"','"+ring.watch_mechanic+"'); ";
		    		query+=" "+query_watch;
		    	}
		    	
		    	sql_query+=" "+query;		    	
	    }
}
