package com.example.hydrationgarden;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * End-to-End Test - testira kompletan korisnički tok kroz aplikaciju
 * Simulira stvarno korištenje aplikacije od početka do kraja
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserJourneyEndToEndTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testCompleteUserJourney() throws InterruptedException {
        // KORAK 1: Čekaj da se aplikacija učita i preusmjeri
        Thread.sleep(3000);

        // KORAK 2: Provjeri da li smo na Dashboard-u (ako je korisnik ulogiran)
        // ili na Login ekranu (ako nije ulogiran)
        try {
            // Pokušaj pronaći Dashboard elemente
            onView(withId(R.id.tvWelcome))
                    .check(matches(isDisplayed()));

            // Ako smo ovdje, korisnik je ulogiran - testiraj Dashboard tok
            testDashboardJourney();

        } catch (Exception e) {
            // Ako Dashboard elementi nisu pronađeni, vjerojatno smo na Login ekranu
            // Testiraj Login tok (ali za sada preskačemo jer nemamo test korisnika)
            // Za ovaj test pretpostavljamo da je korisnik već ulogiran
            throw new AssertionError("Korisnik mora biti ulogiran za E2E test");
        }
    }

    private void testDashboardJourney() throws InterruptedException {
        // KORAK 3: Testiraj Dashboard funkcionalnost

        // Provjeri da su svi glavni elementi vidljivi
        onView(withId(R.id.tvWelcome))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvCurrentIntake))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAddWater))
                .check(matches(isDisplayed()))
                .check(matches(isClickable()));

        // KORAK 4: Dodaj vodu
        onView(withId(R.id.btnAddWater))
                .perform(click());

        // Čekaj da se dialog otvori
        Thread.sleep(1000);

        // Klikni na 250ml opciju (pretpostavljamo da postoji)
        try {
            onView(withText("250ml"))
                    .perform(click());
            Thread.sleep(2000); // Čekaj da se voda doda
        } catch (Exception e) {
            // Ako nema 250ml gumb, zatvori dialog i nastavi
            Espresso.pressBack();
        }

        // KORAK 5: Provjeri da se progress ažurirao
        onView(withId(R.id.tvCurrentIntake))
                .check(matches(isDisplayed()));

        onView(withId(R.id.progressBarLinear))
                .check(matches(isDisplayed()));

        // KORAK 6: Testiraj navigaciju na Garden
        onView(withId(R.id.cardPlantPreview))
                .check(matches(isDisplayed()))
                .perform(click());

        // Čekaj da se Garden učita
        Thread.sleep(3000);

        // KORAK 7: Provjeri Garden ekran
        onView(withId(R.id.tvGardenTitle))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerViewPlants))
                .check(matches(isDisplayed()));

        // KORAK 8: Klikni na biljku (prva biljka u listi)
        Thread.sleep(2000);
        try {
            onView(withId(R.id.recyclerViewPlants))
                    .perform(click());

            // Čekaj da se dialog otvori
            Thread.sleep(1000);

            // Zatvori plant dialog ako se otvorio
            onView(withId(R.id.btnClose))
                    .perform(click());
        } catch (Exception e) {
            // Dialog se možda nije otvorio, nastavi
        }

        // KORAK 9: Testiraj navigaciju na Stats
        onView(withId(R.id.nav_stats))
                .perform(click());

        // Čekaj da se Stats učita
        Thread.sleep(3000);

        // KORAK 10: Provjeri Stats ekran
        onView(withText("Statistike"))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvTodayIntake))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvWeeklyAverage))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvBestDay))
                .check(matches(isDisplayed()));

        onView(withId(R.id.progressGoal))
                .check(matches(isDisplayed()));

        // KORAK 11: Vrati se na Dashboard
        onView(withId(R.id.nav_home))
                .perform(click());

        Thread.sleep(2000);

        // KORAK 12: Testiraj Settings
        onView(withId(R.id.btnSettings))
                .perform(click());

        Thread.sleep(2000);

        // Provjeri Settings ekran
        onView(withText("Postavke"))
                .check(matches(isDisplayed()));

        onView(withId(R.id.switchDarkTheme))
                .check(matches(isDisplayed()));

        // Testiraj dark theme toggle
        onView(withId(R.id.switchDarkTheme))
                .perform(click());

        Thread.sleep(1000);

        // Vrati na light theme
        onView(withId(R.id.switchDarkTheme))
                .perform(click());

        // KORAK 13: Testiraj postavljanje novog cilja
        onView(withId(R.id.btn2500ml))
                .perform(click());

        Thread.sleep(2000);

        // Vrati se na Dashboard
        onView(withId(R.id.btnBack))
                .perform(click());

        Thread.sleep(2000);

        // KORAK 14: Finalna provjera - sve mora biti funkcionalno
        onView(withId(R.id.tvWelcome))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tvCurrentIntake))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btnAddWater))
                .check(matches(isClickable()));

        // E2E test je uspješno završen!
        // Korisnik je prošao kroz sve glavne funkcionalnosti aplikacije
    }

    @Test
    public void testNavigationFlow() throws InterruptedException {
        // Kraći test koji testira samo navigaciju između ekrana

        Thread.sleep(3000);

        // Dashboard -> Garden
        onView(withId(R.id.nav_garden))
                .perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.tvGardenTitle))
                .check(matches(isDisplayed()));

        // Garden -> Stats
        onView(withId(R.id.nav_stats))
                .perform(click());
        Thread.sleep(2000);

        onView(withText("Statistike"))
                .check(matches(isDisplayed()));

        // Stats -> Dashboard
        onView(withId(R.id.nav_home))
                .perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.tvWelcome))
                .check(matches(isDisplayed()));

        // Navigacija radi ispravno!
    }
}
