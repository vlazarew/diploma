package application.data.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

public enum UserStatus {
    NotRegistered, VerifyPhone, VerifyEmail, Registered;

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

