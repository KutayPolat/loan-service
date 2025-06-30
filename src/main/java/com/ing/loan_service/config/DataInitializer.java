package com.ing.loan_service.config;

import com.ing.loan_service.model.Customer;
import com.ing.loan_service.model.User;
import com.ing.loan_service.repository.CustomerRepository;
import com.ing.loan_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", passwordEncoder.encode("admin"), User.Role.ADMIN);
            userRepository.save(admin);
        }

        // Create sample customer
        if (customerRepository.count() == 0) {
            Customer customer = new Customer("Kutay", "Polat", new BigDecimal("5000000.00"));

            customer = customerRepository.save(customer);

            // Create customer user
            User customerUser = new User("customer1", passwordEncoder.encode("customer123"),
                    User.Role.CUSTOMER, customer);

            userRepository.save(customerUser);
        }
    }
}
