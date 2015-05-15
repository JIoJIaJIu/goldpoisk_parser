package com.goldpoisk.parser.orm;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name="info")
public class Info {
    @Id @GeneratedValue
    Integer id;
    @Column(name="shop")
    String shopName;
    @Column(name="schema")
    String schemaName;
    Date datetime;
    @Column(name="parsed")
    Boolean isParsed;

    public Info(String shopName, String schemaName) {
        this.shopName = shopName;
        this.schemaName = schemaName;
        datetime = new Date();
    }
}
