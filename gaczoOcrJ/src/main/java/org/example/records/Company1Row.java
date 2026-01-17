package org.example.records;

public record Company1Row(
        String lp,
        String name,
        String quantity,
        String unitPrice,
        String netValue,
        String vatRate,
        String vatAmount,
        String grossValue,
        String margin
) {}

