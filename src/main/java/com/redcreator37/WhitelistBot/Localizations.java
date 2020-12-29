package com.redcreator37.WhitelistBot;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Handles all localization-specific aspects of the bot
 */
public class Localizations {

    /**
     * The {@link ResourceBundle} object with all user-facing messages
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static final ResourceBundle strings =
            getBundleFromLangCode("Strings", "en_US").get();

    /**
     * Returns the localized message with the matching tag from the
     * strings {@link ResourceBundle}
     *
     * @param key the localization key
     * @return the message (or {@code null} if the key isn't valid)
     */
    public static String lc(String key) {
        return strings.getString(key);
    }

    /**
     * Returns the {@link ResourceBundle} with the matching name for this
     * language code
     *
     * @param baseName the full name of the {@link ResourceBundle} to retrieve
     * @param langCode a code in the language_country format
     *                 (ex. {@code en_US})
     * @return the matching {@link ResourceBundle} or an empty {@link Optional}
     * if the matching bundle-language combination wasn't found
     */
    @SuppressWarnings("SameParameterValue")
    private static Optional<ResourceBundle> getBundleFromLangCode(String baseName, String langCode) {
        String[] locale = langCode.split("_");
        try {
            return Optional.of(ResourceBundle.getBundle(baseName, new Locale(locale[0]
                    .toLowerCase(), locale[1].toUpperCase())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
