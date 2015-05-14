package com.goldpoisk.parser.orm;

import javax.persistence.*;

@Entity
@Table(name="product_product")
public class Product {
    @Id
    Integer id;

    @Column(name="number")
    String article;
}
