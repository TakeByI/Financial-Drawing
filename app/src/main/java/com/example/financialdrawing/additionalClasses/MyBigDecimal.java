package com.example.financialdrawing.additionalClasses;

import java.math.BigDecimal;

public class MyBigDecimal {
    private BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public MyBigDecimal(BigDecimal value) {
        this.value = value;
    }
}
