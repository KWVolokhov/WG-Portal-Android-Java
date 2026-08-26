package com.example.calendar4;

/**
 * Edit card for Form = Remember.
 * Fields: Form, Состояние, Название, Дата напоминания, Текст Напоминания, Комментарий, Ключевые слова.
 * Non-editable: Дата создания, Дата последнего обновления, Автор.
 */
public class RememberActivity extends BaseCalPlanEditActivity {

    @Override
    protected String getFormType() { return "Remember"; }
    @Override
    protected String getStartDateLabel() { return "Дата напоминания:"; }
    @Override
    protected String getBodyTextLabel() { return "Текст Напоминания:"; }
    @Override
    protected String getAuthorLabel() { return "Автор:"; }
    @Override
    protected boolean showMainSystem() { return false; }
    @Override
    protected boolean showPriority() { return false; }
    @Override
    protected boolean showRequestName() { return false; }
    @Override
    protected boolean showAnalitikExector() { return false; }
    @Override
    protected boolean showInstallOrder() { return false; }
    @Override
    protected boolean showLastUpdatedBy() { return false; }
    @Override
    protected boolean showEndDate() { return false; }
    @Override
    protected boolean showHoldDate() { return false; }
    @Override
    protected boolean allowFormChange() { return false; }
}