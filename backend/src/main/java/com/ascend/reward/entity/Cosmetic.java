package com.ascend.reward.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cosmetics")
public class Cosmetic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "rarity", nullable = false, length = 20)
    private String rarity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "gem_cost")
    private Integer gemCost;

    @Column(name = "coin_cost")
    private Integer coinCost;
}
