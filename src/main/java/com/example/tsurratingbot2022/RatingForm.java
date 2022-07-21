package com.example.tsurratingbot2022;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class RatingForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;
    private String userName;
    private String company;
    private String criteria;
    private Date scoreDate;
    private Long score;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Enumerated(EnumType.STRING)
    private CompanyType companyType;
}
