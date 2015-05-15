package com.goldpoisk.parser.orm;

import javax.persistence.*;

@Entity
@Table(name="goldpoisk_update_entity")
public class UpdatedValue {
    @Id @GeneratedValue
    Integer id;
    String article;
    String fieldName;
    String fieldValue;
    String category;

    public UpdatedValue(String article, String fieldName, String fieldValue, String category) {
        this.article = article;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.category = category;
    }
}
