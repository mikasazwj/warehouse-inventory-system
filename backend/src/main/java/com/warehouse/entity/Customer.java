package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 客户实体类
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_code", columnList = "code", unique = true),
    @Index(name = "idx_customer_name", columnList = "name")
})
public class Customer extends BaseEntity {

    @NotBlank(message = "客户编码不能为空")
    @Size(max = 50, message = "客户编码长度不能超过50个字符")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 200, message = "客户名称长度不能超过200个字符")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "customer_type", length = 20)
    private String customerType = "企业客户";

    @Column(name = "legal_person", length = 50)
    private String legalPerson;

    @Column(name = "business_license", length = 100)
    private String businessLicense;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Email(message = "邮箱格式不正确")
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "fax", length = 20)
    private String fax;

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "credit_level", length = 10)
    private String creditLevel = "A";

    @Column(name = "cooperation_start_date")
    private java.time.LocalDate cooperationStartDate;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "remark", length = 1000)
    private String remark;

    // Constructors
    public Customer() {
    }

    public Customer(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getLegalPerson() {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        this.legalPerson = legalPerson;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getCreditLevel() {
        return creditLevel;
    }

    public void setCreditLevel(String creditLevel) {
        this.creditLevel = creditLevel;
    }

    public java.time.LocalDate getCooperationStartDate() {
        return cooperationStartDate;
    }

    public void setCooperationStartDate(java.time.LocalDate cooperationStartDate) {
        this.cooperationStartDate = cooperationStartDate;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", customerType='" + customerType + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
