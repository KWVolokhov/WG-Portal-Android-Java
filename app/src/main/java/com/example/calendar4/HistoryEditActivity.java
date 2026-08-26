package com.example.calendar4;

/**
 * Edit card for Form = History.
 * Fields: Form, Название, Расшифровка, Комментарий. StartDate hidden but equals creation date.
 * Non-editable: Дата создания, Дата последнего обновления, Автор.
 */
public class HistoryEditActivity extends BaseCalPlanEditActivity {

    @Override
    protected String getFormType() { return "History"; }
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
}