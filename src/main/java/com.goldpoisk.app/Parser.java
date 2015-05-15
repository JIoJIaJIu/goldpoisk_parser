package goldpoisk_parser;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.ini4j.Profile.Section;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {
    static Logger logger = LogManager.getLogger(Parser.class.getName());

    static Config config;
    //final Ftp ftp;
    static GoldpoiskDatabase goldpoiskDb;

	public Parser() throws IOException {
        config = new Config();
        Section cfg = config.get("package");
        logger.info("Parser: {}", cfg.get("name"));
        logger.info("Version: {}", cfg.get("version"));
        goldpoiskDb = new GoldpoiskDatabase();

        Sunlight sunlight = new Sunlight();
        try {
            sunlight.parse();
        } catch (Exception e) {}

        /*
        ftp = new Ftp(config.ftpUrl,config.ftpLogin,config.ftpPassword);
		if (ftp.connect()) {
			 logger.info("Successfully connect to FTP");
			ftp.makeDir("sunlight");
		}
        */

	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Parser parser = new Parser();
	}
}
