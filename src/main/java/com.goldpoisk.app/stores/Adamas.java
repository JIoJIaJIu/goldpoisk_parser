package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.ini4j.Ini;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Adamas implements IStore {
    static Logger logger = LogManager.getLogger(Adamas.class.getName());
    
    static int timeout = 30000;
    
    WebClient webClient;
    BrowserVersion browserVersion = BrowserVersion.CHROME;
    
    final Ini.Section settings;
    
    private Database db;
    String name = "Adamas";
    
    public Adamas () throws IOException {
        Ini ini = new Ini(new File("stores.ini"));
        settings = ini.get("Valtera");
    }
    
    // IStore
    public Database getDatabase() {
        return db;
    }

    public String getShopName() {
        return name;
    }
    
    String parseDescription (HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("meta", "itemprop", "description")
                         .get(0)
                         .getAttribute("content");
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseUrl (HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("meta", "itemprop", "url")
                         .get(0)
                         .getAttribute("content");
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseName(HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("div" ,"id", "cat_elem_right")
                         .get(0)
                         .getElementsByTagName("h1")
                         .get(0)
                         .getTextContent().trim();
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseArticle (HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("span", "itemprop", "name")
                         .get(0)
                         .getTextContent();
        } catch (Exception e) {}

        return string;
    }
    
    int parsePrice (HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("span", "itemprop", "price")
                         .get(0)
                         .getTextContent();
        } catch (Exception e) {}
        
        string = string.replaceAll(" ", "");
        
        if (string.equals("")) {
            try {
                string = item.getElementsByAttribute("p", "class", "cat_cena")
                             .get(0)
                             .getTextContent();
            } catch (Exception e) {}
            
            string = string.replaceAll(" ", "");
        }
        
        if (string.equals(""))
            return 0;
        
        return Integer.parseInt(string);
    }
    
    int parseOldPrice (HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("span", "class", "old-price")
                         .get(0)
                         .getTextContent();
            String[] tempString = string.split("\\s+");
            String[] floorPrice = tempString[0].split("\\.");
            string = floorPrice[0];
        } catch (Exception e) {}
        
        if (string.equals(""))
            return 0;
        
        return Integer.parseInt(string);
    }
    
    String parseMaterial (HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("div", "class", "tabdiv")
                         .get(0)
                         .getElementsByTagName("p")
                         .get(0)
                         .getElementsByAttribute("span", "class", "col999")
                         .get(0)
                         .getTextContent();
        } catch (Exception e) {}
        
        return string;
    }
    
    float parseWeight (HtmlElement item) {
        String weight = "";
        float w = -1;
        
        try {
            weight = item.getElementsByAttribute("div", "class", "tabdiv")
                    .get(0)
                    .getElementsByTagName("p")
                    .get(1)
                    .getElementsByAttribute("span", "class", "col999")
                    .get(0)
                    .getTextContent();
            w = Float.parseFloat(weight);
        } catch (Exception e) { 
            w = -1;
        }
        
        return w;
    }
    
    String parseDetailedDescription (HtmlElement item) {
        String description = "";
        
        try {
            description = item.getElementsByAttribute("ul", "class", "stoneList list-unstyled")
                              .get(0)
                              .getElementsByTagName("li")
                              .get(0)
                              .getTextContent(); 
        } catch(Exception e) {}

        return description;
    }
    
    public ByteArrayOutputStream loadImage (String url) throws MalformedURLException,
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
    
    void parseImages (HtmlElement body, Product product) {
        try {
            DomNodeList<HtmlElement> imagesDom = body.getElementsByAttribute("div", "class", "scroll-img")
                                                     .get(0)
                                                     .getElementsByTagName("a")
                                                     .get(0)
                                                     .getElementsByTagName("img");
            for (int i = 0; i < imagesDom.size(); i++) {
                String imageUrl = imagesDom.get(i)
                                           .getAttribute("src");
                Pattern p = Pattern.compile("/resize/80x0x80x0x100");
                Matcher m = p.matcher(imageUrl);
                imageUrl = m.replaceAll("");
                product.addImage(loadImage(settings.get("url") + imageUrl));
            }
        } catch(Exception e) {}
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
            case "obruchalniekolca":
                return "Кольца";
            default:
                throw new Exception("Wrong category  " + category);
        }
    }
    
    public Product parsePage (String article,
            String name,
            String url,
            String category) throws FailingHttpStatusCodeException, 
                               MalformedURLException, 
                               IOException {
        
        logger.info("*** Parsing page {}", url);
        
        Product product = new Product(this);
        
        HtmlPage page = webClient.getPage(url);
        HtmlElement body = page.getBody();
        
        product.url = url;
        product.article = parseArticle(body);
        product.price = parsePrice(body);
        product.oldPrice = parseOldPrice(body);
        product.name = parseName(body);
        product.material = parseMaterial(body);
        product.weight = parseWeight(body);
        product.category = category;
        product.description = "";
        
        product.count = -1;
        
        logger.info("Setting name: {}", product.name);
        logger.info("Setting article: {}", product.article);
        logger.info("Setting url: {}", product.url);
        logger.info("Setting category: {}", product.type);
        
        // Doesn't parse the lost fields if it needs just to update
        if (product.exist()) {
            return product;
        }
        
        parseImages(body, product);
        
        return product;
    }
    
    public void parse () {
        
        int errors = 0;
        int count = 0;
        
        String[] categories = settings.get("categories").split(",");
        String siteUrl = settings.get("url");
        
        if (!createWebClient())
            return;
        
        for (int index = 0; index < categories.length; index ++) {
            String url = String.format("%s/catalog/%s", siteUrl, categories[index]);
            
            try {
                HtmlPage page = processWebPage(url);
                
                int id = 1;
                int tagA = 5;
                while (tagA >= 5) {
                    String page_url = String.format("%s/?PAGEN_6=%d", url, id);
                    page = processWebPage(page_url);
                    
                    DomElement navigation = page.getFirstByXPath("//div[@class='pager']");
                    tagA = navigation.getElementsByTagName("a").size();
                    
                    DomElement catalog = page.getElementById("catalog");
                    DomNodeList<HtmlElement> products = catalog.getElementsByTagName("li");
                    
                    for (int i = 0; i < products.size(); i ++) {
                        try {
                            String productUrl = siteUrl + products.get(i).getElementsByTagName("a").get(0).getAttribute("href");
                            Product product = parsePage("", "", productUrl, getCategoryName(categories[index]));
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
                        } catch (Exception e) {
                            logger.error("Exception IStore.parsePage: {}", e.getLocalizedMessage());
                            errors++;
                        }
                    }
                    id ++;
                }
                id = id -1;
                logger.info("{}: {}", categories[index], url);
                logger.info("Successful: {}", count);
                logger.info("Errors: {}", errors);
                logger.info("Finished");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    
    private boolean createWebClient () {
        boolean result = false;
        webClient = new WebClient(browserVersion);
        
        try {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            //webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            result = true;
        } catch (Exception e) { 
            logger.error("Couldn't create WebClient: {}", e.getMessage());
        }
    
        return result;
    }
    
    private HtmlPage processWebPage (String url) {
        WebRequest request = null;
        HtmlPage page = null;
        
        try {
            request = new WebRequest(new URL(url));
            page = webClient.getPage(request);
            webClient.getAjaxController().processSynchron(page, request, false);
        } catch (Exception e) {
            logger.error("Couldn't open url {}: {}", url, e.getMessage());
        }
        
        return page;
    }
    
}
