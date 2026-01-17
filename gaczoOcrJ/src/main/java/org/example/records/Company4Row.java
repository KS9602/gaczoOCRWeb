package org.example.records;

public record Company4Row(
        String lp,
        String symbol,
        String name,
        String quantity,
        String unit,
        String promo,
        String unitNetValue,
        String vatPercent,
        String netValue,
        String vat,
        String grossValue,
        String margin
) { }
