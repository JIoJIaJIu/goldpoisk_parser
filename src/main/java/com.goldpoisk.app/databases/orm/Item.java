package com.goldpoisk.parser.orm;

import java.lang.String;

import javax.persistence.*;

@Entity
@Table(name="product_item")
public class Item {
    @Id
    Integer id;
    @Column(name="cost")
    public Integer price;
    @Column(name="quantity")
    public Integer count;

    @ManyToOne
    Product product;

    @ManyToOne
    Shop shop;


    public String toString() {
        if (this == null)
            return "NULL";

        return String.format("" + //TODO memory address
            "[id]: %d, [pid]: %d, [sid]: %d, " +
            "[a]: %s, [s]: %s",
            id, product.id, shop.id,
            product.article, shop.name);
    }
}
