package br.com.fabfdev.conversordemoedas.utils

import java.util.Currency
import java.util.Locale

fun String.getCurrencySymbol(): String {
    val currency = Currency.getInstance(this)

    val locale = Locale.getAvailableLocales()
        .firstOrNull() { locale ->
            try {
                Currency.getInstance(locale) == currency
            } catch (e: Exception) {
                false
            }
        } ?: Locale.getDefault()
    return currency.getSymbol(locale)
}