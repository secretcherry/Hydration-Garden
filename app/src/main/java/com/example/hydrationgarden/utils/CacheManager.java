package com.example.hydrationgarden.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class CacheManager {
    private static final String PREFS_NAME = "hydration_cache";
    private static final String KEY_TODAY_INTAKE = "today_intake";
    private static final String KEY_CACHE_DATE = "cache_date";
    private static final String KEY_CACHE_TIME = "cache_time";
    private static final long CACHE_DURATION = 60000; // 1 minuta

    private SharedPreferences prefs;
    private static CacheManager instance;

    private CacheManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized CacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new CacheManager(context.getApplicationContext());
        }
        return instance;
    }

    public void cacheTodayIntake(int intake, String date) {
        prefs.edit()
                .putInt(KEY_TODAY_INTAKE, intake)
                .putString(KEY_CACHE_DATE, date)
                .putLong(KEY_CACHE_TIME, System.currentTimeMillis())
                .apply();

        Log.d("CacheManager", "Cached today intake: " + intake + "ml for date: " + date);
    }

    public int getCachedTodayIntake(String date) {
        String cachedDate = prefs.getString(KEY_CACHE_DATE, "");
        long cacheTime = prefs.getLong(KEY_CACHE_TIME, 0);
        long currentTime = System.currentTimeMillis();

        if (date.equals(cachedDate) && (currentTime - cacheTime) < CACHE_DURATION) {
            int cachedIntake = prefs.getInt(KEY_TODAY_INTAKE, -1);
            Log.d("CacheManager", "Returning cached intake: " + cachedIntake + "ml");
            return cachedIntake;
        }

        Log.d("CacheManager", "Cache expired or invalid");
        return -1; // Cache invalid
    }

    public void invalidateCache() {
        prefs.edit().clear().apply();
        Log.d("CacheManager", "Cache cleared");
    }
}
