package com.example.calendar4;

/**
 * Edit card for Form = Project (the main "проект" screen).
 * Uses the shared reworked layout from BaseCalPlanEditActivity with all fields visible.
 */
public class InputCalPlanActivity extends BaseCalPlanEditActivity {

    @Override
    protected String getFormType() {
        return "Project";
    }
}