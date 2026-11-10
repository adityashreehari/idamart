package com.idamart.entity;

import jakarta.persistence.*;
import lombok.*;

/** e.g. Figures, Apparel, Posters, Manga, Accessories */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 255)
    private String description;
}
