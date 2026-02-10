package br.com.fabfdev.conversordemoedas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import br.com.fabfdev.conversordemoedas.databinding.ItemCurrencyTypeBinding
import br.com.fabfdev.conversordemoedas.network.model.CurrencyType
import coil3.load

class CurrencyTypesAdapter(
    private val currencyTypes: List<CurrencyType>
): BaseAdapter() {

    override fun getCount(): Int {
        return currencyTypes.size
    }

    override fun getItem(position: Int): Any? {
        return currencyTypes[position]
    }

    override fun getItemId(position: Int): Long {
        return currencyTypes[position].hashCode().toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        return convertView ?: run {
            val item = currencyTypes[position]
            val binding = ItemCurrencyTypeBinding
                .inflate(LayoutInflater.from(parent?.context))
            with(binding) {
                tvCurrencyAcronym.text = item.acronym.uppercase()
                ivFlag.load(item.countryFlagImgUrl)/* { // Para carregar SVG
                    decoderFactory { result, options, _ -> SvgDecoder(result.source, options) }
                }*/
            }
            binding.root
        }
    }
}