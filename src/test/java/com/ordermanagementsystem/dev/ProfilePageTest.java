package com.ordermanagementsystem.dev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.model.CompanyModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.CompanyRepository;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

// /profile sayfası gerçekten render ediliyor mu, form kaydı ve şifre doğrulaması çalışıyor mu
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfilePageTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void createUsers() {
        CompanyModel company = new CompanyModel();
        company.setCompanyName("Zehra Midder Company");
        companyRepository.save(company);

        userRepository.save(newUser("zehra@test.com", "Zehra", UserRole.OWNER, company));
        userRepository.save(newUser("jhon@test.com", "Jhon", UserRole.CUSTOMER, null));
    }

    private UsersModel newUser(String email, String name, UserRole role, CompanyModel company) {
        UsersModel user = new UsersModel();
        user.setUserEmail(email);
        user.setUserName(name);
        user.setUserSurname("Test");
        user.setUserPassword(passwordEncoder.encode("current123"));
        user.setUserRole(role);
        user.setUserActive(true);
        user.setCompany(company);
        return user;
    }

    @Test
    void ownerSeesProfileWithCompany() throws Exception {
        mvc.perform(get("/profile").with(user("zehra@test.com").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("zehra@test.com")))
                .andExpect(content().string(containsString("Zehra Midder Company")))
                .andExpect(content().string(containsString("Change Password")));
    }

    @Test
    void customerSeesProfile() throws Exception {
        mvc.perform(get("/profile").with(user("jhon@test.com").roles("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jhon@test.com")))
                .andExpect(content().string(containsString("Personal Information")));
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/profile")).andExpect(status().is3xxRedirection());
    }

    @Test
    void updatingProfileSavesNewName() throws Exception {
        mvc.perform(post("/profile").with(user("jhon@test.com").roles("CUSTOMER")).with(csrf())
                .param("userName", "John")
                .param("userSurname", "Smith")
                .param("userPhone", "05001112233"))
                .andExpect(redirectedUrl("/profile?updated"));

        UsersModel saved = userRepository.findByUserEmail("jhon@test.com").orElseThrow();
        assertThat(saved.getUserName()).isEqualTo("John");
        assertThat(saved.getUserSurname()).isEqualTo("Smith");
        assertThat(saved.getUserPhone()).isEqualTo("05001112233");
    }

    @Test
    void blankNameIsRejected() throws Exception {
        mvc.perform(post("/profile").with(user("jhon@test.com").roles("CUSTOMER")).with(csrf())
                .param("userName", "")
                .param("userSurname", "Smith"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Name cannot be blank")));
    }

    @Test
    void mismatchedPasswordsAreRejected() throws Exception {
        mvc.perform(post("/profile/password").with(user("jhon@test.com").roles("CUSTOMER")).with(csrf())
                .param("currentPassword", "current123")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "different123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Passwords do not match")));
    }

    @Test
    void passwordIsChangedWithCorrectCurrentPassword() throws Exception {
        mvc.perform(post("/profile/password").with(user("jhon@test.com").roles("CUSTOMER")).with(csrf())
                .param("currentPassword", "current123")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "newpass123"))
                .andExpect(redirectedUrl("/profile?passwordChanged"));

        UsersModel saved = userRepository.findByUserEmail("jhon@test.com").orElseThrow();
        assertThat(passwordEncoder.matches("newpass123", saved.getUserPassword())).isTrue();
    }
}
