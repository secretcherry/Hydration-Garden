package com.example.hydrationgarden;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.example.hydrationgarden.models.Plant;
import com.example.hydrationgarden.models.PlantType;
import com.example.hydrationgarden.models.User;
import com.example.hydrationgarden.models.WaterIntake;
import com.example.hydrationgarden.utils.DateUtils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testUserCreation() {
        // Test User model creation
        User user = new User("test-uid", "test@example.com", "Test User");

        assertEquals("test-uid", user.getUid());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getName());
        assertEquals(2000, user.getDailyGoal()); // Default goal
        assertTrue(user.getCreatedAt() > 0);
    }

    @Test
    public void testPlantCreation() {
        // Test Plant model creation
        Plant plant = new Plant("1", "Alojka", PlantType.ALOE);

        assertEquals("1", plant.getId());
        assertEquals("Alojka", plant.getName());
        assertEquals(PlantType.ALOE, plant.getType());
        assertTrue(plant.isHappy()); // Default is happy
        assertNotNull(plant.getCurrentMessage());
        assertTrue(plant.getCurrentImageResId() > 0);
    }

    @Test
    public void testPlantMoodChange() {
        // Test plant mood changes
        Plant plant = new Plant("1", "Test Plant", PlantType.CACTUS);

        // Initially happy
        assertTrue(plant.isHappy());
        String happyMessage = plant.getCurrentMessage();

        // Make sad
        plant.setHappy(false);
        assertFalse(plant.isHappy());
        String sadMessage = plant.getCurrentMessage();

        // Messages should be different
        assertNotEquals(happyMessage, sadMessage);
    }

    @Test
    public void testWaterIntakeCreation() {
        // Test WaterIntake model creation
        WaterIntake intake = new WaterIntake("user-123", 500, "2025-01-15");

        assertEquals("user-123", intake.getUserId());
        assertEquals(500, intake.getAmount());
        assertEquals("2025-01-15", intake.getDate());
        assertTrue(intake.getTimestamp() > 0);
    }

    @Test
    public void testPlantTypeDisplayNames() {
        // Test PlantType enum display names
        assertEquals("Aloe Vera", PlantType.ALOE.getDisplayName());
        assertEquals("Kaktus", PlantType.CACTUS.getDisplayName());
        assertEquals("Suncokret", PlantType.SUNFLOWER.getDisplayName());
        assertEquals("Ruža", PlantType.ROSE.getDisplayName());
        assertEquals("Orhideja", PlantType.ORCHID.getDisplayName());
    }

    @Test
    public void testDateUtilsCurrentDate() {
        // Test DateUtils current date functionality
        String currentDate = DateUtils.getCurrentDate();

        assertNotNull("Current date should not be null", currentDate);
        assertTrue("Date should be in yyyy-MM-dd format",
                currentDate.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testDateUtilsGreeting() {
        // Test DateUtils greeting functionality
        String greeting = DateUtils.getGreeting();

        assertNotNull("Greeting should not be null", greeting);
        assertTrue("Greeting should be valid",
                greeting.equals("Dobro jutro") ||
                        greeting.equals("Dobar dan") ||
                        greeting.equals("Dobra večer"));
    }

    @Test
    public void testUserDailyGoalValidation() {
        // Test user daily goal validation
        User user = new User("test-uid", "test@example.com", "Test User", 3000);

        assertEquals(3000, user.getDailyGoal());

        // Test setting new goal
        user.setDailyGoal(2500);
        assertEquals(2500, user.getDailyGoal());
    }

    @Test
    public void testWaterIntakeValidAmounts() {
        // Test various water intake amounts
        WaterIntake smallIntake = new WaterIntake("user-1", 250, "2025-01-15");
        WaterIntake mediumIntake = new WaterIntake("user-1", 500, "2025-01-15");
        WaterIntake largeIntake = new WaterIntake("user-1", 1000, "2025-01-15");

        assertEquals(250, smallIntake.getAmount());
        assertEquals(500, mediumIntake.getAmount());
        assertEquals(1000, largeIntake.getAmount());

        // All should have valid timestamps
        assertTrue(smallIntake.getTimestamp() > 0);
        assertTrue(mediumIntake.getTimestamp() > 0);
        assertTrue(largeIntake.getTimestamp() > 0);
    }
}