package main_service.category.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "categories", schema = "public")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
}
