package org.common.service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
public class UserState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String middleName;
    private String lastName;

    private String emailAddress;
    private String mobileNumber;

    public UserState() {
    }

    private boolean favorites;

<<<<<<< HEAD
=======
    private ArrayList<String> categories;

>>>>>>> e354414 (Basic Category feature adding)
    public long getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }
<<<<<<< HEAD
=======

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }
>>>>>>> e354414 (Basic Category feature adding)
}
