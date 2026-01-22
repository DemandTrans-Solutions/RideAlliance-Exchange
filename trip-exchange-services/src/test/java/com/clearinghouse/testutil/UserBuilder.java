package com.clearinghouse.testutil;

import com.clearinghouse.dto.ProviderDTO;
import com.clearinghouse.dto.UserDTO;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.User;
import com.clearinghouse.entity.UserAuthority;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class UserBuilder {
    private User user;
    private UserDTO userDTO;
    
    public UserBuilder() {
        user = new User();
        userDTO = new UserDTO();
        
        // Set default values
        user.setId(1);
        user.setCreatedAt(ZonedDateTime.now());
        user.setUpdatedAt(ZonedDateTime.now());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setIsActive(true);
        // Ensure boolean account flags have safe defaults to avoid NPEs in tests
        user.setAccountLocked(false);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);
        user.setAccountDisabled(false);
        
        // Mirror to DTO
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setActive(user.isActive());
    }
    
    public UserBuilder withId(int id) {
        user.setId(id);
        userDTO.setId(id);
        return this;
    }
    
    public UserBuilder withUsername(String username) {
        user.setUsername(username);
        userDTO.setUsername(username);
        return this;
    }
    
    public UserBuilder withRole(String role) {
        Set<UserAuthority> authorities = new HashSet<>();
        UserAuthority authority = new UserAuthority();
        authority.setAuthority(role);
        authorities.add(authority);
        user.setAuthorities(authorities);
        return this;
    }
    
    public UserBuilder withProvider(Provider provider) {
        user.setProvider(provider);
        ProviderDTO providerDTO = new ProviderDTO();
        providerDTO.setProviderId(provider.getProviderId());
        providerDTO.setProviderName(provider.getProviderName());
        userDTO.setProviderId(provider.getProviderId());
        
        return this;
    }
    
    public UserBuilder asInactiveUser() {
        user.setIsActive(false);
        userDTO.setActive(false);
        return this;
    }
    
    public UserBuilder withPassword(String password) {
        user.setPassword(password);
        userDTO.setPassword(password);
        return this;
    }
    
    public UserBuilder withAccountDisabled(boolean disabled) {
        user.setAccountDisabled(disabled);
        return this;
    }
    
    public UserBuilder withCredentialsExpired(boolean expired) {
        user.setCredentialsExpired(expired);
        return this;
    }
    
    public UserBuilder withAuthorities(Set<UserAuthority> authorities) {
        user.setAuthorities(authorities);
        return this;
    }
    
    public User build() {
        return user;
    }
    
    public UserDTO buildDTO() {
        return userDTO;
    }
}