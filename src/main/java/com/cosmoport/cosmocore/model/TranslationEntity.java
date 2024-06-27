package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "TRANSLATION")
public class TranslationEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "locale_id")
    private int localeId;
    private String code;
    @Basic
    @Column(name = "tr_text")
    private String text;
    private boolean isExternal;
}
