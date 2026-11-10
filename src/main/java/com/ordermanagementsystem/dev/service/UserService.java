package com.ordermanagementsystem.dev.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setUserName(userData.getUserName());
        user.setUserPassword(passwordEncoder.encode(userData.getUserPassword()));
        user.setUserSurname(userData.getUserSurname());
        user.setUserPhone(userData.getUserPhone());
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

        return userRepository.save(user);
    }

    // Owner'ın şirketi (ürün eklerken ürüne bağlanır)
    public CompanyModel getCompany(String userEmail) {
        return userRepository.findByUserEmail(userEmail).map(UsersModel::getCompany).orElse(null);
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
