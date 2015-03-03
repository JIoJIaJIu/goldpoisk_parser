package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {

	
	
	public static void main(String[] args) throws IOException{
		
		Database database = new Database();
		ArrayList<Ring> rings = new ArrayList<Ring>();
		int count=0;
		
		// TODO Auto-generated method stub
		String url="http://www.gold585.ru/catalog/rings";
		String slash="/";
		Document doc = Jsoup.connect(url+slash).timeout(30000).get();
		//System.out.println(doc.title());
		Element ul = doc.body().getElementById("catlist");
		Elements li = ul.getElementsByTag("li");
		for(int i=0;i<li.size();i++){
			Element element = li.get(i);
			Element image = element.getElementsByClass("img-wrap").first();
			String href=image.attr("href");
			//System.out.println("http://www.gold585.ru"+href);
			Document elementDescription = Jsoup.connect("http://www.gold585.ru"+href).timeout(30000).get();
			Element body = elementDescription.body();
			Element elementArticle=body.getElementsByClass("section-header").first();
			//System.out.println(elementArticle.get(0).text());
			try{
				String string=elementArticle.text();
				int index=string.indexOf("№");
				String name=string.substring(0,index);
				index+=2;
				String article="";
				while(index<string.length()){
					article+=string.charAt(index);
					index++;
				}
				
				Ring ring = new Ring();
				ring.article=article;
				ring.name=name;
				ring.url="http://www.gold585.ru"+href;
				
				Elements elementCharacter=body.getElementsByClass("characteristics");
				Elements elementCharacterLi=elementCharacter.get(0).getElementsByTag("li");
				for(int j=0;j<elementCharacterLi.size();j++){
					String text = elementCharacterLi.get(j).text();
					String[] parts = text.split(":");
					if(parts[0].toLowerCase().contains("Изделие".toLowerCase()))
						ring.category=parts[1];
					else if(parts[0].toLowerCase().contains("Металл".toLowerCase()))
						ring.material=parts[1];
					else if(parts[0].toLowerCase().contains("Вес изделия".toLowerCase()))
						ring.weight=parts[1];
					else if(parts[0].toLowerCase().contains("Вставка".toLowerCase())){
						String [] kamni = parts[1].split(",");
						for(int z=0;z<kamni.length;z++){
							ring.addKamni(kamni[z]);
						}
					}
					else if(parts[0].toLowerCase().contains("Вес камней".toLowerCase())){
						String [] kamni = parts[1].split(";");
						for(int z=0;z<kamni.length;z++){
							ring.addKamniWeight(kamni[z]);
						}
					}
					else if(parts[0].toLowerCase().contains("Чистота".toLowerCase())){
						String [] kamni = parts[1].split(";");
						for(int z=0;z<kamni.length;z++){
							ring.addKamniColor(kamni[z]);
						}
					}
					else if(parts[0].toLowerCase().contains("Диаметр камней".toLowerCase())){
						String [] kamni = parts[1].split(";");
						for(int z=0;z<kamni.length;z++){
							ring.addKamniSize(kamni[z]);
						}
					}
					
					
						//System.out.println(elementCharacterLi.get(j).text());
				}
				
				Element ring_images_block = body.getElementsByClass("ad-thumb-list").first();
				Elements ring_images = ring_images_block.getElementsByTag("img");
				
				for(int k=0;k<ring_images.size();k++){
					String image_url=ring_images.get(k).attr("src");
					URL img = new URL("http://www.gold585.ru"+image_url);
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
				}
			catch(Exception e){
				System.out.println("ERROR");
			}
			//rings.add(ring);
			count++;
			System.out.println(count);
			
		}
		
	}

}
