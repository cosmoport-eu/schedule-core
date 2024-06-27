package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "LOCALE")
public class LocaleEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "code")
    private String code;
    @Basic
    @Column(name = "is_default")
    private boolean isDefault;
    @Basic
    @Column(name = "locale_description")
    private String localeDescription;
    @Basic
    @Column(name = "show")
    private boolean show;
    @Basic
    @Column(name = "show_time")
    private int showTime;
}
