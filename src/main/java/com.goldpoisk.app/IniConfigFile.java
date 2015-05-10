package goldpoisk_parser;

import java.io.File;

import org.ini4j.Wini;

public class IniConfigFile{
	
	String postgresUrl = "";
	String postgresDB = "";
	String postgresName = "";
	String postgresPassword = "";
	String postgresSchema="";
	
	String ftpUrl = "";
	String ftpLogin = "";
	String ftpPassword = "";
	
	String appName = "";
	String appVersion = "";
	
	Wini ini = null;
	File file = null;
	
	public IniConfigFile(String iniFile){
		try{
			file = new File(iniFile);
			ini = new Wini(file);
		}catch(Exception e){
			Parser.logger.error("Error while open .ini file");
		}
	}
	
	void setConfigParameters(){
		try{
			postgresUrl = ini.get("postgresql", "url", String.class);
			postgresDB = ini.get("postgresql", "name", String.class);
			postgresName = ini.get("postgresql", "user", String.class);
			postgresPassword = ini.get("postgresql", "password", String.class);
			postgresSchema = ini.get("postgresql", "schema", String.class);
			
			ftpUrl = ini.get("ftp", "url", String.class);
			ftpLogin = ini.get("ftp", "login", String.class);
			ftpPassword = ini.get("ftp", "password", String.class);
			
			appName = ini.get("package", "name", String.class);
			appVersion = ini.get("package", "version", String.class);
		}catch(Exception e){
			Parser.logger.error("Error while set ini parameters");
		}
		
	}	
}