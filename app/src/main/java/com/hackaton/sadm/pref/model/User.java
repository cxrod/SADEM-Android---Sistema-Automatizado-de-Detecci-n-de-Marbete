package com.hackaton.sadm.pref.model;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by cesar_000 on 17/11/2017.
 */

public class User {

    @Expose
    @SerializedName("first_name")
    private String firstName;

    @Expose
    @SerializedName("last_name")
    private String lastName;

    @Expose
    @SerializedName("birthday")
    private Date birthday;

    @Expose
    @SerializedName("sex")
    private char  sex;

    @Expose
    @SerializedName("agent_number")
    private int  agentNumber;

    @Expose
    @SerializedName("email")
    private String  email;

    @Expose
    @SerializedName("phone")
    private String  phone;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public int getAgentNumber() {
        return agentNumber;
    }

    public void setAgentNumber(int agentNumber) {
        this.agentNumber = agentNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public static User fromJson(String json){
        return new Gson().fromJson(json, User.class);
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}
