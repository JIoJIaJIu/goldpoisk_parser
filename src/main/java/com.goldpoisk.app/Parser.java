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

    static IniConfigFile config = null;
    static CurrentDatabase postgreDB = null;
    static Database database = new Database();
    
    String iniFile="development.ini";
    
	public Parser() throws FileNotFoundException, IOException {
        logger.info("Constructing");

        config = new IniConfigFile(iniFile);
        config.setConfigParameters();
        
        ftp = new Ftp(config.ftpUrl,config.ftpLogin,config.ftpPassword);

        postgreDB = new CurrentDatabase();
		
		if (ftp.connect()) {
			 logger.info("Successfully connect to FTP");
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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Parser parser = new Parser();
	}
}
