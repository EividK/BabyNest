package com.assignment_two_starter;

import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@EnableScheduling
public class AssignmentTwoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssignmentTwoApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner testCustomer(CustomerRepository customerRepository) {
//        return args -> {
//            Optional<Customer> opt = customerRepository.findByEmail("aoife_murphy78@gmail.com");
//            if(opt.isPresent()){
//                Customer c = opt.get();
//                System.out.println("Loaded customer: " + c);
//                System.out.println("Roles: " + c.getRoles());
//            } else {
//                System.out.println("Customer not found");
//            }
//        };
//    }
//
//    @Bean
//    public CommandLineRunner testRoles(CustomerRepository customerRepository) {
//        return args -> {
//            List<Object[]> roles = customerRepository.findRolesByUserId(2);
//            for (Object[] role : roles) {
//                System.out.println("Role id: " + role[0] + ", name: " + role[1]);
//            }
//        };
//    }

}
