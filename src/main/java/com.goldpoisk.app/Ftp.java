package goldpoisk_parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;
public class Ftp {
	
	private String username="";
	private String password="";
	private String host="";
	private int port=21;
	
	private FTPClient client=null;
	
	public Ftp(String address, String username, String password){
		
		URL url=null;
		
		try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
		
		this.host = url != null ? url.getHost() : null;
		this.username=username;
		this.password=password;
		
	}
	
	boolean connect(){
		boolean status=false;
		
		this.client = new FTPClient();
		
		try {
			client.connect(host, port);
			client.enterLocalPassiveMode();
			client.login(username, password);
			client.setFileType(FTP.BINARY_FILE_TYPE);
			status=true;
		} catch (Exception e) {
			System.out.println("Ошибка подключения к ftp серверу");
		}
		
		return status;
	}
	
	boolean makeDir(String siteName){
		boolean status=false;
		String dirname="";
		
		try{
			 String[] names = client.listNames();
			 client.changeWorkingDirectory(names[0]);
			 names=client.listNames();
			 
			 if(!Arrays.asList(names).contains(siteName)){
				 client.makeDirectory(siteName);
			 }
			 
			 client.changeWorkingDirectory(siteName);
			 names = client.listNames();
			 dirname="0.0.";
			 dirname+=names.length;
			 client.makeDirectory(dirname);
			 client.changeWorkingDirectory(dirname);
			 status=true;
		}catch(Exception e){
			System.out.println("Ошибка! Не удалось создать каталог "+dirname+" в директории "+siteName);
		}
		
		return status;
	}
	
	boolean saveFile(String filename,final String string){
		
		boolean status=false;
		System.out.println("Запись дампа на ftp сервер...");
         InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
         try{
        	
        	 CopyStreamAdapter streamListener = new CopyStreamAdapter() {

        		 int percent=0;
        		    
        		    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        		       //this method will be called everytime some bytes are transferred

        		      int current_percent = (int)(totalBytesTransferred*100/string.length());
        		      if(current_percent>=percent+10){
        		    	  System.out.println("Процесс "+percent);
        		    	  percent=current_percent;
        		      }
        		    }

        		 };
    		 client.setCopyStreamListener(streamListener);
    		 client.storeFile(filename+".sql", stream);
        	 System.out.println("Файл успешно записан!");
        	 status=true;
         }catch(Exception e){
        	 System.out.println("Ошибка! Не удалось сохранить файл "+filename+".sql");
         }
         
         
         
         return status;
	}
}
