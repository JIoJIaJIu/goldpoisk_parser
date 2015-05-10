package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public interface IStore {
    ByteArrayOutputStream loadImage(String url) throws MalformedURLException,
                                                       IOException;
    public Product parsePage(String article, String name, String url) throws Exception;
    public void parse() throws Exception;
}
