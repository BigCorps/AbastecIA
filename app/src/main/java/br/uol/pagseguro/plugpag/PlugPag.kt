package br.uol.pagseguro.plugpag

import android.content.Context
import kotlinx.coroutines.delay

class PlugPag(context: Context, appIdent: PlugPagAppIdentification) {
    companion object {
        const val TYPE_CREDITO = 1
        const val TYPE_DEBITO = 2
        const val TYPE_PIX = 3
        const val TYPE_DINHEIRO = 4
        const val INSTALLMENT_TYPE_A_VISTA = 1
        const val INSTALLMENT_TYPE_PARC_VENDEDOR = 2
        
        const val RET_OK = 0
    }

    private var eventListener: PlugPagEventListener? = null

    fun setEventListener(listener: PlugPagEventListener) {
        this.eventListener = listener
    }

    suspend fun doPayment(paymentData: PlugPagPaymentData): PlugPagPaymentResult {
        when (paymentData.type) {
            TYPE_PIX -> {
                eventListener?.onEvent(PlugPagEventData(0, "Gerando QR Code PIX..."))
                delay(1200)
                eventListener?.onEvent(PlugPagEventData(1, "Aguardando confirmação do PIX..."))
                delay(2000)
                eventListener?.onEvent(PlugPagEventData(2, "Confirmando com o Banco..."))
                delay(1500)
                eventListener?.onEvent(PlugPagEventData(3, "Recebimento PIX Confirmado!"))
                delay(1000)
                
                return PlugPagPaymentResult(
                    nsu = "PIX${(100000..999999).random()}",
                    authorizationCode = "PIX_OK",
                    cardNumber = "PIX",
                    message = "PIX Aprovado",
                    result = RET_OK
                )
            }
            TYPE_DINHEIRO -> {
                eventListener?.onEvent(PlugPagEventData(0, "Aguardando dinheiro em mãos..."))
                delay(1200)
                eventListener?.onEvent(PlugPagEventData(1, "Aguardando confirmação do frentista..."))
                delay(1500)
                eventListener?.onEvent(PlugPagEventData(2, "Registrando venda no Caixa..."))
                delay(1000)
                
                return PlugPagPaymentResult(
                    nsu = "CASH${(100000..999999).random()}",
                    authorizationCode = "CASH_OK",
                    cardNumber = "DINHEIRO",
                    message = "Dinheiro Registrado",
                    result = RET_OK
                )
            }
            else -> {
                eventListener?.onEvent(PlugPagEventData(0, "Iniciando comunicação com leitor..."))
                delay(1200)
                eventListener?.onEvent(PlugPagEventData(1, "Aguardando cartão (insira ou aproxime)..."))
                delay(2000)
                eventListener?.onEvent(PlugPagEventData(2, "Lendo chip e senha..."))
                delay(1500)
                eventListener?.onEvent(PlugPagEventData(3, "Processando pagamento no PagBank..."))
                delay(1800)
                eventListener?.onEvent(PlugPagEventData(4, "Imprimindo comprovante..."))
                delay(1000)
                
                return PlugPagPaymentResult(
                    nsu = paymentData.nsu,
                    authorizationCode = paymentData.authorizationCode,
                    cardNumber = paymentData.cardNumber,
                    message = "Transação Aprovada",
                    result = RET_OK
                )
            }
        }
    }

    suspend fun voidPayment(paymentData: PlugPagPaymentData): PlugPagPaymentResult {
        delay(1200)
        return PlugPagPaymentResult(
            nsu = paymentData.userReference,
            authorizationCode = "VOID_OK",
            cardNumber = "0000",
            message = "Cancelamento efetuado",
            result = RET_OK
        )
    }
}

class PlugPagPaymentResult(
    val nsu: String,
    val authorizationCode: String,
    val cardNumber: String,
    val message: String,
    val result: Int
)
