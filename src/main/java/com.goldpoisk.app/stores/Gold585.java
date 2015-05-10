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
		
		Parser.sDatabase.clearQuery();
		
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
					Document elementDescription = Jsoup.connect("http://www.gold585.ru"+href).timeout(timeout).get();
					Element body = elementDescription.body();
					Element elementArticle=body.getElementsByClass("section-header").first();
					
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
						
							Product product = new Product();
							product.article=article;
							product.name=name;
							product.url="http://www.gold585.ru"+href;
							
							if(name.toLowerCase().contains("цепь".toLowerCase()))
								product.type="chains";
							else if(name.toLowerCase().contains("браслет".toLowerCase()))
								product.type="bracelets";
							else
								product.type=category;
							
								String description = body.getElementById("tab2").text();
								String[] price_whole = body.getElementById("nprice").text().split(":");
								String price = price_whole[1];
		
								String old_price = body.getElementById("oldprice").text();
								String discount = body.getElementById("discount").getElementsByTag("b").get(0).text();
								
								
								product.old_price=old_price;
								product.discount=discount;
								product.price=price;
								product.description=description;
								
								if(!Parser.sPostgreDB.existProduct(article)){
									Elements elementCharacter=body.getElementsByClass("characteristics");
									Elements elementCharacterLi=elementCharacter.get(0).getElementsByTag("li");
									for(int j=0;j<elementCharacterLi.size();j++){
										String text = elementCharacterLi.get(j).text();
										String[] parts = text.split(":");
										if(parts[0].toLowerCase().contains("Изделие".toLowerCase()))
											product.category=parts[1];
										else if(parts[0].toLowerCase().contains("Металл".toLowerCase()))
											product.material=parts[1];
										else if(parts[0].toLowerCase().contains("Вес изделия".toLowerCase()))
											product.weight=parts[1];
										else if(parts[0].toLowerCase().contains("Вставка".toLowerCase())){
											String [] kamni = parts[1].split(",");
											for(int z=0;z<kamni.length;z++){
												product.addKamni(kamni[z]);
											}
										}
										else if(parts[0].toLowerCase().contains("Вес камней".toLowerCase())){
											String [] kamni = parts[1].split(";");
											for(int z=0;z<kamni.length;z++){
												product.addKamniWeight(kamni[z]);
											}
										}
										else if(parts[0].toLowerCase().contains("Чистота".toLowerCase())){
											String [] kamni = parts[1].split(";");
											for(int z=0;z<kamni.length;z++){
												product.addKamniColor(kamni[z]);
											}
										}
										else if(parts[0].toLowerCase().contains("Диаметр камней".toLowerCase())){
											String [] kamni = parts[1].split(";");
											for(int z=0;z<kamni.length;z++){
												product.addKamniSize(kamni[z]);
											}
										}
										else if(parts[0].toLowerCase().contains("Проба".toLowerCase())){
											product.proba=parts[1];
										}
										else if(parts[0].toLowerCase().contains("Материал браслета".toLowerCase())){
											product.watch_material=parts[1];
										}
										else if(parts[0].toLowerCase().contains("Материал корпуса".toLowerCase())){
											product.watch_material_body=parts[1];
										}
										else if(parts[0].toLowerCase().contains("Стекло".toLowerCase())){
											product.watch_glass=parts[1];
										}
										else if(parts[0].toLowerCase().contains("Тип".toLowerCase())){
											product.watch_type=parts[1];
										}
										else if(parts[0].toLowerCase().contains("Механизм".toLowerCase())){
											product.watch_mechanic=parts[1];
										}
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
										product.addImage(baos);
									}
									
									Parser.sDatabase.save(product);
									
								} else {
									Product existedRing = Parser.sPostgreDB.getProduct(article);
									
									if (existedRing.price.trim() != "" && !existedRing.price.equals(price)) {
										Parser.sDatabase.update(product);
									}
								}

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

            Parser.sFtp.saveFile(category, Parser.sDatabase.sql_query);
		}catch(Exception e){
			System.out.println("Ошибка подключения к сайту. Проверьте правильность ввода категории или увеличьте timeout");
		}
		
	}
}
