package br.com.fabfdev.conversordemoedas

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import br.com.fabfdev.conversordemoedas.databinding.ActivityMainBinding
import br.com.fabfdev.conversordemoedas.databinding.ContentExchangeRateSuccessBinding
import br.com.fabfdev.conversordemoedas.network.model.CurrencyType
import br.com.fabfdev.conversordemoedas.network.model.ExchangeRateResult
import br.com.fabfdev.conversordemoedas.ui.CurrencyTypesAdapter
import br.com.fabfdev.conversordemoedas.utils.addCurrencyMask
import br.com.fabfdev.conversordemoedas.utils.onTextChanged
import br.com.fabfdev.conversordemoedas.utils.updateTextInput
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<CurrencyExchangeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            lExchangeRateSuccess.etFromExchange.apply {
                addCurrencyMask()
                onTextChanged {
                    lExchangeRateSuccess.generateConvertedValue(viewModel.getCurrentExchangeRate())
                }
            }

            lExchangeRateError.btnTryAgain.setOnClickListener {
                showContentLoading()
                viewModel.requireCurrencyTypes()
            }

            lifecycleScope.apply {
                launch {
                    viewModel.currencyTypes.collectLatest { result ->
                        result.onSuccess { currencyTypes ->
                            showContentSuccess()
                            lExchangeRateSuccess.configureCurrencyTypesSpinners(currencyTypes)
                        }.onFailure {
                            showContentError()
                        }
                    }
                }

                launch {
                    viewModel.exchangeRate.collectLatest { result ->
                        result.onSuccess { exchangeRateResult ->
                            if (exchangeRateResult == ExchangeRateResult.empty()) {
                                return@collectLatest
                            }

                            showContentSuccess()
                            lExchangeRateSuccess.generateConvertedValue(exchangeRateResult.exchangeRate)
                        }.onFailure {
                            showContentError()
                        }
                    }
                }
            }
        }

        viewModel.requireCurrencyTypes()
        binding.showContentLoading()
    }

    private fun ContentExchangeRateSuccessBinding.configureCurrencyTypesSpinners(currencyTypes: List<CurrencyType>) {
        spnFromExchange.apply {
            adapter = CurrencyTypesAdapter(currencyTypes)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedFromCurrency = currencyTypes[position]
                    val from = selectedFromCurrency.acronym
                    val to = currencyTypes[spnToExchange.selectedItemPosition].acronym
                    viewModel.requireExchangeRate(from = from, to = to)
                    etFromExchange.updateTextInput(selectedFromCurrency.symbol)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }

        spnToExchange.apply {
            adapter = CurrencyTypesAdapter(currencyTypes)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val from = currencyTypes[spnFromExchange.selectedItemPosition].acronym
                    val selectedToCurrency = currencyTypes[spnToExchange.selectedItemPosition]
                    val to = selectedToCurrency.acronym
                    viewModel.requireExchangeRate(from = from, to = to)
                    etToExchange.updateTextInput(selectedToCurrency.symbol)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    currencyTypes.firstOrNull()?.let { firstCurrencyType ->
                        etFromExchange.updateTextInput(firstCurrencyType.symbol)
                        etToExchange.updateTextInput(firstCurrencyType.symbol)
                        viewModel.requireExchangeRate(
                            from = firstCurrencyType.acronym,
                            to = firstCurrencyType.acronym,
                        )
                    }
                }
            }
        }
    }

    private fun ContentExchangeRateSuccessBinding.generateConvertedValue(exchangeRate: Double) {
        val values = etFromExchange.text.split(" ")
        if (values.size > 1) {
            val symbol = etToExchange.text.split(" ")[0]
            val currencyValue = values[1]
                .replace("[,.]".toRegex(), "")
                .toDoubleOrNull() ?: 0.0
            val formattedValue = DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols(Locale.getDefault())
            ).format((currencyValue * exchangeRate) / 100)
            etToExchange.setText(formattedValue)
            etToExchange.updateTextInput(symbol)
        }
    }

    private fun ActivityMainBinding.showContentSuccess() {
        lExchangeRateSuccess.root.isVisible = true
        lExchangeRateError.root.isVisible = false
        progressBar.isVisible = false
    }

    private fun ActivityMainBinding.showContentError() {
        lExchangeRateSuccess.root.isVisible = false
        lExchangeRateError.root.isVisible = true
        progressBar.isVisible = false
    }

    private fun ActivityMainBinding.showContentLoading() {
        lExchangeRateSuccess.root.isVisible = false
        lExchangeRateError.root.isVisible = false
        progressBar.isVisible = true
    }

}