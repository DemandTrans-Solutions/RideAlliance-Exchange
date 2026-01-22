package com.clearinghouse.dao;

import com.clearinghouse.entity.User;
import com.clearinghouse.entity.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@DataJpaTest
@Import({UserDAO.class, com.clearinghouse.TestConfig.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.jdbc.Sql(scripts = {"classpath:test-schema.sql", "classpath:data.sql"})
class UserDAOTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserDAO userDAO;

    private User mockUser;
    private Provider mockProvider;

    @BeforeEach
    void setUp() {
        com.clearinghouse.entity.AbstractEntity.setTimezone("UTC");
        mockProvider = new Provider();
        mockProvider.setProviderId(1);
        mockProvider.setProviderName("Test Provider");

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setProvider(mockProvider);
        mockUser.setIsActive(true);
    }

    @Test
    void findAllUsers_ShouldReturnUserList() {
        // UserDAO.findAllUsers filters out users with provider.providerId == 1, so mirror that here
        List<User> expectedUsers = entityManager.createQuery("SELECT u FROM User u WHERE u.provider.providerId!=1 order by name", User.class).getResultList();
        List<User> result = userDAO.findAllUsers();
        assertNotNull(result);
        assertEquals(expectedUsers.size(), result.size());
        if (!expectedUsers.isEmpty()) {
            assertEquals(expectedUsers.get(0), result.get(0));
        }
    }

    @Test
    void findUserByUserId_ShouldReturnUser() {
        User result = userDAO.findUserByUserId(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void createUser_ShouldPersistUser() {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setProvider(mockProvider);
        user.setIsActive(true);
        User result = userDAO.createUser(user);
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        User user = userDAO.findUserByUserId(1);
        user.setEmail("updated@example.com");
        User result = userDAO.updateUser(user);
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void deleteUserByUserId_ShouldDeactivateUser() {
        userDAO.deleteUserByUserId(1);
        User user = userDAO.findUserByUserId(1);
        assertFalse(user.isActive());
    }

    @Test
    void findUserByUsername_ShouldReturnUserWithUsername() {
        User result = userDAO.findUserByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void updateUserRole_ShouldUpdateRole() {
        int result = userDAO.updateUserRole(1, "ROLE_USER", "ROLE_ADMIN");
        assertEquals(1, result);
    }

    @Test
    void getUserIdbyProviderId_ShouldReturnUserId() {
        int result = userDAO.getUserIdbyProviderId(1);
        assertEquals(1, result);
    }

    @Test
    void findUserByUsername_WithInvalidUsername_ShouldReturnNull() {
        User result = userDAO.findUserByUsername("nonexistent");
        assertNull(result);
    }
}