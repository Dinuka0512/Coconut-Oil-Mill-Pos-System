package com.dev09.kumaracoconutoil.dto;

public class CustomerDto {
    private String custId;
    private String name;
    private String contact;

    public CustomerDto() {}

    public CustomerDto(String custId, String name, String contact) {
        this.custId = custId;
        this.name = name;
        this.contact = contact;
    }

    public String getCustId() { return custId; }
    public void setCustId(String custId) { this.custId = custId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    @Override
    public String toString() {
        return "CustomerDto{custId=" + custId + ", name=" + name + ", contact=" + contact + "}";
    }
}
