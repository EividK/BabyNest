package com.assignment_two_starter.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.*;
import lombok.Data;

/**
 *
 * @author Alan.Ryan
 */
@Entity
@Table(name = "discount_codes")
@Data
public class DiscountCodes implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "code_id")
    private Integer codeId;

    @Basic(optional = false)
    @Column(name = "code")
    private String code;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @Column(name = "discount_value")
    private Double discountValue;

    @Column(name = "expiration_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Column(name = "status")
    private String status;

}
