package com.example.caresharecarpool;

public class Student {

    public String fullname, username, email, gender, university, phone;

    public Student(){

    }

    public Student(String fullname, String username, String email, String gender, String university, String phone) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.university = university;
        this.phone = phone;
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

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
