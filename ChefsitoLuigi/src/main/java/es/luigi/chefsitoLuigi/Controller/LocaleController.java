package es.luigi.chefsitoLuigi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/locale")
public class LocaleController {

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/change")
    public String changeLocale(@RequestParam String lang) {
        return "Language changed to: " + lang;
    }

    @GetMapping("/current")
    public String getCurrentLocale(Locale locale) {
        return "Current locale: " + locale.getLanguage() + ", Display: " + locale.getDisplayName();
    }

    @GetMapping("/test-message")
    public String testMessage(Locale locale) {
        // Probar que los mensajes se resuelven correctamente
        String emailRequired = messageSource.getMessage("message.user.email.required", null, locale);
        String passwordSize = messageSource.getMessage("message.user.password.size", null, locale);

        return String.format("Locale: %s, Email required: %s, Password size: %s",
                locale.getLanguage(), emailRequired, passwordSize);
    }

    @GetMapping("/test-all")
    public String testAllMessages(Locale locale) {
        StringBuilder result = new StringBuilder();
        result.append("Locale: ").append(locale.getLanguage()).append("\n");

        // Probar varios mensajes
        String[] messageKeys = {
                "message.user.email.required",
                "message.user.password.required",
                "message.user.password.size",
                "message.user.fullName.required"
        };

        for (String key : messageKeys) {
            try {
                String message = messageSource.getMessage(key, null, locale);
                result.append(key).append(": ").append(message).append("\n");
            } catch (Exception e) {
                result.append(key).append(": NOT FOUND\n");
            }
        }

        return result.toString();
    }
}