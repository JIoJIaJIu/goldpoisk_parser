package com.goldpoisk.parser.orm;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="info")
public class Info {
    @Id @GeneratedValue
    Integer id;
    String shopName;
    String schemaName;
    Date datetime;

    public Info(String shopName, String schemaName) {
        this.shopName = shopName;
        this.schemaName = schemaName;
        datetime = new Date();
    }
}
