package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Data
@Entity
public class Item {

    private String color;
    private String grade;

    @Id
    private Long id;

    @ManyToOne
    private ItemType itemType;

    private String name;
    private BigDecimal price;

    @ManyToOne
    private Store store;

    @Embedded
    private Audit audit;
}
