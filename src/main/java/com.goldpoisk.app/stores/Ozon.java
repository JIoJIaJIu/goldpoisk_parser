package goldpoisk_parser;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONObject;

public class Ozon implements IStore {
    static Logger logger = LogManager.getLogger(Ozon.class.getName());
    static int timeout = 30000; // ms
    final Ini.Section settings;

    private Database db;
    private final String name = "Ozon";

    JSONArray images = null;
    
    public Ozon () throws IOException {
        Ini ini = new Ini(new File("stores.ini"));
        settings = ini.get("Ozon");
    }

    //IStore
    public Database getDatabase() {
        return db;
    }

    public String getShopName () {
        return name;
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

    public Product parsePage(String article,
                            String name,
                            String url,
                            String category) throws MalformedURLException,
                                                    IOException {
        Product product = new Product(this);
        product.article = article;
        product.name = name;
        product.url = url;
        if (product.name.toLowerCase().contains("цепь"))
            category = "10101";
        if (product.name.toLowerCase().contains("колье"))
            category = "10100";
        
        try {
            product.category = getCategoryName(Integer.parseInt(category));
        } catch (Exception e) {
            
        }
        
        // Doesn't parse the lost fields if it needs just to update
        if (product.exist()) {
           return product;
        }    
        
        for (int j = 0; j < images.length(); j++)
            product.addImage(loadImage(images.getString(j)));
        
        return product;
    }
    
    public void parse () throws Exception {
        
        int errors = 0;
        int goods = 0;
        
        db = new Database(name);
        String[] categories = settings.get("categories").split(",");
        
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
        
        for (int index = 0, category = Integer.parseInt(categories[index]); index < categories.length; index++) {
            
            int count = 0;
            int PRODUCT_PER_REQUEST = 28;
            boolean changeCategory = false;
            while (true) {
                try {
                    HttpPost request = new HttpPost("http://www.ozon.ru/json/tiles.asmx/gettiles");
                    
                    request.addHeader("Host", "www.ozon.ru");
                    request.addHeader("Connection", "keep-alive");
                    request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
                    request.addHeader("Origin", "http://www.ozon.ru");
                    request.addHeader("X-Requested-With", "XMLHttpRequest");
                    request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
                    request.addHeader("Content-Type", "application/json");
                    request.addHeader("Referer", "http://www.ozon.ru/catalog/1159227/?decortype=43010");
                    request.addHeader("Accept-Encoding", "gzip, deflate");
                    request.addHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
                   
                    CookieStore cookieStore = new BasicCookieStore(); 
                    DefaultHttpClient client = new DefaultHttpClient();
        
                    String requestString = String.format("{\"context\":\"catalog\", " +
                                                          "\"facetId\":67, " +
                                                          "\"facetParams\":\"catalog=%d&decortype=43010\", " +
                                                           "\"searchText\":\"\", " +
                                                           "\"limit\":%d, " +
                                                           "\"offset\":%d}", category, PRODUCT_PER_REQUEST, count);
                    
                    StringEntity params = new StringEntity(requestString);
                    params.setContentType("application/json");
                    request.setEntity(params);
                    
                    JSONArray products = null;
                    try {
                        HttpResponse response = client.execute(request);
                        
                        int code = response.getStatusLine().getStatusCode();
                        
                        String jsonString = EntityUtils.toString(response.getEntity(), "UTF-8");
                        
                        JSONObject object = new JSONObject(jsonString);
                        
                        JSONObject json = object.getJSONObject("d");
                        
                        products = json.getJSONArray("Tiles");
                    } catch (Exception e) {
                        break;
                    }
                    
                    for (int i = 0; products != null && i < products.length(); i++) {
                        JSONObject productJSON = products.getJSONObject(i);
                        
                        JSONObject imagesJSON = productJSON.getJSONArray("Images").getJSONObject(0);
                        images = imagesJSON.getJSONArray("ImagesUrls");
                        
                        JSONObject saleInfo = productJSON.getJSONObject("SaleBlock");
                        
                        String article = String.valueOf(productJSON.getInt("ID"));
                        int price =  saleInfo.getInt("Price");
                        String name = productJSON.getString("Name");
                        String url = imagesJSON.getString("Href");
                        
                        try {
                            Product product = parsePage(article, name, url, String.valueOf(category));
                            product.price = price;
                            
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
                        } catch (Exception e) {
                            logger.error("Exception IStore.parsePage: {}", e.getLocalizedMessage());
                            errors++;
                        }
                        
                        count += PRODUCT_PER_REQUEST;
                        
                    }
                    logger.info("{}", categories[index]);
                    logger.info("Successful: {}", count);
                    goods += count;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        
        logger.info("Successful: {}", goods);
        logger.info("Errors: {}", errors);
        logger.info("Finished");
    }

    String getCategoryName (int id) throws Exception {
        switch (id) {
            case 1159227:
                return "Серьги";
            case 1159226:
                return "Кольца";
            case 1159224:
                return "Подвески";
            case 1159228:
                return "Браслеты";
            case 10101:
                return "Цепи";
            case 10100:
                return "Колье";
            case 1159225:
                return "Брошь";
            case 318:
                return "Часы";
            case 1159220:
                return "Пирсинг";
            default:
                throw new Exception("Wrong category id " + id);
        }
    }
}
