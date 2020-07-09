package com.patukes.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userid;
    private String email;
    private String username;
    private String password;
    private String usertype;
}
