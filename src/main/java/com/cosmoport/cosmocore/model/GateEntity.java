package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "GATE")
public class GateEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "i18n_code")
    private String code;
    @Column(name = "is_disabled")
    private boolean isDisabled;

    public GateEntity() {
    }

    public GateEntity(String code) {
        this.code = code;
    }
}
