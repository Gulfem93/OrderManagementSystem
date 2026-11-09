package com.ordermanagementsystem.dev.model;

import com.ordermanagementsystem.enums.CategoryType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// products.productCategory sütunu metin olarak kalır (ör. "LAPTOP").
// Enum'a geçmeden önce elle yazılmış eski değerler ("Electronics" gibi) uygulamayı çökertmez:
// eşleşmeyenler null (kategorisiz) okunur, owner Edit ile yeni kategori seçebilir.
@Converter
public class CategoryTypeConverter implements AttributeConverter<CategoryType, String> {

    @Override
    public String convertToDatabaseColumn(CategoryType category) {
        return category == null ? null : category.name();
    }

    @Override
    public CategoryType convertToEntityAttribute(String value) {
        return CategoryType.fromValue(value);
    }
}
