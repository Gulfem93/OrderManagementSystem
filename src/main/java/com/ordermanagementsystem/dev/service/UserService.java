package com.ordermanagementsystem.dev.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.ProfileData;
import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.model.CompanyModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.CompanyRepository;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isEmailTaken(String email) {
        return email != null && userRepository.existsByUserEmail(email.trim());
    }

    public boolean isCompanyNameTaken(String companyName) {
        return companyName != null && companyRepository.existsByCompanyNameIgnoreCase(companyName.trim());
    }

    // Kayıt: OWNER ise şirketi de oluşturur. Formdan sadece OWNER / CUSTOMER seçilebilir;
    // ADMIN rolü yalnızca admin panelinden verilir.
    @Transactional
    public UsersModel register(UsersData userData) {
        if (isEmailTaken(userData.getUserEmail())) {
            throw new IllegalArgumentException("A user with this email address is already registered.");
        }

        UserRole role = userData.getUserRole() != null && userData.getUserRole().isSignupRole()
                ? userData.getUserRole()
                : UserRole.CUSTOMER;

        UsersModel user = new UsersModel();
        user.setUserEmail(userData.getUserEmail().trim());
        user.setUserName(userData.getUserName().trim());
        user.setUserPassword(passwordEncoder.encode(userData.getUserPassword()));
        user.setUserSurname(userData.getUserSurname().trim());
        user.setUserPhone(userData.getUserPhone() == null || userData.getUserPhone().isBlank()
                ? null : userData.getUserPhone().trim());
        user.setUserRole(role);
        user.setUserActive(true);

        if (role == UserRole.OWNER) {
            String companyName = userData.getCompanyName().trim();
            // Aynı adla ikinci bir owner, başka şirketin siparişlerini göremesin
            if (isCompanyNameTaken(companyName)) {
                throw new IllegalArgumentException("A company with this name is already registered.");
            }
            CompanyModel company = new CompanyModel();
            company.setCompanyName(companyName);
            user.setCompany(companyRepository.save(company));
        }

        try {
            // saveAndFlush: benzersizlik ihlali burada yakalanır, controller'a anlaşılır mesaj gider
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A user with this email address is already registered.");
        }
    }

    // Owner'ın şirketi (ürün eklerken ürüne bağlanır)
    public CompanyModel getCompany(String userEmail) {
        return userRepository.findByUserEmail(userEmail).map(UsersModel::getCompany).orElse(null);
    }

    // Profile formu: mevcut değerlerle doldurulur
    public ProfileData getProfileData(String userEmail) {
        UsersModel user = findUser(userEmail);
        ProfileData data = new ProfileData();
        data.setUserName(user.getUserName());
        data.setUserSurname(user.getUserSurname());
        data.setUserPhone(user.getUserPhone());
        return data;
    }

    // Profile: ad, soyad ve telefon güncellenir (e-posta, rol, şirket değişmez)
    @Transactional
    public void updateProfile(String userEmail, ProfileData data) {
        UsersModel user = findUser(userEmail);
        user.setUserName(data.getUserName().trim());
        user.setUserSurname(data.getUserSurname().trim());
        String phone = data.getUserPhone() == null ? null : data.getUserPhone().trim();
        user.setUserPhone(phone == null || phone.isEmpty() ? null : phone);
    }

    // Profile: şifre değiştirme; mevcut şifre doğru değilse değişmez
    @Transactional
    public void changePassword(String userEmail, String currentPassword, String newPassword) {
        UsersModel user = findUser(userEmail);
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one.");
        }
        user.setUserPassword(passwordEncoder.encode(newPassword));
    }

    private UsersModel findUser(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    public UsersData getUserData(String userEmail) {
        UsersData userData = new UsersData();
        userRepository.findByUserEmail(userEmail).ifPresent(user -> {
            userData.setUserName(user.getUserName());
            userData.setUserSurname(user.getUserSurname());
            userData.setUserEmail(user.getUserEmail());
            userData.setUserPhone(user.getUserPhone());
            userData.setUserRole(user.getUserRole());
            userData.setUserActive(user.getUserActive());
            userData.setCompanyId(user.getCompanyId());
            userData.setCompanyName(user.getCompanyName());
        });
        return userData;
    }
}
