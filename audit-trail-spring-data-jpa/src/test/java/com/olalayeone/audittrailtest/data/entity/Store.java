package com.olalayeone.audittrailtest.data.entity;

import com.olaleyeone.audittrail.api.IgnoreData;
import lombok.Data;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@Access(AccessType.PROPERTY)
public class Store {

    private Long id;
//    @OneToMany
//    private List<Item> items = new ArrayList<>();
    private Long itemsSold;
    private Boolean active;

//    @ManyToOne
//    private Location location;

    private String name;
    private String pin;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @IgnoreData
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}