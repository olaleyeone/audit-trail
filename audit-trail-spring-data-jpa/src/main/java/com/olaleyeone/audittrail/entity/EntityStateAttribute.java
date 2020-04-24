package com.olaleyeone.audittrail.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class EntityStateAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private EntityState entityState;

    private String name;
    private String value;

    private boolean modified;
    private String previousValue;
}
