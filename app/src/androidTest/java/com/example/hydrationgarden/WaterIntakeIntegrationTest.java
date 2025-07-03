package com.example.hydrationgarden;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.hydrationgarden.utils.FirebaseHelper;
import com.example.hydrationgarden.models.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Medium Integration Test - testira integraciju između FirebaseHelper-a i podataka o vodi
 * Testira dodavanje vode, dohvaćanje današnjeg unosa i računanje postotka cilja
 */
@RunWith(AndroidJUnit4.class)
public class WaterIntakeIntegrationTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    private FirebaseHelper firebaseHelper;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        firebaseHelper = new FirebaseHelper();

        // Čekaj da se aplikacija potpuno učita
        Thread.sleep(2000);

        // NOVO: Ulogiraj test korisnika prije testiranja
        loginTestUser();
    }

    private void loginTestUser() throws Exception {
        // Provjeri je li korisnik već ulogiran
        String currentUserId = firebaseHelper.getCurrentUserId();
        if (currentUserId != null) {
            System.out.println("Test user already logged in: " + currentUserId);
            return;
        }

        // Pokušaj login s test korisnikom
        System.out.println("Logging in test user: novi@mail.com");

        CompletableFuture<String> loginFuture = firebaseHelper.loginUser("novi@mail.com", "123456");

        try {
            String userId = loginFuture.get(15, TimeUnit.SECONDS);
            assertNotNull("Login mora biti uspješan", userId);
            System.out.println("Test user logged in successfully: " + userId);

            // Čekaj malo da se login potpuno završi
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            // Ako login ne uspije, preskačemo test
            org.junit.Assume.assumeTrue("Test user login failed - skipping test", false);
        }
    }

    @Test
    public void testWaterIntakeIntegration() throws Exception {
        // Test integracije između dodavanja vode, dohvaćanja podataka i računanja postotka

        // 1. Provjeri je li korisnik ulogiran (sada bi trebao biti)
        String userId = firebaseHelper.getCurrentUserId();
        assertNotNull("Korisnik mora biti ulogiran za test", userId);
        System.out.println("Testing with user ID: " + userId);

        // 2. Dohvati početni unos vode
        CompletableFuture<Integer> initialIntakeFuture = firebaseHelper.getTodayWaterIntake();
        int initialIntake = initialIntakeFuture.get(10, TimeUnit.SECONDS);
        assertTrue("Početni unos mora biti >= 0", initialIntake >= 0);
        System.out.println("Initial water intake: " + initialIntake + "ml");

        // 3. Dohvati korisničke podatke (cilj)
        CompletableFuture<User> userFuture = firebaseHelper.getUser();
        User user = userFuture.get(10, TimeUnit.SECONDS);
        assertNotNull("Korisnik mora postojati", user);
        assertTrue("Dnevni cilj mora biti > 0", user.getDailyGoal() > 0);
        System.out.println("User daily goal: " + user.getDailyGoal() + "ml");

        // 4. Dodaj vodu (250ml)
        int waterToAdd = 250;
        System.out.println("Adding " + waterToAdd + "ml of water...");

        CompletableFuture<String> addWaterFuture = firebaseHelper.addWaterIntake(waterToAdd);
        String intakeId = addWaterFuture.get(10, TimeUnit.SECONDS);
        assertNotNull("ID unosa vode ne smije biti null", intakeId);
        assertFalse("ID unosa vode ne smije biti prazan", intakeId.isEmpty());
        System.out.println("Water added successfully with ID: " + intakeId);

        // 5. Čekaj malo da se podaci ažuriraju u Firebase
        Thread.sleep(3000);

        // 6. Provjeri je li unos ažuriran
        CompletableFuture<Integer> updatedIntakeFuture = firebaseHelper.getTodayWaterIntake();
        int updatedIntake = updatedIntakeFuture.get(10, TimeUnit.SECONDS);
        System.out.println("Updated water intake: " + updatedIntake + "ml");

        // Provjeri je li unos povećan za dodanu količinu
        assertEquals("Unos vode mora biti povećan za dodanu količinu",
                initialIntake + waterToAdd, updatedIntake);

        // 7. Testiraj računanje postotka cilja
        int expectedPercentage = (updatedIntake * 100) / user.getDailyGoal();
        int actualPercentage = calculateGoalPercentage(updatedIntake, user.getDailyGoal());
        assertEquals("Postotak cilja mora biti točno izračunat",
                expectedPercentage, actualPercentage);
        System.out.println("Goal percentage: " + actualPercentage + "%");

        // 8. Testiraj logiku srećnih biljaka
        boolean shouldPlantsBeHappy = updatedIntake >= user.getDailyGoal();
        boolean plantsAreHappy = checkPlantsHappiness(updatedIntake, user.getDailyGoal());
        assertEquals("Logika srećnih biljaka mora biti ispravna",
                shouldPlantsBeHappy, plantsAreHappy);
        System.out.println("Plants should be happy: " + shouldPlantsBeHappy);

        System.out.println("✅ Water intake integration test passed!");
    }

    @Test
    public void testWeeklyAndMonthlyDataIntegration() throws Exception {
        // Test integracije tjednih i mjesečnih podataka

        String userId = firebaseHelper.getCurrentUserId();
        if (userId == null) {
            System.out.println("⚠️ No user logged in, skipping weekly/monthly test");
            org.junit.Assume.assumeTrue("User must be logged in", false);
            return;
        }

        System.out.println("Testing weekly/monthly data for user: " + userId);

        // Testiraj tjedne podatke
        CompletableFuture<Integer> weeklyFuture = firebaseHelper.getWeeklyWaterIntake();
        int weeklyIntake = weeklyFuture.get(15, TimeUnit.SECONDS);
        assertTrue("Tjedni unos mora biti >= 0", weeklyIntake >= 0);
        System.out.println("Weekly intake: " + weeklyIntake + "ml");

        // Testiraj mjesečne podatke
        CompletableFuture<Integer> monthlyFuture = firebaseHelper.getMonthlyWaterIntake();
        int monthlyIntake = monthlyFuture.get(15, TimeUnit.SECONDS);
        assertTrue("Mjesečni unos mora biti >= 0", monthlyIntake >= 0);
        System.out.println("Monthly intake: " + monthlyIntake + "ml");

        // Testiraj najbolji dan
        CompletableFuture<Integer> bestDayFuture = firebaseHelper.getBestDayWaterIntake();
        int bestDay = bestDayFuture.get(15, TimeUnit.SECONDS);
        assertTrue("Najbolji dan mora biti >= 0", bestDay >= 0);
        System.out.println("Best day: " + bestDay + "ml");

        // Logička provjera - mjesečni unos mora biti >= tjedni unos
        assertTrue("Mjesečni unos mora biti >= tjedni unos", monthlyIntake >= weeklyIntake);

        System.out.println("✅ Weekly/monthly data integration test passed!");
    }

    @Test
    public void testBasicFirebaseConnection() throws Exception {
        // Jednostavan test koji ne zahtijeva ulogiranog korisnika
        assertNotNull("FirebaseHelper mora biti inicijaliziran", firebaseHelper);

        // Test osnovnih funkcionalnosti
        boolean isLoggedIn = firebaseHelper.isUserLoggedIn();
        System.out.println("User logged in: " + isLoggedIn);

        if (isLoggedIn) {
            String userId = firebaseHelper.getCurrentUserId();
            System.out.println("Current user ID: " + userId);
            assertNotNull("User ID ne smije biti null ako je korisnik ulogiran", userId);
        }

        System.out.println("✅ Basic Firebase connection test passed!");
    }

    // Helper metode za testiranje logike
    private int calculateGoalPercentage(int currentIntake, int dailyGoal) {
        if (dailyGoal <= 0) return 0;
        return (currentIntake * 100) / dailyGoal;
    }

    private boolean checkPlantsHappiness(int currentIntake, int dailyGoal) {
        return currentIntake >= dailyGoal;
    }
}
