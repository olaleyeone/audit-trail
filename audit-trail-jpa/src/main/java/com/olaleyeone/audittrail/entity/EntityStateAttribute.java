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

    private boolean modified;

    private boolean hasPreviousValue;
    private boolean hasNewValue;

    @Column(columnDefinition="TEXT")
    private String newValue;
    @Column(columnDefinition="TEXT")
    private String previousValue;
}
