package com.olalayeone.audittrailtest.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Data
@NoArgsConstructor
@Embeddable
public class KVTag {

    private String key;
    private String value;

    public KVTag(String key, String value) {
        this.key = key;
        this.value = value;
    }
}