package goldpoisk_parser;

import java.io.IOException;
import java.util.Scanner;

public class Parser {
	
	Gold585 gold585;
	Sunlight sunlight;
	Valtera valtera;
	static Ftp ftp;
	
	public Parser(){
		
		/*
		 *  Эту грязь переделаю в следующем коммите=)
		 */
		Scanner scanner = new Scanner(System.in);
		System.out.println("Введите ftp адрес");
		String address=scanner.nextLine();
		System.out.println("Введите ftp логин");
		String username=scanner.nextLine();
		System.out.println("Введите ftp пароль");
		String password=scanner.nextLine();
		
		ftp=new Ftp(address,username,password);
		
		if(ftp.connect()){
			/*
			 * Выбор по сайтам в следующем коммите
			 */
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
