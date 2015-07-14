package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public class Adamas implements IStore {
    static Logger logger = LogManager.getLogger(Adamas.class.getName());
    static int timeout = 30000; // ms
    final Ini.Section settings;

    private Database db;
    private final String name = "Adamas";

    public Adamas () throws IOException {
        Ini ini = new Ini(new File("stores.ini"));
        settings = ini.get("Adamas");
    }

    // IStore
    public Database getDatabase() {
        return db;
    }

    public String getShopName () {
        return name;
    }

    String parseDescription (Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("itemprop", "description")
                         .get(0)
                         .attr("content");
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseUrl (Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("itemprop", "url")
                         .get(0)
                         .attr("content");
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseName(Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("id", "cat_elem_right")
                         .get(0)
                         .getElementsByTag("h1")
                         .get(0)
                         .text().trim();
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseArticle (Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("itemprop", "name")
                         .get(0)
                         .text();
        } catch (Exception e) {}

        return string;
    }
    
    int parsePrice (Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("itemprop", "price")
                         .get(0)
                         .text();
        } catch (Exception e) {}
        
        string = string.replaceAll(" ", "");
        
        if (string.equals("")) {
            try {
                string = item.getElementsByAttributeValue("class", "cat_cena")
                             .get(0)
                             .text();
            } catch (Exception e) {}
            
            string = string.replaceAll(" ", "");
        }
        
        if (string.equals(""))
            return 0;
        
        return Integer.parseInt(string);
    }
    
    int parseOldPrice (Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("class", "old-price")
                         .get(0)
                         .text();
            String[] tempString = string.split("\\s+");
            String[] floorPrice = tempString[0].split("\\.");
            string = floorPrice[0];
        } catch (Exception e) {}
        
        if (string.equals(""))
            return 0;
        
        return Integer.parseInt(string);
    }
    
    String parseMaterial (Element item) {
        String string = "";
        
        try {
            string = item.getElementsByAttributeValue("class", "tabdiv")
                         .get(0)
                         .getElementsByTag("p")
                         .get(0)
                         .getElementsByAttributeValue("class", "col999")
                         .get(0)
                         .text();
        } catch (Exception e) {}
        
        return string;
    }
    
    float parseWeight (Element item) {
        String weight = "";
        float w = -1;
        
        try {
            weight = item.getElementsByAttributeValue("class", "tabdiv")
                    .get(1)
                    .getElementsByTag("p")
                    .get(1)
                    .getElementsByAttributeValue("class", "col999")
                    .get(0)
                    .text();
            w = Float.parseFloat(weight);
        } catch (Exception e) { 
            w = -1;
        }
        
        return w;
    }
    
    String parseDetailedDescription (Element item) {
        String description = "";

        return description;
    }
    
    public ByteArrayOutputStream loadImage (String url) throws MalformedURLException,
                                                              IOException {
        //logger.info("Loading image {}", url);
        
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
    
    ArrayList<ByteArrayOutputStream> parseImages (Element body) {
        ArrayList<ByteArrayOutputStream> images = new ArrayList<ByteArrayOutputStream>();
        
        try {
            Elements imagesDom = body.getElementsByAttributeValue("class", "scroll-img")
                                                     .get(0)
                                                     .getElementsByTag("a");
            for (int i = 0; i < imagesDom.size(); i++) {
                String imageUrl = imagesDom.get(i)
                                           .getElementsByTag("img")
                                           .get(0)
                                           .attr("src");
                Pattern p = Pattern.compile("/resize/80x0x80x0x100");
                Matcher m = p.matcher(imageUrl);
                imageUrl = m.replaceAll("");
                images.add(loadImage("http://www.adamas.ru" + imageUrl));
            }
        } catch(Exception e) {}
        
        return images;
    }
    
    public Product parsePage (String article,
                             String name,
                             String url,
                             String category) throws FailingHttpStatusCodeException, 
                                                     MalformedURLException, 
                                                     IOException{
        Product product = new Product(this);
        
        Document page = Jsoup.connect(url).timeout(timeout).get();
        Element body = page.body();
        
        product.url = url;
        product.article = parseArticle(body);
        product.price = parsePrice(body);
        product.oldPrice = parseOldPrice(body);
        product.name = parseName(body);
        product.material = parseMaterial(body);
        product.weight = parseWeight(body);
        product.category = category;
        
        if (product.exist()) {
            return product;
        }
        
        ArrayList<ByteArrayOutputStream> images = parseImages(body);
        
        for (int i = 0; i < images.size(); i++)
               product.addImage(images.get(i));
        
        return product;
    }

    public void parse () throws Exception {
        db = new Database(name);
        String[] categories = settings.get("categories").split(",");

        String siteUrl = "http://www.adamas.ru";
        
        for (Iterator<String> i = Arrays.asList(categories).iterator(); i.hasNext(); ) {
            int count = 0;
            int errors = 0;
            String category = i.next();
            int pages;

            String url = String.format("%s/catalog/%s", siteUrl, category);
            /*logger.info("{}: {}", getCategoryName(id), url);
            logger.info("Started..");*/
            
            Document page = null;
            
            try {
                page = Jsoup.connect(url).timeout(timeout).get();
            } catch(Exception e) { }
            
            int id = 1;
            int tagA = 5;
            while (tagA >= 5) {
                String page_url = String.format("%s/?PAGEN_6=%d", url, id);
                page = Jsoup.connect(page_url).timeout(timeout).get();
                
                Element paginationNode = page.getElementsByClass("pager").get(0);
                Elements pageNodes = paginationNode.getElementsByTag("a");
                tagA = pageNodes.size();
                
                Element catalog = page.getElementById("catalog");
                Elements products = catalog.getElementsByTag("li");
                
                for (int index = 0; index < products.size(); index ++) {
                    String productUrl = siteUrl + products.get(index).getElementsByTag("a").get(0).attr("href");
                    Product product = parsePage("", "", productUrl, getCategoryName(category));

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
                }
                id ++;
            }
        }
    }

    // from "1 200 р." to 1200
    int parsePrice (String price) {
        Pattern p = Pattern.compile("\\D");
        Matcher m = p.matcher(price);

        price = m.replaceAll("");
        if (price == "")
            return 0;

        return Integer.parseInt(price);
    }

    int parseWeight (String weight) {
        Pattern p = Pattern.compile("\\D");
        Matcher m = p.matcher(weight);

        weight = m.replaceAll("");
        if (weight == "")
            return -1;

        return Integer.parseInt(weight);

    }

    String getCategoryName (String category) throws Exception {
        switch (category.trim().toLowerCase()) {
            case "sergi":
                return "Серьги";
            case "kolca":
                return "Кольца";
            case "sergipuseti":
                return "Серьги";
            case "sergikongo":
                return "Серьги";
            case "podveski":
                return "Подвески";
            case "brasleti":
                return "Браслеты";
            case "cepi":
                return "Цепи";
            case "kolie":
                return "Колье";
            default:
                throw new Exception("Wrong category  " + category);
        }
    }
}
