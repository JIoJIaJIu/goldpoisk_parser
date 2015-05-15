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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.commons.lang3.text.WordUtils;

import org.ini4j.Ini;

public class Sunlight implements IStore {
    static Logger logger = LogManager.getLogger(Sunlight.class.getName());
    static int timeout = 30000; // ms

    final Ini.Section settings;

    private Database db;
    private final String name = "Sunlight";

    public Sunlight() throws IOException {
        Ini ini = new Ini(new File("stores.ini"));
        settings = ini.get("Sunlight");
    }

    // IStore
    public Database getDatabase() {
        return db;
    }

    public String getShopName() {
        return name;
    }

    public Product parsePage(String article,
                             String name,
                             String url,
                             String category) throws MalformedURLException,
                                                     IOException {
        logger.info("*** Parsing page {}", url);
        Product product = new Product(this);

        product.article = article;
        product.name = name;
        product.url = url;
        product.category = category;
        logger.info("Setting name: {}", name);
        logger.info("Setting article: {}", article);
        logger.info("Setting url: {}", url);
        logger.info("Setting category: {}", category);
        int count = -1;

        Document doc = Jsoup.connect(url).timeout(timeout).get();
        Element price = doc.getElementsByClass("price").get(0);
        String newPriceStr = "";
        String oldPriceStr = "";

        try {
            newPriceStr = price.getElementsByTag("span").get(0).text();
            count = 1;
        } catch (Exception e) {
            newPriceStr = "";
            count = 0;
        }

        try {
            oldPriceStr = price.getElementsByTag("s").get(0).text();
            newPriceStr = newPriceStr.substring(0, newPriceStr.length() - 1);
        } catch (Exception e) {
            oldPriceStr = "";
        }

        int newPrice = parsePrice(newPriceStr);
        int oldPrice = parsePrice(oldPriceStr);

        logger.info("Setting count: {}", count);
        logger.info("Setting newPrice: {}", newPrice);
        logger.info("Setting oldPrice: {}", oldPrice);
        product.count = count;
        product.price = newPrice;
        product.oldPrice = oldPrice;

        // parse description
        Element detail = doc.getElementsByClass("detail_info").get(0);
        String[] description = detail.getElementsByClass("text").get(1).html().split("<br />");

        product.description = detail.getElementsByTag("h2").get(0).text();

        for (int i = 0; i < description.length; i++) {
            String[] parts = description[i].split(":");
            String part = parts[0].toLowerCase().trim();
            String value = parts[1].toLowerCase().trim();

            if (part.contains("металл") &&
                !part.contains("цвет")) {
                value = WordUtils.capitalize(value);
                product.material = value;
                logger.info("Setting material: {}", value);

            } else if (part.contains("вес изделия")) {
                int weight = parseWeight(value);
                logger.info("Settings weight: {}", weight);
                product.weight = weight;

            } else if (part.contains("проба")) {
                int proba = Integer.parseInt(value);
                logger.info("Settings material: {}", proba);
                product.proba = proba;
            }
        }

        // Doesn't parse the lost fields if it needs just to update
        if (product.exist()) {
            return product;
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
        db = new Database(name);
        String[] categories = settings.get("categories").split(",");

        for (Iterator<String> i = Arrays.asList(categories).iterator(); i.hasNext(); ) {
            int count = 0;
            int errors = 0;
            int id = Integer.parseInt(i.next());
            int pages;

            String url = String.format("%s/catalog/?product_type=%d", settings.get("url"), id);
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
                        Product product = parsePage(article, name, settings.get("url") + path, getCategoryName(id));
                        if (product.exist()) {
                            boolean updated = product.update(); 
                            if (updated) {
                                logger.info("Update existed product");
                            } else {
                                logger.info("Skip existed product");
                            }
                        } else {
                            logger.info("New product");
                            product.save();
                        }
                        count++;
                    } catch (Exception e) {
                        logger.error("Exception IStore.parsePage: {}", e.getLocalizedMessage());
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
        db.release();
	}

    // from "1 200 р." to 1200
    int parsePrice(String price) {
        Pattern p = Pattern.compile("\\D");
        Matcher m = p.matcher(price);

        price = m.replaceAll("");
        if (price == "")
            return 0;

        return Integer.parseInt(price);
    }

    int parseWeight(String weight) {
        Pattern p = Pattern.compile("\\D");
        Matcher m = p.matcher(weight);

        weight = m.replaceAll("");
        if (weight == "")
            return -1;

        return Integer.parseInt(weight);

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
