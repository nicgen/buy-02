package com.buy01.userservice.dto;

public class UserProfileResponse {
    private String id;
    private String email;
    private String role;
    private String street;
    private String city;
    private String zip;
    private String country;
    private String phoneNumber;

    public UserProfileResponse() {
    }

    @SuppressWarnings("java:S107")
    private UserProfileResponse(String id, String email, String role, String street, String city, String zip,
            String country, String phoneNumber) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String email;
        private String role;
        private String street;
        private String city;
        private String zip;
        private String country;
        private String phoneNumber;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder zip(String zip) {
            this.zip = zip;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserProfileResponse build() {
            return new UserProfileResponse(id, email, role, street, city, zip, country, phoneNumber);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
