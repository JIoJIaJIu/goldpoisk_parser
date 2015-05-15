package com.goldpoisk.parser.orm;

import java.sql.Blob;

import javax.persistence.*;

import goldpoisk_parser.Product;

@Entity
@Table(name="goldpoisk_entity_images")
public class Image {
    @Id @GeneratedValue
    Integer id;
    byte[] image;

    public Image(Product product, byte[] image) {
        this.product = product;
        this.image = image;
    }

    @ManyToOne
    Product product;
}
