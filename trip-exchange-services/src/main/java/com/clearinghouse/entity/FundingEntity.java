package com.clearinghouse.entity;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "fundingEntity")
public class FundingEntity extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FundingEntityId")
    private Integer fundingEntityId;

    private String name;
}
