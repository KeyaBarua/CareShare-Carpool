package com.example.careshare;

public class Driver {

    public String fullname, username, email, gender, phone, license;

    public Driver(){

    }

    public Driver(String fullname, String username, String email, String gender, String phone, String license) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
        this.license = license;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
