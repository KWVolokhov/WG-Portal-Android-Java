package com.example.calendar4.help;

/**
 * Константа для HELP-текста на экранах с кнопкой Help.
 * Содержит русские названия экранов для отображения в SpotlightActivity.
 */
public class HelpConstants {

    // Названия экранов для отображения в подсказке "Экран: "
    public static final String MAIN_ACTIVITY = "Календарный план";
	public static final String CONTACTS_ACTIVITY = "Контакты";
	public static final String CALC_ACTIVITY = "Калькулятор";
	public static final String LIVETYPE_ACTIVITY = "Типы жизнидеятельности";
	public static final String PROJECTS_ACTIVITY = "Проекты";
	public static final String SMS_ACTIVITY = "СМС";
	public static final String HELP_ACTIVITY = "Help Справочник";
    public static final String PARAMS_ACTIVITY = "Параметры";
	
    public static final String HISTORY_ACTIVITY = "История";
    public static final String HISTORY_EDIT_ACTIVITY = "Просмотр Истории";
    public static final String HEALTH_ACTIVITY = "Экран Здоровья";

    public static final String DESC_MAIN_ACTIVITY = "Календарный план";
	public static final String DESC_CONTACTS_ACTIVITY = "Контакты";
	public static final String DESC_CALC_ACTIVITY = "Простой прецеззионный Калькулятор, предназначен расчетов формул";
	public static final String DESC_LIVETYPE_ACTIVITY = "Справочник стандартных \"Типов жизнидеятельности\", может быть расширен";
	public static final String DESC_PROJECTS_ACTIVITY = "Ведение Проекты, Заявок на проекты, задач в проектах и отдельно. Все три типа можно было оюъеденить в один, но они отличаются размерами, количеством этапов, проработанностью постановки.\nСтартуют от даты старта, переключение этапа в Выполнено фиксирует дату окончания. От даты старта до даты окончания на основном экране записи показываются как активные на этапах В работе и Тестирование";
	public static final String DESC_SMS_ACTIVITY = "Раздел с сообщениями, из-за постоянных блокировок ГовноМИНЦИФРЫ явно требуется, но вот вопрос через СМС ли, может через HTTPRequest и с шифрованием. В разработке";
	public static final String DESC_HELP_ACTIVITY = "Весь Help по системе пока будет биться по сути на экраны";
    public static final String DESC_PARAMS_ACTIVITY = "Параметры системы для настройки, назабудьте выставить корректно автора и придумать как выставить автора так, чтобы подливались данные с сервера верные";
	
    public static final String HISTORY_ACTIVITY = "История критических изменений в системе";
    public static final String HISTORY_EDIT_ACTIVITY = "Просмотр события Истории";
    public static final String HEALTH_ACTIVITY = "Экран Здоровья это дневной список активностей, которые точно влияють на здоровье - поесть, попить, побегать";
    
    private HelpConstants() {
        // Utility class, prevent instantiation
    }
	
    /** Возвращает русский название экрана для отображения в подсказке */
    public static String getScreenTitle(String className) {
        //String className = getClass().getSimpleName();
        switch (className) {
            case "MainActivity":
                return HelpConstants.MAIN_ACTIVITY;
            case "ContactsActivity":
                return HelpConstants.CONTACTS_ACTIVITY;
            case "CaclActivity":
                return HelpConstants.CALC_ACTIVITY;
            case "LivetypeActivity":
                return HelpConstants.LIVETYPE_ACTIVITY;
            case "LivetypeEditActivity":
                return HelpConstants.LIVETYPE_ACTIVITY;
            case "ProjectsActivity":
                return HelpConstants.PROJECTS_ACTIVITY;

            case "SmsViewActivity":
                return HelpConstants.SMS_ACTIVITY;
            case "SmsActivity":
                return HelpConstants.SMS_ACTIVITY;
            case "SmsChatActivity":
                return HelpConstants.SMS_ACTIVITY;

            case "HelpActivity":
                return HelpConstants.HELP_ACTIVITY;

            case "ParamsActivity":
                return HelpConstants.PARAMS_ACTIVITY;
            case "HistoryActivity":
                return HelpConstants.HISTORY_ACTIVITY;
            case "HistoryEditActivity":
                return HelpConstants.HISTORY_EDIT_ACTIVITY;
            case "HealthActivity":
                return HelpConstants.HEALTH_ACTIVITY;
            default:
                return className;
        }
    }

	/** Перебрасывает следующую константу */
    public static getNextTitle(String currentTitle) {
		if(MAIN_ACTIVITY.equals(currentTitle)) return CONTACTS_ACTIVITY;
		if(CONTACTS_ACTIVITY.equals(currentTitle)) return CALC_ACTIVITY;
		if(CALC_ACTIVITY.equals(currentTitle)) return LIVETYPE_ACTIVITY;
		if(LIVETYPE_ACTIVITY.equals(currentTitle)) return PROJECTS_ACTIVITY;
		if(PROJECTS_ACTIVITY.equals(currentTitle)) return SMS_ACTIVITY;
		if(SMS_ACTIVITY.equals(currentTitle)) return HELP_ACTIVITY;
		if(HELP_ACTIVITY.equals(currentTitle)) return PARAMS_ACTIVITY;
		if(PARAMS_ACTIVITY.equals(currentTitle)) return HISTORY_ACTIVITY;
		
		if(HISTORY_ACTIVITY.equals(currentTitle)) return HISTORY_EDIT_ACTIVITY;
		if(HISTORY_EDIT_ACTIVITY.equals(currentTitle)) return HEALTH_ACTIVITY;
		if(HEALTH_ACTIVITY.equals(currentTitle)) return MAIN_ACTIVITY;
		
        return MAIN_ACTIVITY;
    }

	/** Получает текст по константе */
    public static getDescByTitle(String currentTitle) {
		if(MAIN_ACTIVITY.equals(currentTitle)) return DESC_MAIN_ACTIVITY;
		if(CONTACTS_ACTIVITY.equals(currentTitle)) return DESC_CONTACTS_ACTIVITY;
		if(CALC_ACTIVITY.equals(currentTitle)) return DESC_CALC_ACTIVITY;
		if(LIVETYPE_ACTIVITY.equals(currentTitle)) return DESC_LIVETYPE_ACTIVITY;
		if(PROJECTS_ACTIVITY.equals(currentTitle)) return DESC_PROJECTS_ACTIVITY;
		if(SMS_ACTIVITY.equals(currentTitle)) return DESC_SMS_ACTIVITY;
		if(HELP_ACTIVITY.equals(currentTitle)) return DESC_HELP_ACTIVITY;
		if(PARAMS_ACTIVITY.equals(currentTitle)) return DESC_PARAMS_ACTIVITY;
		
		if(HISTORY_ACTIVITY.equals(currentTitle)) return DESC_HISTORY_ACTIVITY;
		if(HISTORY_EDIT_ACTIVITY.equals(currentTitle)) return DESC_HISTORY_EDIT_ACTIVITY;
		if(HEALTH_ACTIVITY.equals(currentTitle)) return DESC_HEALTH_ACTIVITY;
		
        return DESC_MAIN_ACTIVITY;
    }
}