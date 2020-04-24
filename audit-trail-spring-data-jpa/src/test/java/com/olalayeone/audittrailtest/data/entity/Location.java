package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Location {

    private String city;
    private String country;
    @Id
    private Long id;

    @OneToMany
    private List<Store> stores = new ArrayList<>();
}