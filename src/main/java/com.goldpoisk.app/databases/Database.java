package goldpoisk_parser;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.ini4j.Profile.Section;

import com.goldpoisk.parser.orm.Info;
import com.goldpoisk.parser.orm.Image;
import com.goldpoisk.parser.orm.UpdatedValue;

public class Database {
    final Logger logger;
    Session session;
    Section cfg;
    String name;

    Database(String name) throws SQLException, NoSuchAlgorithmException {
        this.name = name;
        cfg = Parser.config.get("dumps_database");
        String schemaName = generateSchemaName();
        logger = LogManager.getLogger(String.format("%s-%s", Database.class.getName(), schemaName));
        createSchema(schemaName);

        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", cfg.get("dialect"));
        config.setProperty("hibernate.connection.driver_class", cfg.get("driver"));
        config.setProperty("hibernate.connection.url", cfg.get("url"));
        config.setProperty("hibernate.connection.username", cfg.get("user"));
        config.setProperty("hibernate.connection.password", cfg.get("password"));
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.show_sql", cfg.get("show_sql"));
        config.setProperty("hibernate.default_schema", schemaName);
        logger.info("End configuration");
        addInfo(schemaName);

        config.addAnnotatedClass(Image.class);
        config.addAnnotatedClass(Product.class);
        config.addAnnotatedClass(UpdatedValue.class);
        SchemaExport schema = new SchemaExport(config);
        schema.setHaltOnError(true);
        schema.execute(true, true, false, true);

        SessionFactory factory = config.buildSessionFactory();
        session = factory.openSession();
    }

    void release() {
        session.close();
    }

    void save(Product product) {
        Transaction tx = session.beginTransaction();
        session.save(product);

        for (ByteArrayOutputStream stream: product.images) {
            Image image = new Image(product, stream.toByteArray());
            session.save(image);
        }
        tx.commit();
    }

     void save(Object model) {
        Transaction tx = session.beginTransaction();
        session.save(model);
        tx.commit();
    }

    private void addInfo(String schemaName) {
        Configuration config = new Configuration();
        config.setProperty("hibernate.dialect", cfg.get("dialect"));
        config.setProperty("hibernate.connection.driver_class", cfg.get("driver"));
        config.setProperty("hibernate.connection.url", cfg.get("url"));
        config.setProperty("hibernate.connection.username", cfg.get("user"));
        config.setProperty("hibernate.connection.password", cfg.get("password"));
        config.setProperty("hibernate.connection.pool_size", "1");
        config.setProperty("hibernate.show_sql", cfg.get("show_sql"));

        config.addAnnotatedClass(Info.class);
        SchemaExport schema = new SchemaExport(config);
        schema.execute(true, true, false, true);

        SessionFactory factory = config.buildSessionFactory();
        session = factory.openSession();

        Info info = new Info(name, schemaName);
        Transaction tx = session.beginTransaction();
        session.save(info);
        tx.commit();

        session.close();
        factory.close();
    }

    private void createSchema(String name) throws SQLException {
        String sql = String.format("CREATE SCHEMA %s", name);
        logger.info("Creating schema {}", sql);
        Connection connection = DriverManager.getConnection(cfg.get("url"), cfg.get("user"), cfg.get("password"));
        connection.createStatement().execute(sql);
        connection.close();
    }

    private String generateSchemaName() throws NoSuchAlgorithmException {
        SimpleDateFormat format = new SimpleDateFormat("MMddyyyy");
        MessageDigest m5 = MessageDigest.getInstance("MD5");

        m5.update(UUID.randomUUID().toString().getBytes());
        byte[] digest = m5.digest();
        StringBuffer hash = new StringBuffer();
        for (byte b : digest) {
            hash.append(String.format("%02x", b & 0xff));
        }

        return String.format("%s_%s_%s",
            name.toLowerCase(),
            format.format(new Date()),
            hash);
    }
}
