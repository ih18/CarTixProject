package com.example.cartix;

import java.lang.reflect.Constructor;

public class UserInformation {
    private String userName;
    private String lastName;
    private String email;
    private String password;

    public UserInformation() {

    }

    public UserInformation(String userName, String lastName, String email, String password) {
        this.userName = userName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
