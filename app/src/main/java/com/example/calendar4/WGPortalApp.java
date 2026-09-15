package com.example.calendar4;

import android.app.Application;

/**
 * Task 134: единая точка старта приложения (зарегистрирована в AndroidManifest).
 * Ставит {@link HardcoreCrashHandler} самым первым - поэтому CrashActivity
 * выскочит при любой необработанной ошибке на любом Activity и в любом потоке,
 * включая падения до того, как onCreate экранов успеет выполниться.
 */
public class WGPortalApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HardcoreCrashHandler.install(this);
    }
}