package com.example.tsurratingbot2022;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    public Company(String name, CompanyType companyType) {
        this.name = name;
        this.companyType = companyType;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CompanyType companyType;
}
