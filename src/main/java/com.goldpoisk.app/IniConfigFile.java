package goldpoisk_parser;

import java.io.File;

import org.ini4j.Wini;

public class IniConfigFile{
	
	static String postgresql_url = "";
	static String postgresql_db = "";
	static String postgresql_user = "";
	static String postgresql_password = "";
	static String postgresql_schema="";
	
	static String ftp_url = "";
	static String ftp_login = "";
	static String ftp_password = "";
	
	static String app_name = "";
	static String app_version = "";
	
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
			postgresql_url = ini.get("postgresql","url",String.class);
			postgresql_db = ini.get("postgresql","name",String.class);
			postgresql_user = ini.get("postgresql","user",String.class);
			postgresql_password = ini.get("postgresql","password",String.class);
			postgresql_schema = ini.get("postgresql","schema",String.class);
			
			ftp_url = ini.get("ftp","url",String.class);
			ftp_login = ini.get("ftp","login",String.class);
			ftp_password = ini.get("ftp","password",String.class);
			
			app_name = ini.get("package","name",String.class);
			app_version = ini.get("package","version",String.class);
		}catch(Exception e){
			Parser.logger.error("Error while set ini parameters");
		}
		
	}	
}