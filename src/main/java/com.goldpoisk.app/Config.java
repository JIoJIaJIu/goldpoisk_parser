package goldpoisk_parser;

import java.io.File;
import java.io.IOException;

import org.ini4j.Wini;
import org.ini4j.Profile.Section;

public class Config {
	Wini ini = null;
	
	public Config(String configName) throws IOException {
        File file = new File(configName);
        ini = new Wini(file);
	}

    Section get(String key) {
        return ini.get(key);
    }
}
