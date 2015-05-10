package goldpoisk_parser;

import java.io.File;

import org.ini4j.Wini;

public class IniConfigFile{
	
	String mPostgresUrl = "";
	String mPostgresDB = "";
	String mPostgresName = "";
	String mPostgresPassword = "";
	String mPostgresSchema="";
	
	String mFtpUrl = "";
	String mFtpLogin = "";
	String mFtpPassword = "";
	
	String mAppName = "";
	String mAppVersion = "";
	
	Wini mIni = null;
	File mFile = null;
	
	public IniConfigFile(String iniFile){
		try{
			mFile = new File(iniFile);
			mIni = new Wini(mFile);
		}catch(Exception e){
			Parser.logger.error("Error while open .ini file");
		}
	}
	
	void setConfigParameters(){
		try{
			mPostgresUrl = mIni.get("postgresql", "url", String.class);
			mPostgresDB = mIni.get("postgresql", "name", String.class);
			mPostgresName = mIni.get("postgresql", "user", String.class);
			mPostgresPassword = mIni.get("postgresql", "password", String.class);
			mPostgresSchema = mIni.get("postgresql", "schema", String.class);
			
			mFtpUrl = mIni.get("ftp", "url", String.class);
			mFtpLogin = mIni.get("ftp", "login", String.class);
			mFtpPassword = mIni.get("ftp", "password", String.class);
			
			mAppName = mIni.get("package", "name", String.class);
			mAppVersion = mIni.get("package", "version", String.class);
		}catch(Exception e){
			Parser.logger.error("Error while set ini parameters");
		}
		
	}	
}