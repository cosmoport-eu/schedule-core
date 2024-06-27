package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "EVENT_TYPE")
public class EventTypeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "category_id")
    private Integer categoryId;
    @Basic
    @Column(name = "default_duration")
    private int defaultDuration;
    @Basic
    @Column(name = "default_repeat_interval")
    private int defaultRepeatInterval;
    @Basic
    @Column(name = "default_cost")
    private double defaultCost;
    @Column(name = "i18n_name_code")
    private String nameCode;
    @Column(name = "i18n_desc_code")
    private String descCode;
    @Column(name = "parent_id")
    private Integer parentId;
    @Column(name = "is_disabled")
    private boolean isDisabled;
    @ManyToMany
    @JoinTable(
            name = "EVENT_TYPE__MATERIAL",
            joinColumns = @JoinColumn(name = "event_type_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private Set<MaterialEntity> materials = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "EVENT_TYPE__FACILITY",
            joinColumns = @JoinColumn(name = "event_type_id"),
            inverseJoinColumns = @JoinColumn(name = "facility_id"))
    private Set<FacilityEntity> facilities = new HashSet<>();
}
