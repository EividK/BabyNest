package com.assignment_two_starter.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@Column(name = "id")
    private Long id;

    //@Column(name = "name")
    private String name;

    //The inverse side of the many-to-many relationship with the Customer entity
    //Each Role can be associated with multiple Customers, and each Customer can have multiple Roles.
    //Using a Set is imortat here so that each customer is unique within the collection, preventing duplicates.
    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Customer> customers;

}
