package com.example.calendar4;

/**
 * Edit card for Form = Task. Keeps all project fields but with "Задача" wording.
 * "Заявка на Автоматизацию" becomes "В Проекте" (modal picker over all Form=Project records,
 * stored in RequestName / RequestUNID).
 */
public class TaskActivity extends BaseCalPlanEditActivity {

    @Override
    protected String getFormType() { return "Task"; }
    @Override
    protected String getStartDateLabel() { return "Дата старта задачи:"; }
    @Override
    protected String getBodyTextLabel() { return "Описание задачи:"; }
    @Override
    protected String getRequestNameLabel() { return "В Проекте:"; }
    @Override
    protected String getAuthorLabel() { return "Автор задачи:"; }
    @Override
    protected String getEndDateLabel() { return "Дата завершения задачи (факт):"; }
    @Override
    protected String getHoldDateLabel() { return "Дата откладывания задачи:"; }
    @Override
    protected boolean isRequestPicker() { return true; }
    @Override
    protected boolean allowFormChange() { return false; }
}