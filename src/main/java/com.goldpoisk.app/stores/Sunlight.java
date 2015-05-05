package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Sunlight implements IStore {

    String category="";
    final String siteName="http://www.love-sl.ru";
    final String databaseName="sunlight_earring";
    static Logger logger = LogManager.getLogger(Sunlight.class);
    int productid=0;

	public Sunlight() {}

    // IStore
    public void parsePage(String url) {
        logger.info("parsePage {}", url);
    }
	
	int getProductId(String category) {
		int id = 0;
		category=category.toLowerCase();

		switch(category){
			case "серьги": id=4; break;
			case "кольца": id=5; break;
			case "подвески": id=8; break;
			case "браслеты": id=317; break;
			case "цепи": id=319; break;
			case "колье": id=365; break;
			case "брошь": id=366; break;
			case "часы": id=318; break;
			case "пирсинг": id=478; break;
			default: break;
		}
		return id;
	}
	
	void parse(){
		final String slash="/";
		int error_count=0;
		int timeout=30000;
		int pageid=1;
		
		Scanner scanner = new Scanner(System.in);
		int count=0;
		
		do{
			System.out.println("Введите категорию изделий для "+siteName+". Например: Серьги");
			category=scanner.nextLine();
			productid=getProductId(category);
		}while(productid==0);
		
		System.out.println("Введите timeout подключения. По умолчанию 30000 (30с)");
		timeout=scanner.nextInt();
		
		scanner.close();
		
		String url=siteName+"/catalog/?product_type="+productid+"&page="+pageid;
		
		//Database database = new Database(category);
		String nextPageURL="";
		try{
			Document doc = Jsoup.connect(url+slash).timeout(timeout).get();
	
			Element pages=doc.getElementsByClass("pagination").get(0);
			Elements page=pages.getElementsByTag("a");
			Element lastPage = page.get(page.size()-1);
			
			String lastPageURL=lastPage.attr("href");
			
			Database database = new Database();
			
			boolean end=false;
			do{
				if(("/catalog/?product_type="+productid+"&page="+pageid).equals(lastPageURL))
					end=true;
				Element ul = doc.body().getElementById("items-list");
				Elements li = ul.children();

				for(int i=0;i<li.size();i++){
					
					Ring ring = new Ring();
					
					Element object = li.get(i);
					String article = object.getElementsByTag("span").get(0).text();
					String name = object.getElementsByTag("span").get(1).text();
					String brand = object.getElementsByTag("span").get(2).text();
					String object_url = object.getElementsByTag("a").get(0).attr("href");
					int object_count=-1;
					
					ring.article=article;
					ring.name=name;
					ring.url=siteName+object_url;
					
					try{
						Document object_document = Jsoup.connect(siteName+object_url).timeout(timeout).get();
						
						Element price = object_document.getElementsByClass("price").get(0);
						String new_price = "";
						try{
							new_price=price.getElementsByTag("span").get(0).text();
						}catch(Exception e){
							new_price="";
							object_count=0;
						}
						String old_price="";
						try{
							old_price = price.getElementsByTag("s").get(0).text();
							new_price = new_price.substring(0, new_price.length()-1);
						}catch(Exception e){
							old_price="";
						}
						
						ring.count=String.valueOf(object_count);
						ring.price=new_price;
						ring.old_price=old_price;
						
						Element detail = object_document.getElementsByClass("detail_info").get(0);
						String[] description = detail.getElementsByClass("text").get(1).html().split("<br />");
						
						ring.description=detail.getElementsByTag("h2").get(0).text();
						
						for(int k=0;k<description.length;k++){
							String[] parts=description[k].split(":");
							if(parts[0].toLowerCase().contains("тип".toLowerCase()))
								ring.category=parts[1];
							else if(parts[0].toLowerCase().contains("Металл".toLowerCase()) && !parts[0].toLowerCase().contains("цвет".toLowerCase()))
								ring.material=parts[1];
							else if(parts[0].toLowerCase().contains("Вес изделия".toLowerCase()))
								ring.weight=parts[1];
							else if(parts[0].toLowerCase().contains("Проба".toLowerCase())){
								ring.proba=parts[1];
							}
						}
						
						Element ring_images_block = object_document.getElementsByClass("smallPics").first();
						Elements ring_images = ring_images_block.getElementsByTag("img");
						
						for(int k=0;k<ring_images.size();k++){
							String image_url=ring_images.get(k).attr("data-src");
							URL img = new URL(image_url);
							InputStream imageRing = img.openStream();
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] byteChunk = new byte[4096];
							int n;
		
							while ( (n = imageRing.read(byteChunk)) > 0 ) {
								baos.write(byteChunk, 0, n);
							}
							ring.addImage(baos);
						}
						
						database.save(ring);
						System.out.println(count);
						count++;
					}
					catch(Exception e){
						System.out.println("ERROR"+e.getLocalizedMessage());
						error_count++;
					}
				}
				
				pageid++;
				doc = Jsoup.connect(siteName+"/catalog/?product_type="+productid+"&page="+pageid).timeout(timeout).get();
			}while(!end);
			
			System.out.println("Выгрузка завершена");
			System.out.println("Выгружено: "+count);
			System.out.println("С ошибкой: "+error_count);
			Parser.ftp.saveFile(category, database.sql_query);
			
		}catch(Exception e){
			System.out.println("Ошибка подключения к сайту. Проверьте правильность ввода категории или увеличьте timeout");
		}
		
	}
}
