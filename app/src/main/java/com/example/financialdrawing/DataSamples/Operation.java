package com.example.financialdrawing.DataSamples;

import com.google.firebase.Timestamp;
import java.util.Objects;

public class Operation {
    private String uid;  //id юзера, которому принадлежит операция
    private String title;  // Название операции
    private boolean isIncome;  // Тип    если true - значит доход, false - расход
    private String category; // категория   ?как хранить все возможныне категории? возможно отдельная коллекция под категории?
    private String amount; // сумма   сумму планирую хранить через String, а делать вычисления уже через BigDecimal
    private Timestamp createdAt; // время создания операции
    private String description;  // описание

    public Operation() {}

    public Operation(String uid, String title, boolean isIncome, String category,
                     String amount, Timestamp createdAt, String description) {
        this.uid = uid;
        this.title = title;
        this.isIncome = isIncome;
        this.category = category;
        this.amount = amount;
        this.createdAt = createdAt;
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public void setIncome(boolean income) {
        isIncome = income;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return isIncome == operation.isIncome && Objects.equals(uid, operation.uid) && Objects.equals(title, operation.title) && Objects.equals(category, operation.category) && Objects.equals(amount, operation.amount) && Objects.equals(createdAt, operation.createdAt) && Objects.equals(description, operation.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, title, isIncome, category, amount, createdAt, description);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "uid='" + uid + '\'' +
                ", title='" + title + '\'' +
                ", isIncome=" + isIncome +
                ", category='" + category + '\'' +
                ", amount='" + amount + '\'' +
                ", createdAt=" + createdAt +
                ", description='" + description + '\'' +
                '}';
    }
}
