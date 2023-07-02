package main_service.event.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "locations", schema = "public")
@Data
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Float lat;
    @Column(nullable = false)
    private Float lon;
}
