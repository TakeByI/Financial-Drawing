package com.example.financialdrawing.DataSamples;

import com.example.financialdrawing.additionalClasses.MyBigDecimal;
import com.google.firebase.Timestamp;

import java.math.BigDecimal;
import java.util.Objects;

public class User {
    private String uid;
    private String name;
    private String email;
    private String balance;
    private Timestamp createdAt; //метка времени по UTC

    // Пустой конструктор (обязателен для Firestore)
    public User() {}

    public User(String uid, String name, String email, String balance) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.createdAt = Timestamp.now();  // Автоматическая установка времени
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(balance, user.balance) && Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, email, balance, createdAt);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", balance=" + balance +
                ", createdAt=" + createdAt +
                '}';
    }
}
