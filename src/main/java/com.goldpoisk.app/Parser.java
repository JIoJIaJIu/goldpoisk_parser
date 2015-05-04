package goldpoisk_parser;

import java.io.*;
import java.util.Scanner;

import org.ini4j.Ini;

public class Parser {
	Gold585 gold585;
	Sunlight sunlight;
	static Ftp ftp;

	public Parser() throws FileNotFoundException, IOException {
        init();
		Scanner scanner = new Scanner(System.in);
		System.out.println("Введите ftp адрес");
		String address=scanner.nextLine();
		System.out.println("Введите ftp логин");
		String username=scanner.nextLine();
		System.out.println("Введите ftp пароль");
		String password=scanner.nextLine();

		ftp=new Ftp(address,username,password);

		if (ftp.connect()) {
			/*
			 * Выбор по сайтам в следующем коммите
			 */
			/*ftp.makeDir("sunlight");
			sunlight=new Sunlight();
			sunlight.parse();*/
			ftp.makeDir("gold585");
			gold585 = new Gold585();
			gold585.parse();
		}

	}

    void init() throws FileNotFoundException, IOException {
        File file = new File("development.ini");
        Ini ini = new Ini(new FileReader(file));
        Ini.Section pack = ini.get("package");

        String name = pack.get("name");
        String version = pack.get("version");
        System.out.println("Name: " + name);
        System.out.println("Version: " + version);
    }

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Parser parser = new Parser();
	}
}
