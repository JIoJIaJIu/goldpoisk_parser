package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Gold585 {
	
	String category="brosh";
	String siteName="http://gold585.ru/";
	
	public Gold585(){
		
	}
	
	void parse(){
		
		final String slash="/";
		int error_count=0;
		int timeout=30000;
		
		
		Scanner scanner = new Scanner(System.in);
		int count=0;
		
		
		System.out.println("Введите категорию изделий для "+siteName+". Например: rings");
		category=scanner.nextLine();
		
		System.out.println("Введите timeout подключения. По умолчанию 30000 (30с)");
		timeout=scanner.nextInt();
		
		scanner.close();
		
		String url="http://www.gold585.ru/catalog/"+category;
		
		Database database = new Database();
		
		try{
			Document doc = Jsoup.connect(url+slash).timeout(timeout).get();
	
			Element ul = doc.body().getElementById("catlist");
			Elements li = ul.getElementsByTag("li");
			
			System.out.println("Изделий найдено "+String.valueOf(li.size()));
			
			for(int i=0;i<li.size();i++){
				try{
					Element element = li.get(i);
					Element image = element.getElementsByClass("img-wrap").first();
					String href=image.attr("href");
					//System.out.println("http://www.gold585.ru"+href);
					Document elementDescription = Jsoup.connect("http://www.gold585.ru"+href).timeout(timeout).get();
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
						
						//if(!database.isExisted(article)){
							Ring ring = new Ring();
							ring.article=article;
							ring.name=name;
							ring.url="http://www.gold585.ru"+href;
							
							if(name.toLowerCase().contains("цепь".toLowerCase()))
								ring.type="chains";
							else if(name.toLowerCase().contains("браслет".toLowerCase()))
								ring.type="bracelets";
							else
								ring.type=category;
							//if(ring.type=="bracelets"){
								String description = body.getElementById("tab2").text();
								String[] price_whole = body.getElementById("nprice").text().split(":");
								String price = price_whole[1];
		
								String old_price = body.getElementById("oldprice").text();
								String discount = body.getElementById("discount").getElementsByTag("b").get(0).text();
								
								
								ring.old_price=old_price;
								ring.discount=discount;
								ring.price=price;
								ring.description=description;
								
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
									else if(parts[0].toLowerCase().contains("Проба".toLowerCase())){
										ring.proba=parts[1];
									}
									//////
									else if(parts[0].toLowerCase().contains("Материал браслета".toLowerCase())){
										ring.watch_material=parts[1];
									}
									else if(parts[0].toLowerCase().contains("Материал корпуса".toLowerCase())){
										ring.watch_material_body=parts[1];
									}
									else if(parts[0].toLowerCase().contains("Стекло".toLowerCase())){
										ring.watch_glass=parts[1];
									}
									else if(parts[0].toLowerCase().contains("Тип".toLowerCase())){
										ring.watch_type=parts[1];
									}
									else if(parts[0].toLowerCase().contains("Механизм".toLowerCase())){
										ring.watch_mechanic=parts[1];
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
							//}
						//}
					}
					catch(Exception e){
						System.out.println("ERROR");
						error_count++;
					}
				}
				catch(Exception e){
					System.out.println("BIG ERROR");
					error_count++;
				}
				count++;
				System.out.println(count);
				
			}
			
			System.out.println("Выгрузка завершена");
			System.out.println("Выгружено: "+count);
			System.out.println("С ошибкой: "+error_count);

			Parser.ftp.saveFile(category, database.sql_query);
		}catch(Exception e){
			System.out.println("Ошибка подключения к сайту. Проверьте правильность ввода категории или увеличьте timeout");
		}
		
	}
}
