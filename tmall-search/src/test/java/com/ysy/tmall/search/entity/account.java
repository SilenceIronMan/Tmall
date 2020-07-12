package com.ysy.tmall.search.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @anthor silenceYin
 * @date 2020/7/12 - 4:58
 */
@Data
public class account {
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
