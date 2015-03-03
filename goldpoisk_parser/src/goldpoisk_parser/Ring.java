package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class Ring {
	String article;
	String name;
	String material;
	String category;
	String weight;
	String url;
	ArrayList<String> kamni = new ArrayList<String>();
	ArrayList<String> kamniColor = new ArrayList<String>();
	ArrayList<String> kamniWeight = new ArrayList<String>();
	ArrayList<String> kamniSize= new ArrayList<String>();
	ArrayList<ByteArrayOutputStream> images= new ArrayList<ByteArrayOutputStream>();
	
	public void addKamni(String kamen){
		kamni.add(kamen);
	}
	
	public void addKamniColor(String kamen){
		kamniColor.add(kamen);
	}
	
	public void addKamniWeight(String kamen){
		kamniWeight.add(kamen);
	}
	
	public void addKamniSize(String kamen){
		kamniSize.add(kamen);
	}
	
	public void addImage(ByteArrayOutputStream image){
		images.add(image);
	}
}
