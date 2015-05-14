package com.goldpoisk.parser.orm;

import javax.persistence.*;

@Entity
@Table(name="shop_shop")
public class Shop {
    @Id
    Integer id;

    String name;
}
