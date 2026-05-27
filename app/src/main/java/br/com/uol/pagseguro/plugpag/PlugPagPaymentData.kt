package br.com.uol.pagseguro.plugpag

class PlugPagPaymentData(
    val type: Int,
    val amount: Int,
    val installmentType: Int,
    val installments: Int,
    val userReference: String,
    val printReceipt: Boolean,
    val nsu: String = "NSU${(100000..999999).random()}",
    val authorizationCode: String = "AUTH${(100000..999999).random()}",
    val cardNumber: String = "123456******${(1000..9999).random()}"
)
