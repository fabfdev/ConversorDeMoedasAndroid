package br.com.fabfdev.conversordemoedas

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import br.com.fabfdev.conversordemoedas.databinding.ActivityMainBinding
import br.com.fabfdev.conversordemoedas.network.model.CurrencyType
import br.com.fabfdev.conversordemoedas.ui.CurrencyTypesAdapter
import br.com.fabfdev.conversordemoedas.utils.addCurrencyMask
import br.com.fabfdev.conversordemoedas.utils.updateTextInput
import kotlinx.coroutines.launch

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

        binding.etFromExchange.addCurrencyMask()

        lifecycleScope.apply {
            launch {
                viewModel.currencyTypes.collect { result ->
                    result.onSuccess { currencyTypes ->
                        binding.configureCurrencyTypesSpinners(currencyTypes)
                    }.onFailure {
                        Toast.makeText(
                            this@MainActivity,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            launch {
                viewModel.exchangeRate.collect { result ->
                    result.onSuccess {
                        println(it)
                    }.onFailure {
                        Toast.makeText(
                            this@MainActivity,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.requireCurrencyTypes()
    }

    private fun ActivityMainBinding.configureCurrencyTypesSpinners(currencyTypes: List<CurrencyType>) {
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
                    val to = currencyTypes[position].acronym
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

}