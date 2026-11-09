package com.ordermanagementsystem.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Hiyerarşik kategori enum'u.
 * Her sabit, üst kategorisini (parent) tutar; ana kategorilerde parent = null.
 */
public enum CategoryType {

    // ===== Ana kategoriler =====
    KADIN("Kadın", null),
    ERKEK("Erkek", null),
    ANNE_COCUK("Anne & Çocuk", null),
    EV_YASAM("Ev & Yaşam", null),
    SUPERMARKET("Süpermarket", null),
    KOZMETIK("Kozmetik", null),
    ELEKTRONIK("Elektronik", null),
    SPOR_OUTDOOR("Spor & Outdoor", null),

    // ===== Kadın =====
    KADIN_GIYIM("Kadın Giyim", KADIN),
    ELBISE("Elbise", KADIN_GIYIM),
    KADIN_TISORT("Kadın Tişört", KADIN_GIYIM),
    KADIN_AYAKKABI("Kadın Ayakkabı", KADIN),
    KADIN_CANTA("Kadın Çanta", KADIN),

    // ===== Erkek =====
    ERKEK_GIYIM("Erkek Giyim", ERKEK),
    GOMLEK("Gömlek", ERKEK_GIYIM),
    ERKEK_PANTOLON("Erkek Pantolon", ERKEK_GIYIM),
    ERKEK_AYAKKABI("Erkek Ayakkabı", ERKEK),

    // ===== Anne & Çocuk =====
    BEBEK_GIYIM("Bebek Giyim", ANNE_COCUK),
    OYUNCAK("Oyuncak", ANNE_COCUK),

    // ===== Ev & Yaşam =====
    MOBILYA("Mobilya", EV_YASAM),
    EV_TEKSTILI("Ev Tekstili", EV_YASAM),
    MUTFAK_GERECLERI("Mutfak Gereçleri", EV_YASAM),

    // ===== Süpermarket =====
    GIDA("Gıda", SUPERMARKET),
    TEMIZLIK("Temizlik Ürünleri", SUPERMARKET),

    // ===== Kozmetik =====
    MAKYAJ("Makyaj", KOZMETIK),
    CILT_BAKIMI("Cilt Bakımı", KOZMETIK),
    PARFUM("Parfüm", KOZMETIK),

    // ===== Elektronik =====
    CEP_TELEFONU("Cep Telefonu", ELEKTRONIK),
    AKILLI_TELEFON("Akıllı Telefon", CEP_TELEFONU),
    TELEFON_KILIFI("Telefon Kılıfı", CEP_TELEFONU),
    BILGISAYAR_TABLET("Bilgisayar & Tablet", ELEKTRONIK),
    LAPTOP("Laptop", BILGISAYAR_TABLET),
    TABLET("Tablet", BILGISAYAR_TABLET),
    BEYAZ_ESYA("Beyaz Eşya", ELEKTRONIK),
    KUCUK_EV_ALETLERI("Küçük Ev Aletleri", ELEKTRONIK),

    // ===== Spor & Outdoor =====
    SPOR_GIYIM("Spor Giyim", SPOR_OUTDOOR),
    KAMP_MALZEMELERI("Kamp Malzemeleri", SPOR_OUTDOOR);

    private final String displayName;
    private final CategoryType parent;

    CategoryType(String displayName, CategoryType parent) {
        this.displayName = displayName;
        this.parent = parent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CategoryType getParent() {
        return parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    /** Doğrudan alt kategoriler */
    public List<CategoryType> getChildren() {
        return Arrays.stream(values()).filter(c -> c.parent == this).toList();
    }

    /**
     * Bu kategori, verilen kategorinin altında mı? (LAPTOP.isUnder(ELEKTRONIK) ->
     * true)
     */
    public boolean isUnder(CategoryType ancestor) {
        for (CategoryType c = this; c != null; c = c.parent) {
            if (c == ancestor)
                return true;
        }
        return false;
    }

    /** Kendisi + tüm alt kategoriler (filtreleme için) */
    public List<CategoryType> getSelfAndDescendants() {
        return Arrays.stream(values()).filter(c -> c.isUnder(this)).toList();
    }

    /** En üstteki ana kategori (LAPTOP -> ELEKTRONIK) */
    public CategoryType getRoot() {
        CategoryType c = this;
        while (c.parent != null) {
            c = c.parent;
        }
        return c;
    }

    /** Derinlik: ana kategori 0, alt kategori 1, onun altı 2 ... */
    public int getDepth() {
        int depth = 0;
        for (CategoryType c = parent; c != null; c = c.parent) {
            depth++;
        }
        return depth;
    }

    /** Tam yol: "Elektronik › Bilgisayar & Tablet › Laptop" */
    public String getPath() {
        return parent == null ? displayName : parent.getPath() + " › " + displayName;
    }

    /** Seçim kutusunda girintili gösterim: "    Laptop" */
    public String getIndentedName() {
        return "    ".repeat(getDepth()) + displayName;
    }

    /** Ad (LAPTOP) veya görünen ad (Laptop) ile bulur; bulunamazsa null */
    public static CategoryType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(v) || c.displayName.equalsIgnoreCase(v))
                .findFirst()
                .orElse(null);
    }

    /** Ana kategoriler */
    public static List<CategoryType> roots() {
        return Arrays.stream(values()).filter(CategoryType::isRoot).toList();
    }
}
