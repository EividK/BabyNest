package com.assignment_two_starter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author Alan.Ryan
 */
@Entity
@Table(name = "orders")
@Data
public class Orders implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "order_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Basic(optional = false)
    @Column(name = "status")
    private String status;

    @Basic(optional = false)
    @Column(name = "address_change_fee")
    private Double addressChangeFee;

    @Column(name = "estimated_Shipping_Date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date estimatedShippingDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<Payment> paymentsList = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<OrderItems> orderItemsList = new HashSet<>();

    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0; // Avoid recursive loop
    }

    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    @ToString.Exclude
    @JsonIgnore
    private Customer customer;

    @JoinColumn(name = "shipping_address_id", referencedColumnName = "address_id")
    @ManyToOne
    @ToString.Exclude
    private Address shippingAddressId;



}
