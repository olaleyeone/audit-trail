package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class ItemType {

    @Id
    private Long id;
    @OneToMany
    private List<Item> items = new ArrayList<>();

    private String name;
}
