package com.assignment_two_starter.service;


import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.model.Role;
import com.assignment_two_starter.repository.CustomerRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService  implements UserDetailsService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Autowired
    private CustomerRepository customerRepository;

    /*
    This method loads a user by their email, retrieves their roles from the database,
    converts them into authorities, and returns a UserDetails object with the user’s
    credentials and roles for Spring Security authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        //retrieve a customer account from the DB by email address
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));


        // Debug: print the loaded customer
        System.out.println("Loaded customer: " + customer);

        Set<Role> roles = customer.getRoles();

        // Debug: print the raw roles from the database
        System.out.println("Customer roles from DB: " + roles);

        /*
        Get a list of all the roles associated with the Customer and convert
        them into a list of GrantedAuthority objects.
        If the authenticated user has two roles, then there will be two GrantedAuthority objects
        in this list etc..
         */
        List<GrantedAuthority> authorities = customer.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        //Log the authorities to the console to verify they are being assigned
        System.out.println("Granted Authorities: " + authorities);

        //Create and return a new instance of User, this is a class provided by Spring
        //Security that implements the UserDetails interface
        return new org.springframework.security.core.userdetails.User(
                customer.getEmail(), //the username in our app
                customer.getPassword(), //the hashed pwd
                //four boolean values for user account status to indicate whether the account is
                true, //enabled
                true, //not expired
                true, //credentials are not expired
                true, //account it not locked
                authorities
        );
    }

    /*
    This method converts a set of Role objects into a collection of GrantedAuthority
    objects prefixed with "ROLE_" for use in authorisation.
    */
    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        Set<GrantedAuthority> authorities = new HashSet();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        }
        return authorities;
    }

    /**
     * Finds a customer by their email.
     *
     * @param email The email of the customer.
     * @return The customer entity if found.
     * @throws RuntimeException If no customer is found.
     */
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }


    //methods to support 2fa with GoogleAuthenticator


    /**
     * Generates a secret key for Google Authenticator and saves it to the user.
     *
     * @param email The email of the user.
     * @return The generated secret key.
     */
    public String generateSecretKey(String email) {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setSecretKey(key.getKey()); //Store the secret key in the database
        customerRepository.save(customer);

        return key.getKey();
    }

    /**
     * Generates a Google Authenticator QR Code URL.
     *
     * @param email     The user's email.
     * @param secretKey The generated secret key.
     * @return A URL for scanning into Google Authenticator.
     */
    public String generateQrCodeUrl(String email, String secretKey) {
        return "otpauth://totp/MyApp:" + email + "?secret=" + secretKey + "&issuer=MyApp";
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(Integer id) {
        return customerRepository.findById(id);
    }

    public Customer save(Customer entity) {
        return customerRepository.save(entity);
    }

    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }
}

