package me.devcexx.basiclockette.i18n

import me.devcexx.basiclockette.i18n.translations.EnUs
import me.devcexx.basiclockette.i18n.translations.EsEs
import me.devcexx.basiclockette.i18n.translations.EsEsCustom
import java.util.Locale

class PluginTranslations(
    private val availableTranslations: List<Pair<Locale, PluginMessages>>,
    private val defaultTranslation: PluginMessages,
) {
    companion object {
        fun buildDefault(enableCustomTranslations: Boolean): PluginTranslations {
            val esEs = if (enableCustomTranslations) EsEsCustom else EsEs

            return PluginTranslations(
                listOf(
                    Locale.of("es", "ES") to esEs,
                    Locale.of("en", "US") to EnUs,
                    Locale.of("en") to EnUs,
                    Locale.of("es") to esEs,
                ),
                EnUs,
            )
        }
    }

    fun messagesForLocale(locale: Locale): PluginMessages =
        availableTranslations.find { (availableLocale, _) ->
            if (availableLocale.country == null) {
                locale.language == availableLocale.language
            } else {
                locale == availableLocale
            }
        }?.second ?: defaultTranslation
}
