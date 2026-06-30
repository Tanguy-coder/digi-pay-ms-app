package net.tanguydev.customerservice.Infrastructure.Repositories;

import net.tanguydev.customerservice.Infrastructure.Models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<Customer, Long> {
}
