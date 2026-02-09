package br.com.fabfdev.conversordemoedas.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyType(
    val name: String,
    val acronym: String,
    val symbol: String,
    @SerialName("country_flag_img_url")
    val countryFlagImgUrl: String,
)

@Serializable
data class CurrencyTypesResult(
    val values: List<CurrencyType>,
)
