package com.pili.oauth2login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Birthday {
    private PeopleApiDate date;

    public PeopleApiDate getDate() {
        return date;
    }

    public void setDate(PeopleApiDate date) {
        this.date = date;
    }

    public int getYear() {
        return (date != null) ? date.getYear() : 0;
    }

    public int getMonth() {
        return (date != null) ? date.getMonth() : 1;
    }

    public int getDay() {
        return (date != null) ? date.getDay() : 1;
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(getYear(), getMonth(), getDay());
    }
}
