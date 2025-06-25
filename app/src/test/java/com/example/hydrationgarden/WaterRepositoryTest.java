package com.example.hydrationgarden;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.example.hydrationgarden.repositories.WaterRepository;
import com.example.hydrationgarden.utils.FirebaseHelper;
import java.util.concurrent.CompletableFuture;

public class WaterRepositoryTest {

    @Mock
    private FirebaseHelper mockFirebaseHelper;

    private WaterRepository waterRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        waterRepository = new WaterRepository();
    }

    @Test
    public void testAddWaterIntake_ValidAmount_ReturnsSuccess() {
        // Arrange
        int waterAmount = 500;
        String expectedId = "test-id-123";
        CompletableFuture<String> mockFuture = CompletableFuture.completedFuture(expectedId);

        // Mock the FirebaseHelper behavior
        when(mockFirebaseHelper.addWaterIntake(waterAmount)).thenReturn(mockFuture);

        // Act
        CompletableFuture<String> result = waterRepository.addWaterIntake(waterAmount);

        // Assert
        assertNotNull("Result should not be null", result);

        // Verify the result
        result.thenAccept(id -> {
            assertEquals("Should return expected ID", expectedId, id);
        });
    }

    @Test
    public void testGetTodayWaterIntake_ReturnsCorrectAmount() {
        // Arrange
        int expectedAmount = 1250;
        CompletableFuture<Integer> mockFuture = CompletableFuture.completedFuture(expectedAmount);

        when(mockFirebaseHelper.getTodayWaterIntake()).thenReturn(mockFuture);

        // Act
        CompletableFuture<Integer> result = waterRepository.getTodayWaterIntake();

        // Assert
        assertNotNull("Result should not be null", result);

        result.thenAccept(amount -> {
            assertEquals("Should return expected amount", expectedAmount, amount.intValue());
        });
    }

    @Test
    public void testGetWeeklyAverage_ReturnsValidAverage() {
        // Act
        CompletableFuture<Integer> result = waterRepository.getWeeklyAverage();

        // Assert
        assertNotNull("Result should not be null", result);

        result.thenAccept(average -> {
            assertTrue("Weekly average should be positive", average > 0);
            assertTrue("Weekly average should be reasonable", average <= 3000);
        });
    }

    @Test
    public void testAddWaterIntake_ZeroAmount_ShouldHandleGracefully() {
        // Arrange
        int waterAmount = 0;

        // Act & Assert
        assertNotNull("Should handle zero amount gracefully",
                waterRepository.addWaterIntake(waterAmount));
    }

    @Test
    public void testAddWaterIntake_NegativeAmount_ShouldHandleGracefully() {
        // Arrange
        int waterAmount = -100;

        // Act & Assert
        assertNotNull("Should handle negative amount gracefully",
                waterRepository.addWaterIntake(waterAmount));
    }
}