package goldpoisk_parser;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.ini4j.Profile.Section;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {
    static Logger logger = LogManager.getLogger(Parser.class.getName());

    static Config config;
    static GoldpoiskDatabase goldpoiskDb;

	public Parser(String configName) throws IOException {
        config = new Config(configName);
        Section cfg = config.get("package");
        logger.info("Parser: {}", cfg.get("name"));
        logger.info("Version: {}", cfg.get("version"));
        goldpoiskDb = new GoldpoiskDatabase();
        
        Gold585 gold585 = new Gold585();
        try {
            gold585.parse();
        } catch (Exception e) {}
        
        /*Sunlight sunlight = new Sunlight();
        try {
            sunlight.parse();
        } catch (Exception e) {}*/
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Parser parser = new Parser(args[0]);
	}
}
