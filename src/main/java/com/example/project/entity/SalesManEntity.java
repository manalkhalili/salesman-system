package com.example.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "salesman")
public class SalesManEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Column(unique = true, nullable = false)
    @NotNull(message = "Email cannot be null")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @NotNull(message = "Address cannot be null")
    private String address;

    @Pattern(regexp = "\\d{10}", message = "Phone number must be exactly 10 digits")
    @Column(nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    @NotNull(message = "National ID cannot be null")
    private int nationalId;

    @Column(nullable = false)
    @NotNull(message = "Password cannot be null")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{7,}$",
            message = "Password must be at least 7 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public SalesManEntity() {}

    public SalesManEntity(String name, String email, String address, String phoneNumber, int nationalId, String password) {
        this.name = name;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.nationalId = nationalId;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


     public String getName() {
    return name;
     }
     public void setName(String name) {
    this.name = name;
     }

     public String getEmail() {
    return email;
     }
     public void setEmail(String email) {
    this.email = email;
     }
     public String getAddress() {
    return address;
     }
     public void setAddress(String address) {
    this.address = address;
     }
     public String getPhoneNumber() {
    return phoneNumber;

     }
     public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
     }
     public int getnationalId() {
    return nationalId;
     }
     public void setnationalId(int nationalId) {
    this.nationalId = nationalId;
     }
     public String getPassword() {
    return password;
     }
     public void setPassword(String password) {
    this.password = password;
     }

}
