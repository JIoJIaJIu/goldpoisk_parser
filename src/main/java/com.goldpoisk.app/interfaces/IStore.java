package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public interface IStore {
    Database getDatabase ();
    String getShopName();
    ByteArrayOutputStream loadImage(String url) throws MalformedURLException,
                                                       IOException;
                                                       /*
    public Product parsePage(String url, String category) throws MalformedURLException,
                                                                 IOException;
                                                                 */
    public Product parsePage(String article,
                             String name,
                             String url,
                             String category) throws MalformedURLException,
                                                     IOException;
    public void parse() throws Exception;
}
