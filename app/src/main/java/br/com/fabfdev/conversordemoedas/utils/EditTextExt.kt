package br.com.fabfdev.conversordemoedas.utils

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.Currency
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import java.util.Locale

fun EditText.updateTextInput(symbol: CurrencyTypeSymbol) {
    val s = this.text

    val values = s.toString().split(" ")
    var lastSymbol = ""
    if (values.size > 1) {
        lastSymbol = values[0]
    }

    val cleanValue = s.toString()
        .replace(lastSymbol, "")
        .trim()

    val formatted = "$symbol $cleanValue"

    this.setText(formatted)
    this.setSelection(formatted.length.coerceAtMost(text.length))
}

fun EditText.addCurrencyMask() {
    var watcher: TextWatcher? = null
    var currentText = ""
    watcher = addTextChangedListener(afterTextChanged = { s ->
        if (s.toString() != currentText) {
            removeTextChangedListener(watcher)

            val values = s.toString().split(" ")

            val symbol = if (values.size > 1) values[0] else ""
            val value = if (values.size > 1) values[1] else s.toString()


            val cleanValue = value
                .replace("[,.]".toRegex(), "")
            val numValue = cleanValue.toDoubleOrNull() ?: 0.0

            val formattedValue = DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols(Locale.getDefault())
            ).format(numValue / 100)

            val finalFormatted = if (symbol.isNotEmpty()) {
                "$symbol $formattedValue"
            } else {
                formattedValue
            }

            currentText = finalFormatted
            setText(finalFormatted)
            setSelection(finalFormatted.length.coerceAtMost(text.length))

            addTextChangedListener(watcher)
        }
    })
}

fun EditText.onTextChanged(onChanging: () -> Unit) {
    addTextChangedListener(onTextChanged = { text, start, before, count ->
        onChanging.invoke()
    })
}

private fun getLocaleBasedOnCurrency(currencyAcronym: CurrencyTypeAcronym): Locale {
    val currency = Currency.getInstance(currencyAcronym)

    val locale = Locale.getAvailableLocales()
        .firstOrNull { locale ->
            try {
                Currency.getInstance(locale) == currency
            } catch (e: Exception) {
                false
            }
        } ?: Locale.getDefault()
    return locale
}