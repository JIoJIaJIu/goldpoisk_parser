package goldpoisk_parser;

public class Product{
	
	Database database;
	
	public Product(){
		database = new Database();
	}
	
	boolean exist(){
		boolean result=false;
		return result;
	}
	
	boolean save(){
		boolean result=false;
		return result;
	}
	
	boolean save(Ring ring){
		boolean result=false;
		database.save(ring);
		return result;
	}
	
	boolean update(Ring ring){
		boolean result=false;
		//database.update(ring);
		return result;
	}
	
}