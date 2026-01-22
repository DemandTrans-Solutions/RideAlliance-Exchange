package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ethnicity")
public class Ethnicity extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EthnicityId")
    private Integer ethnicityId;

    private String name;
}
