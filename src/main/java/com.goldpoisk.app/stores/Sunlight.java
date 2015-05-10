package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Sunlight implements IStore {

    String category = "";
    final String siteName="http://www.love-sl.ru";
    final String databaseName="sunlight_earring";
    static Logger logger = LogManager.getLogger(Sunlight.class);
    static int timeout = 30000; // ms
    int productid = 0;

    public Sunlight() {}

    // IStore
    public Product parsePage(String article, String name, String url) throws Exception {
        logger.info("parsePage {}", url);
        logger.info("article: {} name: {}", article, name);
        // TODO: rename
        Product product = new Product();

        product.article = article;
        product.name = name;
        product.url = url;
        int count = -1;

        Document doc = Jsoup.connect(url).timeout(timeout).get();
        Element price = doc.getElementsByClass("price").get(0);
        String newPrice = "";
        String oldPrice="";

        try {
            newPrice = price.getElementsByTag("span").get(0).text();
        } catch (Exception e) {
            newPrice = "";
            count = 0;
        }

        try {
            oldPrice = price.getElementsByTag("s").get(0).text();
            newPrice = newPrice.substring(0, newPrice.length() - 1);
        } catch (Exception e) {
            oldPrice = "";
        }

        logger.info("Setting count: {} newPrice: {} oldPrice: {}", count, newPrice, oldPrice);
        product.count = String.valueOf(count);
        product.price = newPrice;
        product.old_price = oldPrice;

        Element detail = doc.getElementsByClass("detail_info").get(0);
        String[] description = detail.getElementsByClass("text").get(1).html().split("<br />");

        product.description = detail.getElementsByTag("h2").get(0).text();

        for (int i = 0; i < description.length; i++) {
            String[] parts = description[i].split(":");
            String part = parts[0].toLowerCase();
            String value = parts[1].toLowerCase();

            if (part.contains("тип")) {
                logger.info("Founded category: {}", value);
                product.category = value;

            } else if (part.contains("металл") &&
                      !part.contains("цвет")) {
                logger.info("Founded material: {}", value);
                product.material = value;

            } else if(part.contains("вес изделия")) {
                logger.info("Founded weight: {}", value);
                product.weight = value;

            } else if(part.contains("проба")) {
                logger.info("Founded material: {}", value);
                product.proba = value;
            }
        }

        Element imgElement = doc.getElementsByClass("smallPics").first();
        Elements images = imgElement.getElementsByTag("img");

        for (int i = 0; i < images.size(); i++) {
            String imageUrl = images.get(i).attr("data-src");
            ByteArrayOutputStream blob = loadImage(imageUrl);
            product.addImage(blob);
        }

        return product;
    }

    public ByteArrayOutputStream loadImage(String url) throws MalformedURLException,
                                                              IOException {
        logger.debug("Loading image {}", url);

        URL imageUrl = new URL(url);
        InputStream image = imageUrl.openStream();
        ByteArrayOutputStream blob = new ByteArrayOutputStream();

        byte[] byteChunk = new byte[4096];
        int n;

        while ( (n = image.read(byteChunk)) > 0 ) {
            blob.write(byteChunk, 0, n);
        }

        return blob;
    }
	
	int getProductId(String category) {
		int id = 0;
		category=category.toLowerCase();

		switch(category){
			case "серьги": id=4; break;
			case "кольца": id=5; break;
			case "подвески": id=8; break;
			case "браслеты": id=317; break;
			case "цепи": id=319; break;
			case "колье": id=365; break;
			case "брошь": id=366; break;
			case "часы": id=318; break;
			case "пирсинг": id=478; break;
			default: break;
		}
		return id;
	}
	
	void parse(){
		final String slash="/";
		int error_count=0;
		int pageid=1;
		
		Scanner scanner = new Scanner(System.in);
		int count=0;
		
		do{
			System.out.println("Введите категорию изделий для "+siteName+". Например: Серьги");
			category=scanner.nextLine();
			productid=getProductId(category);
		}while(productid==0);
		
		System.out.println("Введите timeout подключения. По умолчанию 30000 (30с)");
		timeout=scanner.nextInt();
		
		scanner.close();
		
		String url=siteName+"/catalog/?product_type="+productid+"&page="+pageid;
		
		String nextPageURL="";
		try{
			Document doc = Jsoup.connect(url+slash).timeout(timeout).get();
	
			Element pages=doc.getElementsByClass("pagination").get(0);
			Elements page=pages.getElementsByTag("a");
			Element lastPage = page.get(page.size()-1);
			
			String lastPageURL=lastPage.attr("href");
			
			Database database = new Database();
			
			boolean end=false;
			do{
				if(("/catalog/?product_type="+productid+"&page="+pageid).equals(lastPageURL))
					end=true;
				Element ul = doc.body().getElementById("items-list");
				Elements li = ul.children();

				for (int i = 0; i < li.size(); i++) {
					Element object = li.get(i);
					String article = object.getElementsByTag("span").get(0).text();
					String name = object.getElementsByTag("span").get(1).text();
					String object_url = object.getElementsByTag("a").get(0).attr("href");

                    try {
                        Product product = parsePage(article, name, siteName + object_url);
                        database.save(product);
                        System.out.println(count);
                        count++;
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage());
                        error_count++;
                    }
				}

				pageid++;
				doc = Jsoup.connect(siteName+"/catalog/?product_type="+productid+"&page="+pageid).timeout(timeout).get();
			}while(!end);
			
			System.out.println("Выгрузка завершена");
			System.out.println("Выгружено: "+count);
			System.out.println("С ошибкой: "+error_count);
			Parser.ftp.saveFile(category, database.sql_query);
			
		}catch(Exception e){
			System.out.println("Ошибка подключения к сайту. Проверьте правильность ввода категории или увеличьте timeout");
		}
		
	}
}
