package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@Entity
public class Possession {

    @Id
    private long id;

    private String name;

    public Possession(final String name) {
        this.name = name;
    }

}