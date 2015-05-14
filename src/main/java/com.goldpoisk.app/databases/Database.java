package goldpoisk_parser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.ini4j.Profile.Section;

import com.goldpoisk.parser.orm.UpdatedValue;

public class Database {
        final Logger logger;
        Session session;
        Section cfg;
	
	    public Database() throws SQLException, NoSuchAlgorithmException {
            cfg = Parser.config.get("dumps_database");
            String schemaName = generateSchemaName();
            logger = LogManager.getLogger(String.format("%s-%s", Database.class.getName(), schemaName));
            createSchema(schemaName);

            Configuration config = new Configuration();
            config.setProperty("hibernate.dialect", cfg.get("dialect"));
            config.setProperty("hibernate.connection.driver_class", cfg.get("driver"));
            config.setProperty("hibernate.connection.url", cfg.get("url"));
            config.setProperty("hibernate.connection.username", cfg.get("user"));
            config.setProperty("hibernate.connection.password", cfg.get("password"));
            config.setProperty("hibernate.connection.pool_size", "1");
            config.setProperty("hibernate.show_sql", cfg.get("show_sql"));
            config.setProperty("hibernate.default_schema", schemaName);
            logger.info("End configuration");

            config.addAnnotatedClass(Product.class);
            config.addAnnotatedClass(UpdatedValue.class);
            SchemaExport schema = new SchemaExport(config);
            schema.setHaltOnError(true);
            schema.execute(true, true, false, true);

            SessionFactory factory = config.buildSessionFactory();
            session = factory.openSession();
	    }

        public void release() {
            session.close();
        }

        void createSchema(String name) throws SQLException {
            String sql = String.format("CREATE SCHEMA %s", name);
            logger.info("Creating schema {}", sql);
            Connection connection = DriverManager.getConnection(cfg.get("url"), cfg.get("user"), cfg.get("password"));
            connection.createStatement().execute(sql);
            connection.close();
        }

        String generateSchemaName() throws NoSuchAlgorithmException {
            SimpleDateFormat format = new SimpleDateFormat("MMddyyyy");
            MessageDigest m5 = MessageDigest.getInstance("MD5");

            m5.update(UUID.randomUUID().toString().getBytes());
            byte[] digest = m5.digest();
            StringBuffer hash = new StringBuffer();
            for (byte b : digest) {
                hash.append(String.format("%02x", b & 0xff));
            }

            return String.format("d%s_%s",
                format.format(new Date()),
                hash);
        }

        public void save(Object model) {
            Transaction tx = session.beginTransaction();
            session.save(model);
            tx.commit();
        }
	    
        /*
	    boolean isExisted(String article){
	    	String select = "SELECT * FROM goldpoisk_entity WHERE article = ?";
	    	return false;
	    }
	    
	    void openDatabaseStructure() {
            try (BufferedReader buf = new BufferedReader(new FileReader("files/goldpoisk_template.sql")) ) {
                String line = "";

                while ((line = buf.readLine()) != null) {
                     sql_query += line;
                }
            } catch (Exception e) {}
	    }
	    
	    void clearQuery() {
	    	sql_query="";
	    }
	    
        void update(String article, String fieldName, String fieldValue) {
            String query = "INSERT INTO goldpoisk_update_entity " +
                "(article, field_name, field_value) " +
                "VALUES " +
                "('%s', '%s', '%s');";

            query = String.format(query, article, fieldName, fieldValue);
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
		    									product.type, product.price, product.description, product.price, 
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
        */
}
