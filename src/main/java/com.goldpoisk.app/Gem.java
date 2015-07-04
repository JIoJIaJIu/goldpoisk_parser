package goldpoisk_parser;

import javax.persistence.*;

@Entity
@Table("goldpoisk_gem")
public class Gem {
    @Id @GeneratedValue
    Integer id;
    String name;
    Float weight;

    public Gem(name, weight) {
        this.name = name;
        this.weight = weight;
    }
}
