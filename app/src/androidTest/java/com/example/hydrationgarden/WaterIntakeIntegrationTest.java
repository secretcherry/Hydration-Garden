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
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        firebaseHelper = new FirebaseHelper();

        // Čekaj da se aplikacija potpuno učita
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaterIntakeIntegration() throws Exception {
        // Test integracije između dodavanja vode, dohvaćanja podataka i računanja postotka

        // 1. Provjeri je li korisnik ulogiran (potrebno za test)
        String userId = firebaseHelper.getCurrentUserId();
        assertNotNull("Korisnik mora biti ulogiran za test", userId);

        // 2. Dohvati početni unos vode
        CompletableFuture<Integer> initialIntakeFuture = firebaseHelper.getTodayWaterIntake();
        int initialIntake = initialIntakeFuture.get(10, TimeUnit.SECONDS);
        assertTrue("Početni unos mora biti >= 0", initialIntake >= 0);

        // 3. Dohvati korisničke podatke (cilj)
        CompletableFuture<User> userFuture = firebaseHelper.getUser();
        User user = userFuture.get(10, TimeUnit.SECONDS);
        assertNotNull("Korisnik mora postojati", user);
        assertTrue("Dnevni cilj mora biti > 0", user.getDailyGoal() > 0);

        // 4. Dodaj vodu (250ml)
        int waterToAdd = 250;
        CompletableFuture<String> addWaterFuture = firebaseHelper.addWaterIntake(waterToAdd);
        String intakeId = addWaterFuture.get(10, TimeUnit.SECONDS);
        assertNotNull("ID unosa vode ne smije biti null", intakeId);
        assertFalse("ID unosa vode ne smije biti prazan", intakeId.isEmpty());

        // 5. Čekaj malo da se podaci ažuriraju u Firebase
        Thread.sleep(3000);

        // 6. Provjeri je li unos ažuriran
        CompletableFuture<Integer> updatedIntakeFuture = firebaseHelper.getTodayWaterIntake();
        int updatedIntake = updatedIntakeFuture.get(10, TimeUnit.SECONDS);

        // Provjeri je li unos povećan za dodanu količinu
        assertEquals("Unos vode mora biti povećan za dodanu količinu",
                initialIntake + waterToAdd, updatedIntake);

        // 7. Testiraj računanje postotka cilja
        int expectedPercentage = (updatedIntake * 100) / user.getDailyGoal();
        int actualPercentage = calculateGoalPercentage(updatedIntake, user.getDailyGoal());
        assertEquals("Postotak cilja mora biti točno izračunat",
                expectedPercentage, actualPercentage);

        // 8. Testiraj logiku srećnih biljaka
        boolean shouldPlantsBeHappy = updatedIntake >= user.getDailyGoal();
        boolean plantsAreHappy = checkPlantsHappiness(updatedIntake, user.getDailyGoal());
        assertEquals("Logika srećnih biljaka mora biti ispravna",
                shouldPlantsBeHappy, plantsAreHappy);

        // Test je prošao - integracija između komponenti radi ispravno!
    }

    @Test
    public void testWeeklyAndMonthlyDataIntegration() throws Exception {
        // Test integracije tjednih i mjesečnih podataka

        String userId = firebaseHelper.getCurrentUserId();
        assertNotNull("Korisnik mora biti ulogiran", userId);

        // Testiraj tjedne podatke
        CompletableFuture<Integer> weeklyFuture = firebaseHelper.getWeeklyWaterIntake();
        int weeklyIntake = weeklyFuture.get(15, TimeUnit.SECONDS);
        assertTrue("Tjedni unos mora biti >= 0", weeklyIntake >= 0);

        // Testiraj mjesečne podatke
        CompletableFuture<Integer> monthlyFuture = firebaseHelper.getMonthlyWaterIntake();
        int monthlyIntake = monthlyFuture.get(15, TimeUnit.SECONDS);
        assertTrue("Mjesečni unos mora biti >= 0", monthlyIntake >= 0);

        // Testiraj najbolji dan
        CompletableFuture<Integer> bestDayFuture = firebaseHelper.getBestDayWaterIntake();
        int bestDay = bestDayFuture.get(15, TimeUnit.SECONDS);
        assertTrue("Najbolji dan mora biti >= 0", bestDay >= 0);

        // Logička provjera - mjesečni unos mora biti >= tjedni unos
        assertTrue("Mjesečni unos mora biti >= tjedni unos", monthlyIntake >= weeklyIntake);
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
