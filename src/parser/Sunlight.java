package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Sunlight {
	
	static String category="";
	final String siteName="http://www.love-sl.ru";
	int productid=0;

	
	public Sunlight(){
		
	}
	
	int getProductId(String category){
		int id=0;
		category=category.toLowerCase();
		switch(category){
			case "������": id=4; break;
			case "������": id=5; break;
			case "��������": id=8; break;
			case "��������": id=317; break;
			case "����": id=319; break;
			case "�����": id=365; break;
			case "�����": id=366; break;
			case "����": id=318; break;
			case "�������": id=478; break;
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
			System.out.println("������� ��������� ������� ��� "+siteName+". ��������: ������");
			category=scanner.nextLine();
			productid=getProductId(category);
		}while(productid==0);
		
		System.out.println("������� timeout �����������. �� ��������� 30000 (30�)");
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
			
			Database database = new Database("sunlight_rings");
			
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
						}catch(Exception e){
							old_price="";
						}
						
						ring.count=String.valueOf(object_count);
						ring.price=new_price;
						ring.old_price=old_price;
						
						Element detail = object_document.getElementsByClass("detail_info").get(0);
						String[] description = detail.getElementsByClass("text").get(1).html().split("<br />");
						
						for(int k=0;k<description.length;k++){	
							if(description[k].toLowerCase().contains("���".toLowerCase()))
								ring.category=description[k];
							else if(description[k].toLowerCase().contains("������".toLowerCase()))
								ring.material=description[k];
							else if(description[k].toLowerCase().contains("��� �������".toLowerCase()))
								ring.weight=description[k];
							else if(description[k].toLowerCase().contains("�����".toLowerCase())){
								ring.proba=description[k];
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
			
			System.out.println("�������� ���������");
			System.out.println("���������: "+count);
			System.out.println("� �������: "+error_count);
			
		}catch(Exception e){
			System.out.println("������ ����������� � �����. ��������� ������������ ����� ��������� ��� ��������� timeout");
		}
		
	}
}
