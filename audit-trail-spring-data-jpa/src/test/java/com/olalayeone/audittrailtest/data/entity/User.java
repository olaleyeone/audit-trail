package com.olalayeone.audittrailtest.data.entity;

import com.olaleyeone.audittrail.api.IgnoreData;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
public class User {

    @Id
    private int id;
    private String name;
    private LocalDate creationDate;
    private LocalDate lastLoginDate;
    private boolean active;
    private int age;
    @Column(unique = true, nullable = false)
    private String email;
    private Integer status;

    @IgnoreData
    private String password;

    @OneToMany
    private List<Possession> possessionList;
}