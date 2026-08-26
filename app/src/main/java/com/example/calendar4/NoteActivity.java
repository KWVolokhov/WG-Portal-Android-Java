package com.example.calendar4;

/**
 * Edit card for Form = Note.
 * Fields: Form, Состояние, Название, Дата записи, Текст Заметки, Комментарий, Ключевые слова.
 * Non-editable: Дата создания, Дата последнего обновления, Автор.
 */
public class NoteActivity extends BaseCalPlanEditActivity {

    @Override
    protected String getFormType() { return "Note"; }
    @Override
    protected String getStartDateLabel() { return "Дата записи:"; }
    @Override
    protected String getBodyTextLabel() { return "Текст Заметки:"; }
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