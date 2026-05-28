package com.abastecia.frentista.data.repository

import com.abastecia.frentista.data.model.PaymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// Interface — o resto do app só conhece isso
interface IPlugPagRepository {
    suspend fun doPayment(
        valueCents: Int,
        type: Int,
        installments: Int
    ): PaymentResult
}

// Mantém compatibilidade com constantes usadas nas telas
object PlugPagRepository {
    const val TYPE_CREDITO = 1
    const val TYPE_DEBITO = 2
}

// Implementação FAKE para desenvolvimento sem maquininha
// Usada enquanto o credenciamento PagBank não está aprovado
@Singleton
class FakePlugPagRepository @Inject constructor() : IPlugPagRepository {
    override suspend fun doPayment(
        valueCents: Int,
        type: Int,
        installments: Int
    ): PaymentResult = withContext(Dispatchers.IO) {
        // Simula o tempo de processamento da maquininha
        delay(2000)
        // Retorna sucesso simulado para testes
        PaymentResult(
            success = true,
            nsu = "NSU${System.currentTimeMillis()}",
            authCode = "AUTH${(100000..999999).random()}",
            cardLast4 = "1234",
            installments = installments
        )
    }
}

// Implementação REAL — descomenta quando o SDK estiver disponível
// @Singleton
// class RealPlugPagRepository @Inject constructor(
//     private val plugPag: br.uol.pagseguro.plugpag.PlugPag
// ) : IPlugPagRepository {
//     override suspend fun doPayment(
//         valueCents: Int,
//         type: Int,
//         installments: Int
//     ): PaymentResult = withContext(Dispatchers.IO) {
//         try {
//             val data = br.uol.pagseguro.plugpag.PlugPagPaymentData(
//                 type = type,
//                 amount = valueCents,
//                 installmentType = if (installments > 1) 2 else 1,
//                 installments = installments,
//                 userReference = "ABASTECIA-${System.currentTimeMillis()}",
//                 printReceipt = true
//             )
//             val result = plugPag.doPayment(data)
//             if (result.result == 0) {
//                 PaymentResult(
//                     success = true,
//                     nsu = result.nsu ?: "",
//                     authCode = result.authorizationCode ?: "",
//                     cardLast4 = result.cardNumber?.takeLast(4) ?: "1234",
//                     installments = installments
//                 )
//             } else {
//                 PaymentResult(
//                     success = false,
//                     errorMessage = result.message ?: "Pagamento não aprovado"
//                 )
//             }
//         } catch (e: Exception) {
//             PaymentResult(success = false, errorMessage = e.message ?: "Erro desconhecido")
//         }
//     }
// }
