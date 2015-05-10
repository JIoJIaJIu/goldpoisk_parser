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
	private String username = "";
	private String password = "";
	private String host = "";
	private int port = 21;
	private FTPClient client = null;
	
	public Ftp(String address, String username, String password)
        throws MalformedURLException {

		URL url = new URL(address);

        this.host = url.getHost();
        this.username = username;
        this.password = password;
	}
	
	boolean connect() {
		boolean status = false;
		
		this.client = new FTPClient();
		
		try {
			client.connect(host, port);
			client.enterLocalPassiveMode();
			client.login(username, password);
			client.setFileType(FTP.BINARY_FILE_TYPE);
			status = true;
		} catch (Exception e) {
			Parser.logger.error("Error while connect to ftp {}", e.getMessage());
		}
		
		return status;
	}
	
	boolean makeDir(String siteName){
		boolean status=false;
		String dirname="";
		
		try {
			 String[] names = client.listNames();
			 client.changeWorkingDirectory(names[0]);
			 names = client.listNames();
			 
			 if (!Arrays.asList(names).contains(siteName)) {
				 client.makeDirectory(siteName);
			 }
			 
			 client.changeWorkingDirectory(siteName);
			 names = client.listNames();
			 dirname="0.0.";
			 dirname += names.length;
			 client.makeDirectory(dirname);
			 client.changeWorkingDirectory(dirname);
			 status = true;
		} catch (Exception e) {
			Parser.logger.error("Ошибка! Не удалось создать каталог {} в директории {}", dirname, siteName);
		}
		
		return status;
	}
	
	boolean saveFile(String filename,final String string) {
		Parser.logger.info("Запись дампа на ftp сервер...");
		boolean status = false;
        InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));

        try {
            CopyStreamAdapter streamListener = new CopyStreamAdapter() {
                int percent = 0;
        		    
                //this method will be called everytime some bytes are transferred
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    int currentPercent = (int)(totalBytesTransferred * 100 / string.length());
                    if (currentPercent >= percent + 10) {
                        Parser.logger.debug("Процесс {}", percent);
                        percent = currentPercent;
                    }
                }
            };

            client.setCopyStreamListener(streamListener);
            client.storeFile(filename + ".sql", stream);
            Parser.logger.info("Файл успешно записан!");
            status = true;
         } catch(Exception e) {
            Parser.logger.error("Ошибка! Не удалось сохранить файл {}.sql", filename);
         }

         return status;
	}
}
