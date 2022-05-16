package ru.alex.java.cloudstorage.server;

public interface ServiceDb {
    /**
     * Получение логина по логину и паролю
     * возвращает логин если есть учетная запись
     * null если пары логин пароль не нашлось
     */
    String getLoginByLoginAndPassword(String nickname, String password);

    /**
     * Регистрация нового пользователя
     * при успешной регистрации (логин и никнейм не заняты) вернет true
     * иначе вернет false
     */
    Long getDiskQuota(String Login);

    Integer getMaxNesting(String login);

    boolean isRegistration(String nickname, String password);

    /**
     * При успешной аутентификации (логин и никнейм есть в базе) вернет true
     * иначе вернет false
     */
    boolean isAuthentication(String nickname, String password);
}