package com.example.calendar4;

/**
 * Shared edit card for ALL health forms (Task 40): HealthEat / HealthDrink /
 * HealthSport / HealthStress / HealthJoy. The Form comes from the "healthForm"
 * intent extra (needed for records created "from scratch"); when the card is
 * opened for an existing record the Form is taken from the record itself.
 *
 * The three legacy subclasses HealthEatActivity / HealthDrinkActivity /
 * HealthSportActivity extend this class and only fix their Form value.
 */
public class HealthEditActivity extends BaseCalPlanEditActivity {

    @Override
    protected String getFormType() {
        try {
            String form = getIntent().getStringExtra("healthForm");
            if (form != null && !form.isEmpty()) return form;
        } catch (Exception e) {
            // ignore
        }
        return "HealthEat";
    }

    @Override
    protected String getBodyTextLabel() { return "Расшифровка:"; }
    @Override
    protected String getAuthorLabel() { return "Автор:"; }
    @Override
    protected boolean showStatus() { return false; }
    @Override
    protected boolean showMainSystem() { return false; }
    @Override
    protected boolean showPriority() { return false; }
    @Override
    protected boolean showStartDate() { return false; }
    @Override
    protected boolean showRequestName() { return false; }
    @Override
    protected boolean showAnalitikExector() { return false; }
    @Override
    protected boolean showInstallOrder() { return false; }
    @Override
    protected boolean showKeyWords() { return false; }
    @Override
    protected boolean showLastUpdatedBy() { return false; }
    @Override
    protected boolean showEndDate() { return false; }
    @Override
    protected boolean showHoldDate() { return false; }
    @Override
    protected boolean allowFormChange() { return false; }

    // Task 41: on the Health edit card the new number fields are visible
    @Override
    protected boolean showHealthNumbers() { return true; }
}