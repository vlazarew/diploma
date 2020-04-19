package application.utils.handler;

import org.apache.commons.validator.EmailValidator;

public class EMailUtils {

    public static boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
