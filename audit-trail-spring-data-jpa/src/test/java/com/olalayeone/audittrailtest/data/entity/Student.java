package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Student {

    @Id
    private long id;
    private String name;

    @ElementCollection
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    private List<SkillTag> skillTags = new ArrayList<>();

    @ElementCollection
    private List<KVTag> kvTags = new ArrayList<>();

    @OneToOne
    private User user;

    public Student(long id, String name) {
        this.id = id;
        this.name = name;
    }

}