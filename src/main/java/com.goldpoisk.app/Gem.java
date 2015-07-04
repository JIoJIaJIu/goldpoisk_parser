package goldpoisk_parser;

import javax.persistence.*;

@Entity
@Table(name="goldpoisk_gem")
public class Gem {
    @Id @GeneratedValue
    Integer id;
    String name;
    Float weight;

    public Gem(String name, Float weight) {
        this.name = name;
        this.weight = weight;
    }
}
