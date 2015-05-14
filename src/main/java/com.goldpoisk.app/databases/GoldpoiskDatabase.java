package goldpoisk_parser;

import java.io.IOException;
import java.lang.Boolean;

import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.ini4j.Profile.Section;

import com.goldpoisk.parser.orm.Item;
import com.goldpoisk.parser.orm.Product;
import com.goldpoisk.parser.orm.Shop;

public class GoldpoiskDatabase {
    Session session;

    public GoldpoiskDatabase() throws IOException {
        Section cfg = Parser.config.get("goldpoisk_database");

        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", cfg.get("dialect"));
        config.setProperty("hibernate.connection.driver_class", cfg.get("driver"));
        config.setProperty("hibernate.connection.url", cfg.get("url"));
        config.setProperty("hibernate.connection.username", cfg.get("user"));
        config.setProperty("hibernate.connection.password", cfg.get("password"));
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.show_sql", cfg.get("show_sql"));

        config.addAnnotatedClass(Item.class);
        config.addAnnotatedClass(Product.class);
        config.addAnnotatedClass(Shop.class);

        SessionFactory factory = config.buildSessionFactory();
        session = factory.openSession();
    }

    public void release() {
        session.close();
    }

    public boolean existProduct(String article, String shopName) {
        if (getProduct(article, shopName) == null)
            return false;

        return true;
    }

    public Item getProduct(String article, String shopName) {
        return (Item)session.createCriteria(Item.class)
                .createAlias("product", "product")
                .createAlias("shop", "shop")
                .add(Restrictions.eq("product.article", article))
                .add(Restrictions.eq("shop.name", shopName))
                .uniqueResult();
    }
}
