package com.pili.oauth2login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    private String resourceName;
    private List<Name> names;
    private List<EmailAddress> emailAddresses;
    private List<Photo> photos;
    private List<PhoneNumber> phoneNumbers;
    private List<Address> addresses;
    private List<Birthday> birthdays;
    private List<Gender> genders;
    private Age age; // Change from List<AgeRange> to Age

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<Name> getNames() {
        return names;
    }

    public void setNames(List<Name> names) {
        this.names = names;
    }

    public List<EmailAddress> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<EmailAddress> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Birthday> getBirthdays() {
        return birthdays;
    }

    public void setBirthdays(List<Birthday> birthdays) {
        this.birthdays = birthdays;
        this.calculateAge();
    }

    public List<Gender> getGenders() {
        return genders;
    }

    public void setGenders(List<Gender> genders) {
        this.genders = genders;
    }

    public Age getAge() {
        return age;
    }

    private void calculateAge() {
        if (birthdays != null && !birthdays.isEmpty()) {
            Birthday birthday = birthdays.get(0);
            LocalDate birthDate = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDay());
            this.age = new Age(birthDate);
        } else {
            this.age = null;
        }
    }
}
