package com.hackaton.sadem.api.model;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public class Code {
    private String code;
    private String description;

    public Code(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}