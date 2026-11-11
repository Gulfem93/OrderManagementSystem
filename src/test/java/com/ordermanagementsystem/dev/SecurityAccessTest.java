package com.ordermanagementsystem.dev;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// SecurityConfig'deki rol kuralları: hangi rol hangi işleme erişebilir
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/products")).andExpect(status().is3xxRedirection());
    }

    @Test
    void ownerCannotUseCart() throws Exception {
        mvc.perform(post("/cart/checkout").with(user("owner@test.com").roles("OWNER")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotUseCart() throws Exception {
        mvc.perform(post("/cart/add").param("productId", "1")
                .with(user("admin@test.com").roles("ADMIN")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotAddProducts() throws Exception {
        mvc.perform(post("/products").with(user("customer@test.com").roles("CUSTOMER")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotChangeOrderStatus() throws Exception {
        mvc.perform(post("/orders/1/status").param("status", "SHIPPED")
                .with(user("customer@test.com").roles("CUSTOMER")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCannotOpenAdminPanel() throws Exception {
        mvc.perform(get("/admin").with(user("owner@test.com").roles("OWNER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/cart/checkout").with(user("customer@test.com").roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void signupWithBlankFieldsIsRejected() throws Exception {
        // Doğrulama hatası: kayıt yapılmaz, form tekrar gösterilir (login'e yönlendirme yok)
        mvc.perform(post("/signup").with(csrf())
                .param("userRole", "CUSTOMER")
                .param("userEmail", "")
                .param("userPassword", "123")
                .param("userName", "")
                .param("userSurname", ""))
                .andExpect(status().isOk());
    }
}
