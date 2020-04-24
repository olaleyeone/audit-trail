package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Data
@NoArgsConstructor
@Embeddable
public class SkillTag {

    private String name;
    private int value;

    public SkillTag(String name, int value) {
        this.name = name;
        this.value = value;
    }
}