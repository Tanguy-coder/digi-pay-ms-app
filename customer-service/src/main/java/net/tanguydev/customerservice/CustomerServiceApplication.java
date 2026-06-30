package net.tanguydev.customerservice;

import net.tanguydev.customerservice.Domain.Entities.DomainCustomer;
import net.tanguydev.customerservice.Domain.Enums.AccountStatus;
import net.tanguydev.customerservice.Domain.Enums.KycStatus;
import net.tanguydev.customerservice.Domain.Enums.TierLevel;
import net.tanguydev.customerservice.Domain.Ports.CustomerServiceInterface;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(CustomerServiceInterface customerService) {
        return args -> {
            for (int i = 0; i < 3; i++) {
                DomainCustomer customer = new DomainCustomer();
                customer.setFirstName("Firstname" + i);
                customer.setLastName("Lastname" + i);
                customer.setEmail("customer" + i + "@mail.com");
                customer.setPhoneNumber("+22877000000" + i);
                customer.setNationality("TGO");
                customer.setAddressLine1("Street " + i);
                customer.setCity("Dakar");
                customer.setCountry("Senegal");
                customer.setStatus(AccountStatus.PENDING);
                customer.setKycStatus(KycStatus.NOT_SUBMITTED);
                customer.setTierLevel(TierLevel.BASIC);
                customer.setPreferredCurrency("XOF");
                customer.setIsEmailVerified(false);
                customer.setIsPhoneVerified(false);
                customerService.save(customer);
            }
        };
    }

}


