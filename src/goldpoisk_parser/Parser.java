package goldpoisk_parser;

import java.io.IOException;

public class Parser {
	
	Gold585 gold585;
	Sunlight sunlight;
	Valtera valtera;
	static Ftp ftp;
	
	public Parser(){
		
		ftp=new Ftp("https://web456.webfaction.com","gpfrontend","QhCAWpR28kxoS");
		if(ftp.connect()){
			/*ftp.makeDir("sunlight");
			sunlight=new Sunlight();
			sunlight.parse();*/
			ftp.makeDir("gold585");
			gold585 = new Gold585();
			gold585.parse();
			
		}
		
	}
	
	public static void main(String[] args) throws IOException{
		Parser parser = new Parser();
	}
}
