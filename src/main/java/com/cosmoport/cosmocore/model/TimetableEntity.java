package com.cosmoport.cosmocore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TIMETABLE")
public class TimetableEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "event_date")
    private String eventDate;
    @Basic
    @Column(name = "event_type_id")
    private Integer eventTypeId;
    @Basic
    @Column(name = "event_status_id")
    private Integer eventStatusId;
    @Basic
    @Column(name = "event_state_id")
    private int eventStateId;
    @Basic
    @Column(name = "gate_id")
    private Integer gateId;
    @Basic
    @Column(name = "gate2_id")
    private Integer gate2Id;
    @Basic
    @Column(name = "start_time")
    private int startTime;
    @Basic
    @Column(name = "duration_time")
    private int durationTime;
    @Basic
    @Column(name = "repeat_interval")
    private int repeatInterval;
    @Basic
    @Column(name = "cost")
    private double cost;
    @Basic
    @Column(name = "people_limit")
    private int peopleLimit;
    @Basic
    @Column(name = "contestants")
    private int contestants;
    @Column(name = "date_added", insertable = false, updatable = false)
    private String dateAdded;
    @Column(name = "description")
    private String description;
    @ManyToMany
    @JoinTable(
            name = "TIMETABLE__MATERIAL",
            joinColumns = @JoinColumn(name = "timetable_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private Set<MaterialEntity> materials = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "TIMETABLE__FACILITY",
            joinColumns = @JoinColumn(name = "timetable_id"),
            inverseJoinColumns = @JoinColumn(name = "facility_id"))
    private Set<FacilityEntity> facilities = new HashSet<>();
}
