package goldpoisk_parser;

import java.io.IOException;

public class Parser {
	
	Gold585 gold585;
	Sunlight sunlight;
	Valtera valtera;
	
	public Parser(){
		sunlight=new Sunlight();
		sunlight.parse();
	}
	
	public static void main(String[] args) throws IOException{
		Parser parser = new Parser();
	}
}
