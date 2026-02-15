package tn.esprit.farmai.test;

import org.junit.jupiter.api.*;
import tn.esprit.farmai.models.Role;
import tn.esprit.farmai.models.User;
import tn.esprit.farmai.services.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServicesTest {

    private static UserService userService;
    private User testUser;

    @BeforeAll
    public static void setup() {
        userService = new UserService();
    }

    @AfterEach
    public void cleanup() throws SQLException {
        if (testUser != null && testUser.getIdUser() > 0) {
            userService.deleteById(testUser.getIdUser());
        }
    }

    @Test
    @Order(1)
    public void testAjouter() throws SQLException {
        // Create new user
        testUser = new User("TestNom", "TestPrenom", "test.add@example.com", "Password123!", "00000001", "Tunis",
                "71111111", "image.png", Role.AGRICOLE);

        // Add User
        userService.insertOne(testUser);

        // Verify ID is generated
        assertTrue(testUser.getIdUser() > 0, "User ID should be generated");

        // Verify afficher() contains the added user
        List<User> users = userService.selectAll();
        boolean foundById = users.stream().anyMatch(u -> u.getIdUser() == testUser.getIdUser());

        assertTrue(foundById, "User should be present in the list");
        assertFalse(users.isEmpty(), "User list should not be empty");
    }

    @Test
    @Order(2)
    public void testModifier() throws SQLException {
        // Setup: Create a user to modify
        testUser = new User("ToModify", "User", "modify.usertest@example.com", "Password123!", "22222222", "Sousse",
                "72222222", "img.png", Role.AGRICOLE);
        userService.insertOne(testUser);

        // Modify
        testUser.setNom("ModifiedNom");
        testUser.setPrenom("ModifiedPrenom");
        userService.updateOne(testUser);

        // Verify using findById or checking the list
        Optional<User> updatedUserOpt = userService.findById(testUser.getIdUser());
        assertTrue(updatedUserOpt.isPresent(), "Updated user should exist");

        User updatedUser = updatedUserOpt.get();
        assertEquals("ModifiedNom", updatedUser.getNom(), "Last name should be updated");
        assertEquals("ModifiedPrenom", updatedUser.getPrenom(), "First name should be updated");
    }

    @Test
    @Order(3)
    public void testSupprimer() throws SQLException {
        // Setup: Create a user to delete
        testUser = new User("ToDelete", "User", "delete.usertest@example.com", "Password123!", "00000001", "Sfax",
                "73333333", "img.png", Role.AGRICOLE);
        userService.insertOne(testUser);
        int userId = testUser.getIdUser();

        // Delete
        userService.deleteById(userId);

        // Verify deletion
        Optional<User> deletedUser = userService.findById(userId);
        assertFalse(deletedUser.isPresent(), "User should be deleted");

        // Reset testUser so @AfterEach doesn't try to delete it again (handling SQL
        // errors gracefully)
        testUser = null;
    }
}
