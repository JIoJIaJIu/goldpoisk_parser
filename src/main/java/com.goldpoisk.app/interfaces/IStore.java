package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public interface IStore {
    ByteArrayOutputStream loadImage(String url) throws MalformedURLException,
                                                       IOException;
    public Ring parsePage(String article, String name, String url) throws Exception;
}
