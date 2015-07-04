package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.persistence.*;

import com.goldpoisk.parser.orm.Item;
import com.goldpoisk.parser.orm.UpdatedValue;

@Entity
@Table(name="goldpoisk_entity")
public class Product {
    @Id @GeneratedValue
    Integer id;
	String article;
	String name;
	String material;
	String category;
	String url;
	String type;
	int proba;
	int price;
	int oldPrice;
	int weight;
	String description;
	String discount;
	int count = -1;

    @Transient
    private String shopName;
    @Transient
	private ArrayList<ByteArrayOutputStream> images = new ArrayList<ByteArrayOutputStream>();
    @Transient
    private ArrayList<Gem> gems = new ArrayList<Gem>();

    @Transient
    private final Database db;
    private static GoldpoiskDatabase goldpoiskDb = Parser.goldpoiskDb;

    public Product(IStore shop) {
        this.shopName = shop.getShopName();
        db = shop.getDatabase();
    }

    public void addGem();
    /*
	public void addKamni(String kamen) {
		kamni.add(kamen);
	}
	
	public void addKamniColor(String kamen) {
		kamniColor.add(kamen);
	}
	
	public void addKamniWeight(String kamen) {
		kamniWeight.add(kamen);
	}
	
	public void addKamniSize(String kamen) {
		kamniSize.add(kamen);
	}
    */
	
	public void addImage(ByteArrayOutputStream image) {
		images.add(image);
	}

    public boolean exist() {
        return this.goldpoiskDb.existProduct(article, shopName);
    }

    public boolean update() {
        Item model = this.goldpoiskDb.getProduct(article, shopName);
        boolean isUpdated = false;
        if (model == null) {
            return isUpdated;
        }

        if (model.price != price) {
            UpdatedValue updateModel = new UpdatedValue(article, "price", String.valueOf(price), category);
            db.save(updateModel);
            isUpdated = true;
        }

        if (model.count != count) {
            UpdatedValue updateModel = new UpdatedValue(article, "count", String.valueOf(count), category);
            db.save(updateModel);
            isUpdated = true;
        }

        return isUpdated;
    }

    public void save() {
        db.save(this);
    }
}
