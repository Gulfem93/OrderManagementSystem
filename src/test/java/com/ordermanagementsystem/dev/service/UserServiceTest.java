package com.ordermanagementsystem.dev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.model.CompanyModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.CompanyRepository;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UsersData form(UserRole role, String companyName) {
        UsersData data = new UsersData();
        data.setUserEmail("  new@test.com ");
        data.setUserPassword("secret123");
        data.setUserName("Ada");
        data.setUserSurname("Lovelace");
        data.setUserRole(role);
        data.setCompanyName(companyName);
        return data;
    }

    @Test
    void customerRegistrationHashesPasswordAndHasNoCompany() {
        when(passwordEncoder.encode("secret123")).thenReturn("HASH");
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UsersModel user = userService.register(form(UserRole.CUSTOMER, null));

        assertThat(user.getUserEmail()).isEqualTo("new@test.com");
        assertThat(user.getUserPassword()).isEqualTo("HASH");
        assertThat(user.getUserRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.getUserActive()).isTrue();
        assertThat(user.getCompany()).isNull();
    }

    @Test
    void ownerRegistrationCreatesCompany() {
        when(companyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UsersModel user = userService.register(form(UserRole.OWNER, " Acme "));

        assertThat(user.getUserRole()).isEqualTo(UserRole.OWNER);
        assertThat(user.getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void adminRoleCannotBeChosenAtSignup() {
        when(userRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        UsersModel user = userService.register(form(UserRole.ADMIN, null));

        assertThat(user.getUserRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void duplicateCompanyNameIsRejected() {
        when(companyRepository.existsByCompanyNameIgnoreCase("Acme")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(form(UserRole.OWNER, "Acme")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(companyRepository, never()).save(any(CompanyModel.class));
    }

    @Test
    void duplicateEmailIsRejected() {
        when(userRepository.existsByUserEmail("new@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(form(UserRole.CUSTOMER, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void concurrentDuplicateEmailBecomesFriendlyError() {
        // İki kayıt aynı anda gelirse veritabanının unique kısıtı yakalar
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_user_email"));

        assertThatThrownBy(() -> userService.register(form(UserRole.CUSTOMER, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A user with this email address is already registered.");
    }
}
