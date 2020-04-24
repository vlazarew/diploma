package application.data.model.telegram;

public enum UserStatus {
    NotRegistered, VerifyPhone, VerifyEmail, Registered, WeatherSettings, Settings, MainPage;

    private static UserStatus[] userStatuses;

    public static UserStatus getInitialStatus() {
        return byId(0);
    }

    public static UserStatus byId(int id) {
        if (userStatuses == null) {
            userStatuses = UserStatus.values();
        }

        return userStatuses[id];
    }
}

