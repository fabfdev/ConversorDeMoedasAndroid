package br.com.fabfdev.conversordemoedas.utils

import android.widget.EditText

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
    this.setSelection(formatted.length)
}