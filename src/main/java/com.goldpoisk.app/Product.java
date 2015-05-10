package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Product{
	
	String article = "";
	String name = "";
	String material = "";
	String category = "";
	String weight = "";
	String url = "";
	String proba = "";
	String type = "";
	ArrayList<String> kamni = new ArrayList<String>();
	ArrayList<String> kamniColor = new ArrayList<String>();
	ArrayList<String> kamniWeight = new ArrayList<String>();
	ArrayList<String> kamniSize = new ArrayList<String>();
	ArrayList<ByteArrayOutputStream> images = new ArrayList<ByteArrayOutputStream>();
	String price = "";;
	String description = "";
	String old_price = "";
	String discount = "";
	String count = "-1";
	
	String watch_material = "";
	String watch_material_body = "";
	String watch_glass = "";
	String watch_type = "";
	String watch_mechanic = "";
	
	public Product() {}
	
	public void addKamni(String kamen) {
		kamni.add(kamen);
	}
	
	public void addKamniColor(String kamen) {
		kamniColor.add(kamen);
	}
	
	public void addKamniWeight(String kamen) {
		kamniWeight.add(kamen);
	}
	
	public void addKamniSize(String kamen) {
		kamniSize.add(kamen);
	}
	
	public void addImage(ByteArrayOutputStream image) {
		images.add(image);
	}
	
	
	
	boolean exist(String article) {
		boolean result = false;
		
		return result;
	}
	
	boolean save(Product product) {
		boolean result = false;
		Parser.sDatabase.save(product);
		return result;
	}
	
	boolean update(Product product) {
		boolean result = false;
		Parser.sDatabase.update(product);
		return result;
	}
	
}