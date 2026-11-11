package com.ordermanagementsystem.dev.model;

import java.math.BigDecimal;

import com.ordermanagementsystem.enums.CategoryType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column
    private String productCode;

    @Column
    private String productName;

    @Column
    private BigDecimal productPrice;

    @Column
    private Long productStock;

    // CategoryType enum'undan seçilir; veritabanında adı saklanır (ör. "LAPTOP")
    @Column
    @Convert(converter = CategoryTypeConverter.class)
    private CategoryType productCategory;

    // Ürünü ekleyen owner'ın şirketi
    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyModel company;

    // Şablonlar için kısayol (veritabanında saklanmaz)
    public String getCompanyName() {
        return company == null ? null : company.getCompanyName();
    }

    public boolean belongsTo(Long companyId) {
        return companyId != null && company != null && companyId.equals(company.getCompanyId());
    }

}
