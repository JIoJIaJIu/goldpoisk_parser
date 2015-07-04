package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.htmlunit.corejs.javascript.NativeArray;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Gold585 implements IStore {
    static Logger logger = LogManager.getLogger(Gold585.class.getName());
    static int timeout = 30000; // ms
    
    WebClient webClient = null;
    BrowserVersion browserVersion = BrowserVersion.CHROME;
    
    final Ini.Section settings;

    private Database db;
    private final String name = "Sunlight";
    
    public Gold585() throws IOException {
        Ini ini = new Ini(new File("stores.ini"));
        settings = ini.get("Gold585");
    }
    
 // IStore
    public Database getDatabase() {
        return db;
    }

    public String getShopName() {
        return name;
    }
    
    private boolean createWebClient(){
        boolean result = false;
        webClient = new WebClient(browserVersion);
        
        try {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            result = true;
        } catch (Exception e) { }
    
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
    
    public Product parsePage(String article,
                             String name,
                             String url,
                             String category) throws FailingHttpStatusCodeException, 
                                                MalformedURLException, 
                                                IOException{
        logger.info("*** Parsing page {}", url);
        Product product = new Product(this);
        logger.info("Started..");
        HtmlPage page = webClient.getPage(url);
        HtmlElement body = page.getBody();

        DomElement elementArticle = body.getFirstByXPath("//h1[@class='section-header']");

        try {
            product.article = parseArticle(elementArticle);
            product.name = parseName(elementArticle);
            product.url = url;
            
            if (product.name.toLowerCase().contains("цепь".toLowerCase()))
                product.type = "chains";
            else if (product.name.toLowerCase().contains("браслет".toLowerCase()))
                product.type = "bracelets";
            else
                product.type = category;
            
            logger.info("Setting name: {}", product.name);
            logger.info("Setting article: {}", product.article);
            logger.info("Setting url: {}", product.url);
            logger.info("Setting category: {}", product.type);
            
            String description = parseDescription(body);
            int price = parsePrice(body);

            int oldPrice = parseOldPrice(body);
            String discount = parseDiscount(body);
            
            
            product.oldPrice = oldPrice;
            product.discount = discount;
            product.price = price;
            product.description = description;
            product.count = 1;
            
            logger.info("Setting count: {}", product.count);
            logger.info("Setting newPrice: {}", product.price);
            logger.info("Setting oldPrice: {}", product.oldPrice);
            
            DomElement elementCharacter = body.getFirstByXPath("//div[@class='characteristics']");
            DomNodeList<HtmlElement> elementCharacterLi = elementCharacter.getElementsByTagName("li");
            for(int j=0; j<elementCharacterLi.size(); j++){
                String text = elementCharacterLi.get(j).getTextContent();
                String[] parts = text.split(":");
                if (parts[0].toLowerCase().contains("Изделие".toLowerCase()))
                    product.category = parts[1];
                else if (parts[0].toLowerCase().contains("Металл".toLowerCase()))
                    product.material = parts[1];
                else if (parts[0].toLowerCase().contains("Вес изделия".toLowerCase()))
                    product.weight = parseWeight(parts[1]);
                /*else if (parts[0].toLowerCase().contains("Вставка".toLowerCase())) {
                    String [] kamni = parts[1].split(",");
                    for(int z=0; z<kamni.length; z++){
                        product.addKamni(kamni[z]);
                    }
                }
                else if (parts[0].toLowerCase().contains("Вес камней".toLowerCase())) {
                    String [] kamni = parts[1].split(";");
                    for(int z=0; z<kamni.length; z++){
                        product.addKamniWeight(kamni[z]);
                    }
                }
                else if (parts[0].toLowerCase().contains("Чистота".toLowerCase())) {
                    String [] kamni = parts[1].split(";");
                    for(int z=0; z<kamni.length; z++){
                        product.addKamniColor(kamni[z]);
                    }
                }
                else if (parts[0].toLowerCase().contains("Диаметр камней".toLowerCase())) {
                    String [] kamni = parts[1].split(";");
                    for(int z=0; z<kamni.length; z++){
                        product.addKamniSize(kamni[z]);
                    }
                }*/
                else if (parts[0].toLowerCase().contains("Проба".toLowerCase())) {
                    product.proba = Integer.parseInt(parts[1]);
                }
                //////
                /*else if (parts[0].toLowerCase().contains("Материал браслета".toLowerCase())) {
                    product.watch_material = parts[1];
                }
                else if (parts[0].toLowerCase().contains("Материал корпуса".toLowerCase())) {
                    product.watch_material_body = parts[1];
                }
                else if (parts[0].toLowerCase().contains("Стекло".toLowerCase())) {
                    product.watch_glass = parts[1];
                }
                else if (parts[0].toLowerCase().contains("Тип".toLowerCase())) {
                    product.watch_type = parts[1];
                }
                else if (parts[0].toLowerCase().contains("Механизм".toLowerCase())) {
                    product.watch_mechanic = parts[1];
                }*/
            }
            
            // Doesn't parse the lost fields if it needs just to update
            if (product.exist()) {
                return product;
            }
            
            DomElement ring_images_block = body.getFirstByXPath("//ul[@class='ad-thumb-list']");
            DomNodeList<HtmlElement> ring_images = ring_images_block.getElementsByTagName("img");
            
            for(int k=0; k<ring_images.size(); k++){
                String image_url=ring_images.get(k).getAttribute("src");
                product.addImage(loadImage(settings.get("url")+image_url));
            }
        } catch(Exception e) {
        }
        
        return product;
    }
    
    String parseDescription(HtmlElement body){
        String description = "";
        
        try { 
            description = ((HtmlElement)body.getFirstByXPath("//div[@id='tab2']")).getTextContent();
        } catch (Exception e) { 
            description = "";
        }
        
        return description;
    }
    
    // from "1 200 р." to 1200
    int parsePrice(HtmlElement body) {
        String price = "";
        
        try {
            String[] price_whole = ((HtmlElement)body.getFirstByXPath("//div[@id='nprice']")).getTextContent().split(":");
            Pattern p = Pattern.compile("\\D");
            Matcher m = p.matcher(price_whole[1]);

            price = m.replaceAll("");
        } catch(Exception e) {
            price = "";
        }
        
        if (price == "")
            return 0;

        return Integer.parseInt(price);
    }
    
    // from "1 200 р." to 1200
    int parseOldPrice(HtmlElement body) {
        String oldPrice = "";
        
        try {
            oldPrice = ((HtmlElement)body.getFirstByXPath("//span[@id='oldprice']")).getTextContent();
            Pattern p = Pattern.compile("\\D");
            Matcher m = p.matcher(oldPrice);

            oldPrice = m.replaceAll("");
        } catch(Exception e) {
            oldPrice = "";
        }
        
        if (oldPrice == "")
            return 0;

        return Integer.parseInt(oldPrice);
    }
    
    String parseDiscount(HtmlElement body) {
        String discount = "";
        
        try {
            discount = ((HtmlElement)body.getFirstByXPath("//div[@id='discount']")).getTextContent();
        } catch (Exception e) {
            discount = "";
        }
        
        return discount;
    }
    
    String parseName(DomElement elementArticle) {
        String string = elementArticle.getTextContent();
        int index = string.indexOf("№");
        String name = string.substring(0,index);
        return name;
    }
    
    Float parseWeight(String weight) {
        Pattern p = Pattern.compile("\\D");
        Matcher m = p.matcher(weight);

        weight = m.replaceAll("");
        if (weight == "")
            return new Float(-1);

        return Float.parseFloat(weight);
    }
    
    String parseArticle(DomElement elementArticle) {
        String article = "";
        String string = elementArticle.getTextContent();
        int index = string.indexOf("№");
        String name = string.substring(0,index);
        index += 2;
        while (index < string.length()) {
            article += string.charAt(index);
            index++;
        }
        return article;
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
        
        int errors = 0;
        int count = 0;
        
        for (int index = 0; index < categories.length; index++) {
            String url = String.format(settings.get("url") + "/catalog/%s", categories[index]);
            try {
                if (createWebClient()) {
                    HtmlPage page = processWebPage(url);
                    ScriptResult result = page.executeJavaScript("goods");
                    webClient.waitForBackgroundJavaScript(timeout);
                    NativeArray array = (NativeArray)result.getJavaScriptResult();
                    
                    for(int i=0; i<array.size(); i++){
                        NativeObject object = (NativeObject)array.get(i,array);
                        try {
                            Product product = parsePage("","",settings.get("url") + String.valueOf(object.get("LINK",object)), categories[index]);
                            
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
                    logger.info("{}: {}", categories[index], url);
                    logger.info("Successful: {}", count);
                    logger.info("Errors: {}", errors);
                    logger.info("Finished");
                }
            } catch(Exception e) {}
        }
    }
}
