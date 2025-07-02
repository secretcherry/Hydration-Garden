package com.example.hydrationgarden;

import android.app.Application;
import com.example.hydrationgarden.utils.ThemeHelper;

public class HydrationGardenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme on app startup
        ThemeHelper.applyTheme(this);
    }
}
