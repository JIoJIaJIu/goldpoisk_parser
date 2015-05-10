package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Integer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.ini4j.Ini;

public class Sunlight implements IStore {
    String category = "";
    final String siteName;
    final String databaseName;
    static Logger logger = LogManager.getLogger(Sunlight.class.getName());
    static int timeout = 30000; // ms
    int productid = 0;
    Ini.Section settings;

    public Sunlight() throws IOException {
        Ini ini = new Ini(new File("stores.ini"));
        settings = ini.get("Sunlight");

        siteName = settings.get("url");
        databaseName = settings.get("database");
    }

    // IStore
    public Product parsePage(String article, String name, String url) throws Exception {
        logger.info("*** Parsing page {}", url);
        Product product = new Product();

        product.article = article;
        product.name = name;
        product.url = url;
        logger.info("Setting name: {}", name);
        logger.info("Setting article: {}", article);
        logger.info("Setting url: {}", url);
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

        logger.info("Setting count: {}", count);
        logger.info("Setting newPrice: {}", newPrice);
        logger.info("Setting oldPrice: {}", oldPrice);
        product.count = String.valueOf(count);
        product.price = newPrice;
        product.old_price = oldPrice;

        Element detail = doc.getElementsByClass("detail_info").get(0);
        String[] description = detail.getElementsByClass("text").get(1).html().split("<br />");

        product.description = detail.getElementsByTag("h2").get(0).text();

        for (int i = 0; i < description.length; i++) {
            String[] parts = description[i].split(":");
            String part = parts[0].toLowerCase().trim();
            String value = parts[1].toLowerCase().trim();

            if (part.contains("тип")) {
                logger.info("Setting category: {}", value);
                product.category = value;

            } else if (part.contains("металл") &&
                      !part.contains("цвет")) {
                logger.info("Setting material: {}", value);
                product.material = value;

            } else if(part.contains("вес изделия")) {
                logger.info("Settings weight: {}", value);
                product.weight = value;

            } else if(part.contains("проба")) {
                logger.info("Settings material: {}", value);
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
        logger.info("Loading image {}", url);

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
	
	public void parse() throws Exception {
        String[] categories = settings.get("categories").split(",");

        for (Iterator<String> i = Arrays.asList(categories).iterator(); i.hasNext(); ) {
            int count = 0;
            int errors = 0;
            int id = Integer.parseInt(i.next());
            int pages;

            String url = String.format("%s/catalog/?product_type=%d", siteName, id);
            logger.info("{}: {}", getCategoryName(id), url);
            logger.info("Started..");

            try {
                Document doc = Jsoup.connect(url).timeout(timeout).get();
                Element paginationNode = doc.getElementsByClass("pagination").get(0);
                Elements pageNodes = paginationNode.getElementsByTag("a");
                pages = pageNodes.size();
            } catch(Exception e) {
                System.out.println("Ошибка подключения к сайту. Проверьте правильность ввода категории или увеличьте timeout");
                errors++;
                continue;
            }

            //Database database = new Database();
			
            int page = 0;
            while (page < pages) {
                String pageUrl = String.format("%s&page=%d", url, page);
                Document doc;
                try {
                    doc = Jsoup.connect(pageUrl).timeout(timeout).get();
                } catch (Exception e) {
                    logger.error("Couldn't open url {}: {}", pageUrl, e.getMessage());
                    errors++;
                    continue;
                }
                Element ul = doc.body().getElementById("items-list");
                Elements lis = ul.children();

                for (int j = 0; j < lis.size(); j++) {
                    Element li = lis.get(j);
                    String article = li.getElementsByTag("span").get(0).text();
                    String name = li.getElementsByTag("span").get(1).text();
                    String path = li.getElementsByTag("a").get(0).attr("href");

                    try {
                        Product product = parsePage(article, name, siteName + path);
                        //database.save(product);
                        count++;
                    } catch (Exception e) {
                        logger.error("Error IStore.parsePage: {}", e.getMessage());
                        errors++;
                    }
                }
                page++;
            }

            logger.info("{}: {}", getCategoryName(id), url);
            logger.info("Successful: {}", count);
            logger.info("Errors: {}", errors);
            logger.info("Finished");
            //Parser.ftp.saveFile(category, database.sql_query);
        }
	}

    String getCategoryName(int id) throws Exception {
        switch (id) {
            case 4:
                return "Серьги";
            case 5:
                return "Кольца";
            case 8:
                return "Подвески";
            case 317:
                return "Браслеты";
            case 319:
                return "Цепи";
            case 365:
                return "Колье";
            case 366:
                return "Брошь";
            case 318:
                return "Часы";
            case 478:
                return "Пирсинг";
            default:
                throw new Exception("Wrong category id " + id);
        }
    }
}
