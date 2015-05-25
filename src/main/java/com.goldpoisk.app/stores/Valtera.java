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

public class Valtera implements IStore {
    static Logger logger = LogManager.getLogger(Valtera.class.getName());
    
    static int timeout = 30000;
    
    WebClient webClient;
    BrowserVersion browserVersion = BrowserVersion.CHROME;
    
    final Ini.Section settings;
    
    private Database db;
    private final String name = "Valtera";
    
    public Valtera() throws IOException {
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
    
    String parseDescription(HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("meta", "itemprop", "description")
                         .get(0)
                         .getAttribute("content");
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseUrl(HtmlElement item) {
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
            string = item.getElementsByAttribute("meta" ,"itemprop", "name")
                         .get(0)
                         .getAttribute("content");
        } catch (Exception e) {}
        
        return string;
    }
    
    String parseArticle(HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("i", "class", "hidden-xs")
                         .get(0)
                         .getTextContent();
        } catch (Exception e) {}

        return string.equals("") ? "" : string.split("\\.")[1];
    }
    
    int parsePrice(HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("meta", "itemprop", "price")
                         .get(0)
                         .getAttribute("content");
        } catch (Exception e) {}
        
        if (string.equals(""))
            return 0;
        
        return Integer.parseInt(string);
    }
    
    int parseOldPrice(HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("p", "class", "catalog_item_old-price")
                         .get(0)
                         .getTextContent();
            Pattern p = Pattern.compile("\\D");
            Matcher m = p.matcher(string);
            string = m.replaceAll("");
        } catch (Exception e) {}
        
        if (string.equals(""))
            return 0;
        
        return Integer.parseInt(string);
    }
    
    String parseMaterial(HtmlElement item) {
        String string = "";
        
        try {
            string = item.getElementsByAttribute("div", "class", "description pull-right col-md-4 col-xs-12")
                         .get(0)
                         .getElementsByTagName("h1")
                         .get(0)
                         .asXml();
            string = string.split("<br/>")[1].trim();
        } catch (Exception e) {}
        
        return string;
    }
    
    int parseWeight(HtmlElement item) {
        String weight = "";
        int w = -1;
        
        try {
            weight = item.getElementsByAttribute("ul", "class", "stoneList list-unstyled").get(0)
                         .getElementsByTagName("li")
                         .get(1)
                         .getTextContent()
                         .split(" ")[1];
            weight = String.valueOf(weight.charAt(0));//Why is weight int?
            w = Integer.parseInt(weight);
        } catch (Exception e) { 
            w = -1;
        }
        
        return w;
    }
    
    String parseDetailedDescription(HtmlElement item) {
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
    
    ArrayList<ByteArrayOutputStream> parseImages(HtmlElement body) {
        ArrayList<ByteArrayOutputStream> images = new ArrayList<ByteArrayOutputStream>();
        
        try {
            String imageUrl = body.getElementsByAttribute("div", "class", "bigImg pull-left col-md-8 col-xs-12")
                                  .get(0).getElementsByTagName("a")
                                  .get(0)
                                  .getElementsByTagName("img")
                                  .get(0)
                                  .getAttribute("src");
            images.add(loadImage(settings.get("url") + imageUrl));
        } catch(Exception e) {}
        
        return images;
    }
    
    String getCategoryName(String category) throws Exception {
        switch (category.trim().toLowerCase()) {
            case "earrings":
                return "Серьги";
            case "rings":
                return "Кольца";
            case "pendant":
                return "Подвески";
            case "bracelets":
                return "Браслеты";
            case "chains":
                return "Цепи";
            case "necklace":
                return "Колье";
            default:
                throw new Exception("Wrong category  " + category);
        }
    }
    
    public Product parsePage(String article,
            String name,
            String url,
            String category) throws FailingHttpStatusCodeException, 
                               MalformedURLException, 
                               IOException {
        
        logger.info("*** Parsing page {}", url);
        
        Product product = new Product(this);
        
        HtmlPage page = webClient.getPage(url);
        HtmlElement body = page.getBody();
        
        product.article = article;
        product.name = name;
        product.url = url;
        product.category = category;
        
        logger.info("Setting name: {}", product.name);
        logger.info("Setting article: {}", product.article);
        logger.info("Setting url: {}", product.url);
        logger.info("Setting category: {}", product.type);
        
        product.material = parseMaterial(body);
        product.weight = parseWeight(body);
        product.description = parseDetailedDescription(body);
        product.count = -1;
        
        // Doesn't parse the lost fields if it needs just to update
        if (product.exist()) {
            return product;
        }
        
        product.images = parseImages(body);
        
        return product;
    }
    
    public void parse() {
        
        int errors = 0;
        int count = 0;
        
        String[] categories;
        String[] categoriesLink;
        
        categories = settings.get("categories").split(",");
        categoriesLink = settings.get("categoriesLink").split(",");
        
        if (!createWebClient())
            return;
        
        for (int index = 0; index < categories.length; index ++) {
            String url = String.format("%s/catalogue/%s.html", settings.get("url"), categories[index]);
            
            try {
                HtmlPage page = processWebPage(url);
                DomElement navigation = page.getFirstByXPath("//ul[@class='pagination pagination-sm']");
                DomNodeList<HtmlElement> pages = navigation.getElementsByTagName("li");
                
                int id = 1;
                int totalPages = Integer.parseInt(pages.get(pages.size()-2).getTextContent().trim());
                
                while (id <= totalPages) {
                    url = String.format("%s/catalogue/search/?type=%s&page=%d", settings.get("url"), categoriesLink[index], id);
                    page = processWebPage(url);
                    DomElement divs = page.getFirstByXPath("//div[@class='catalog col-md-9 col-sm-9 col-xs-12']");
                    DomNodeList<HtmlElement> items = divs.getElementsByTagName("div");
                    for (int i = 0; i < items.size(); i ++) {
                        if (items.get(i).getAttribute("itemprop").equals("itemListElement")) {
                            String article = parseArticle(items.get(i));
                            String itemUrl = parseUrl(items.get(i));
                            String name = parseName(items.get(i));
                            
                            int price = parsePrice(items.get(i));
                            int oldPrice = parseOldPrice(items.get(i));
                            
                            try {
                                Product product = parsePage(article, name, itemUrl, getCategoryName(categories[index]));
                                product.price = price;
                                product.oldPrice = oldPrice;
                                
                                logger.info("Setting newPrice: {}", product.price);
                                logger.info("Setting oldPrice: {}", product.oldPrice);
                                
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
                    }
                    id ++;
                }
                logger.info("{}: {}", categories[index], url);
                logger.info("Successful: {}", count);
                logger.info("Errors: {}", errors);
                logger.info("Finished");
            } catch (Exception e) {}
        }
    }
    
    private boolean createWebClient() {
        boolean result = false;
        webClient = new WebClient(browserVersion);
        
        try {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            result = true;
        } catch (Exception e) { 
            logger.error("Couldn't create WebClient: {}", e.getMessage());
        }
    
        return result;
    }
    
    private HtmlPage processWebPage(String url) {
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
