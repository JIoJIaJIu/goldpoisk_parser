package goldpoisk_parser;

import java.io.*;
import java.util.Scanner;

import org.ini4j.Ini;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Parser {
	Gold585 gold585;
	Sunlight sunlight;
	static Ftp ftp;
    static Logger logger = LogManager.getLogger(Parser.class.getName());

    static IniConfigFile config=null;
    
    String iniFile="development.ini";
    
	public Parser() throws FileNotFoundException, IOException {
        logger.info("Constructing");

        config = new IniConfigFile(iniFile);
        config.setConfigParameters();
        
        //init();
		/*Scanner scanner = new Scanner(System.in);
		System.out.println("Введите ftp адрес");
		String address=scanner.nextLine();
		System.out.println("Введите ftp логин");
		String username=scanner.nextLine();
		System.out.println("Введите ftp пароль");
		String password=scanner.nextLine();*/
        System.out.println(config.ftp_url+" "+config.ftp_login+" "+config.ftp_password);
		ftp=new Ftp(config.ftp_url,config.ftp_login,config.ftp_password);

		if (ftp.connect()) {
			 logger.info("Successfully connect to FTP");
			 System.out.println("Successfully connect to FTP");
			/*
			 * Выбор по сайтам в следующем коммите
			 */
			/*ftp.makeDir("sunlight");
			sunlight=new Sunlight();
			sunlight.parse();*/
			/*ftp.makeDir("gold585");
			gold585 = new Gold585();
			gold585.parse();*/
		}

	}

    /*void init() throws FileNotFoundException, IOException {
        File file = new File("development.ini");
        Ini ini = new Ini(new FileReader(file));
        Ini.Section pack = ini.get("package");

        String name = pack.get("name");
        String version = pack.get("version");
        System.out.println("Name: " + name);
        System.out.println("Version: " + version);
    }*/

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Parser parser = new Parser();
	}
}
