package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "MATERIAL")
public class MaterialEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "i18n_code")
    private String code;

    @Column(name = "is_disabled")
    private boolean isDisabled;

    @ManyToMany(mappedBy = "materials")
    private Set<EventTypeEntity> eventTypes = new HashSet<>();

    @ManyToMany(mappedBy = "materials")
    private Set<TimetableEntity> events = new HashSet<>();
}
