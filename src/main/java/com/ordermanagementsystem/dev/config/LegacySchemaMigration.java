package com.ordermanagementsystem.dev.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Eski veri modelinden yenisine tek seferlik geçiş (MySQL). Her açılışta çalışır ama
 * sadece eski sütun/tablo varsa iş yapar; yeni kurulan veritabanında hiçbir şey yapmaz.
 *
 * Eski model                              Yeni model
 * users.company_name / products.company_name (metin)  → companies tablosu + company_id
 * customers tablosu, orders.customer_id               → orders.user_id (users tablosu)
 * users.role (USER/ADMIN) + users.user_role (metin)   → sadece users.user_role (UserRole enum)
 *
 * Hibernate (ddl-auto: update) yeni sütunları ekler ama veri taşımaz ve eski sütunları silmez;
 * özellikle eski NOT NULL orders.customer_id sütunu kalırsa yeni sipariş kaydedilemez.
 * Diğer başlangıç işlerinden (admin oluşturma vb.) önce çalışır.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacySchemaMigration implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySchemaMigration.class);

    // Eski customers kaydından oluşturulan kullanıcılar giriş yapamaz (BCrypt değil, aktif değil)
    private static final String NO_LOGIN_PASSWORD = "!migrated-customer-no-login";

    private final JdbcTemplate jdbc;

    public LegacySchemaMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrateRoles();
        migrateCompanies("users");
        migrateCompanies("products");
        migrateOrdersToUsers();
    }

    // users.role + users.user_role → tek user_role sütunu
    private void migrateRoles() {
        if (!columnExists("users", "user_role")) {
            return;
        }
        if (columnExists("users", "role")) {
            jdbc.update("update users set user_role = 'ADMIN' "
                    + "where role = 'ADMIN' and (user_role is null or trim(user_role) = '')");
            jdbc.execute("alter table users drop column role");
            LOGGER.info("Migration: users.role kaldırıldı, rol artık sadece users.user_role'de.");
        }
        // "role_owner", " Owner " gibi değerleri düzelt; tanınmayanlar CUSTOMER olur
        jdbc.update("update users set user_role = upper(trim(user_role)) where user_role is not null");
        jdbc.update("update users set user_role = substring(user_role, 6) where user_role like 'ROLE\\_%'");
        int fixed = jdbc.update("update users set user_role = 'CUSTOMER' "
                + "where user_role is null or user_role not in ('CUSTOMER', 'OWNER', 'ADMIN')");
        if (fixed > 0) {
            LOGGER.info("Migration: {} kullanıcının rolü CUSTOMER yapıldı (boş/tanınmayan rol).", fixed);
        }
    }

    // <table>.company_name (metin) → companies tablosu + <table>.company_id
    private void migrateCompanies(String table) {
        if (!columnExists(table, "company_name") || !columnExists(table, "company_id")) {
            return;
        }
        // Aynı ad tek şirket olur (companies.company_name benzersiz; MySQL karşılaştırması büyük/küçük harf duyarsız)
        jdbc.update("insert ignore into companies (company_name) "
                + "select distinct trim(company_name) from " + table + " "
                + "where company_name is not null and trim(company_name) <> ''");
        int linked = jdbc.update("update " + table + " t join companies c on c.company_name = trim(t.company_name) "
                + "set t.company_id = c.company_id where t.company_id is null");
        jdbc.execute("alter table " + table + " drop column company_name");
        LOGGER.info("Migration: {}.company_name → companies ({} satır bağlandı), eski sütun kaldırıldı.",
                table, linked);
    }

    // customers + orders.customer_id → orders.user_id
    private void migrateOrdersToUsers() {
        if (!tableExists("customers") || !columnExists("orders", "customer_id")
                || !columnExists("orders", "user_id")) {
            return;
        }

        // Sipariş vermiş ama users'ta hesabı olmayan eski müşteriler: giriş yapamayan pasif kullanıcı olarak eklenir
        int created = jdbc.update("insert into users "
                + "(user_email, user_name, user_surname, user_phone, user_role, user_active, user_password) "
                + "select c.customer_email, c.customer_name, c.customer_surname, c.customer_phone, "
                + "'CUSTOMER', 0, ? from customers c "
                + "where c.customer_email is not null and trim(c.customer_email) <> '' "
                + "and exists (select 1 from orders o where o.customer_id = c.customer_id) "
                + "and not exists (select 1 from users u where lower(u.user_email) = lower(c.customer_email))",
                NO_LOGIN_PASSWORD);

        int linked = jdbc.update("update orders o "
                + "join customers c on c.customer_id = o.customer_id "
                + "join users u on lower(u.user_email) = lower(c.customer_email) "
                + "set o.user_id = u.user_id where o.user_id is null");

        dropForeignKeys("orders", "customer_id");
        Integer unlinked = jdbc.queryForObject("select count(*) from orders where user_id is null", Integer.class);
        if (unlinked != null && unlinked > 0) {
            // Veri kaybetmemek için eski sütun ve tablo silinmez; sadece yeni siparişleri engellemesin
            jdbc.execute("alter table orders modify customer_id bigint null");
            LOGGER.warn("Migration: {} sipariş bir kullanıcıya bağlanamadı (müşterinin e-postası yok). "
                    + "orders.customer_id ve customers tablosu inceleme için bırakıldı.", unlinked);
            return;
        }

        jdbc.execute("alter table orders drop column customer_id");
        jdbc.execute("drop table customers");
        LOGGER.info("Migration: siparişler users'a bağlandı ({} sipariş, {} yeni pasif kullanıcı); "
                + "customers tablosu kaldırıldı.", linked, created);
    }

    private boolean tableExists(String table) {
        Integer n = jdbc.queryForObject("select count(*) from information_schema.tables "
                + "where table_schema = database() and table_name = ?", Integer.class, table);
        return n != null && n > 0;
    }

    private boolean columnExists(String table, String column) {
        Integer n = jdbc.queryForObject("select count(*) from information_schema.columns "
                + "where table_schema = database() and table_name = ? and column_name = ?",
                Integer.class, table, column);
        return n != null && n > 0;
    }

    private void dropForeignKeys(String table, String column) {
        List<String> names = jdbc.queryForList("select constraint_name from information_schema.key_column_usage "
                + "where table_schema = database() and table_name = ? and column_name = ? "
                + "and referenced_table_name is not null", String.class, table, column);
        for (String name : names) {
            jdbc.execute("alter table " + table + " drop foreign key `" + name + "`");
        }
    }
}
