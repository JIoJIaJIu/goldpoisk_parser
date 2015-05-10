package goldpoisk_parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;

import org.apache.commons.codec.binary.Hex;

public class Database {
	    
		public String sql_query="";
	
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
	    
	    void clearQuery(){
	    	sql_query="";
	    }
	    
	    void update(Product product){
            String query = "UPDATE goldpoisk_entity" +
                           "SET price='%s'" +
                           "WHERE article='%s'";

            sql_query += " " + String.format(query, product.price, product.article);
	    }
	    
	    void save(Product product){
	  
		    	String query = "INSERT INTO goldpoisk_entity" +
		    			" (article,name,material,category,weight,url,proba,type,price,description,old_price,discount,count)" +
		    			" VALUES " +
		    			" ('%s','%s','%s'," +
		    			"'%s','%s','%s','%s'," +
		    			"'%s','%s','%s','%s'," +
		    			"'%s','%s'); ";
		    	
		    	query += " " + String.format(query, product.article, product.name, product.material, 
		    									product.category, product.weight, product.url, product.proba, 
		    									product.type, product.price, product.description, product.old_price, 
		    									product.discount, product.count);
		    	
		    	for(int i = 0; i < product.kamni.size(); i ++){
		    		String query_kamni = "INSERT INTO goldpoisk_kamni" +
			    			" (article,name,color,weight,size)" +
			    			" VALUES " +
			    			" ('%s','%s'," +
			    			"'%s','%s'," +
			    			"'%s'); ";
		    		query += " "+String.format(query_kamni, product.article, product.kamni.get(i),
		    				(i<product.kamniColor.size()?product.kamniColor.get(i):""), (i<product.kamniWeight.size()?product.kamniWeight.get(i):""),
		    				(i<product.kamniSize.size()?product.kamniSize.get(i):""));
		    	}
		    	
		    	for(int i = 0; i<product.images.size(); i++){
		    		ByteArrayOutputStream image = product.images.get(i);
		    		String query_kamni = "INSERT INTO goldpoisk_entity_images" +
			    			" (article,image)" +
			    			" VALUES " +
			    			" ('%s','%s'); ";
		    		query += " " + String.format(query_kamni, product.article, Hex.encodeHexString(image.toByteArray()));
		    	}
		    	
		    	if(product.type=="Watch"){
		    		String query_watch="INSERT INTO goldpoisk_watchdetails" +
			    			" (article,material,body_material,glass,type,mechanic)" +
			    			" VALUES " +
			    			" ('%s','%s','%s'," +
			    			" '%s','%s','%s'); ";
		    		query+=" "+String.format(query_watch, product.article, product.watch_material, product.watch_material_body,
		    				product.watch_glass, product.watch_type, product.watch_mechanic);
		    	}
		    	
		    	sql_query+=" "+query;		    	
	    }
}
