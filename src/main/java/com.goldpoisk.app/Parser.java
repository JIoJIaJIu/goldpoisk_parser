package goldpoisk_parser;

import java.io.*;
import java.util.Scanner;

import org.ini4j.Ini;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Parser {
	Gold585 gold585;
	Sunlight sunlight;
	static Ftp sFtp;
    static Logger logger = LogManager.getLogger(Parser.class.getName());

    static IniConfigFile sConfig = null;
    static CurrentDatabase sPostgreDB = null;
    static Database sDatabase = new Database();
    
    String iniFile="development.ini";
    
	public Parser() throws FileNotFoundException, IOException {
        logger.info("Constructing");

        sConfig = new IniConfigFile(iniFile);
        sConfig.setConfigParameters();
        
        sFtp = new Ftp(sConfig.mFtpUrl,sConfig.mFtpLogin,sConfig.mFtpPassword);

        sPostgreDB = new CurrentDatabase();
		
		if (sFtp.connect()) {
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
