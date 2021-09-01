package com.yh.esdemo.domain;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author : yh
 * @date : 2021/9/1 21:28
 */
@Data
public class Staff implements Serializable {

    private static final long serialVersionUID = -8701034189084186667L;

    private Long accountNumber;
    private BigDecimal balance;
    private String firstname;
    private String lastname;
    private Integer age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;

}
