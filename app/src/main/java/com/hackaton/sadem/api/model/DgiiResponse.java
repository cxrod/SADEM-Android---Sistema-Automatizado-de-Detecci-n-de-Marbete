package com.hackaton.sadem.api.model;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public class DgiiResponse extends BaseResponse{

    private Data data;

    public Data getData() {
        return data;
    }

    public static class Code {

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

    public static class Data{
        private String uuid;
        private Code code;
        private String license_plate;
        private String brand;
        private String model;
        private String type_vehicle;
        private String year_production;
        private float amount;
        private String owner;
        private String document_description;
        private String document_type;
        private boolean oposition;
        private boolean valid;
        private boolean penalized;

        public Data(Code code) {
            this.code = code;
        }

        public String getUuid() {
            return uuid;
        }

        public Code getCode() {
            return code;
        }

        public String getLicense_plate() {
            return license_plate;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public String getType_vehicle() {
            return type_vehicle;
        }

        public String getYear_production() {
            return year_production;
        }

        public float getAmount() {
            return amount;
        }

        public String getOwner() {
            return owner;
        }

        public String getDocument_description() {
            return document_description;
        }

        public String getDocument_type() {
            return document_type;
        }

        public boolean isOposition() {
            return oposition;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isPenalized() {
            return penalized;
        }
    }
}
