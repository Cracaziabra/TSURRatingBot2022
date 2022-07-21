package com.example.tsurratingbot2022;

public enum CompanyType {
    OMSU("ОМСУ"), ROIV("РОИВ");

    private final String name;

    CompanyType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
