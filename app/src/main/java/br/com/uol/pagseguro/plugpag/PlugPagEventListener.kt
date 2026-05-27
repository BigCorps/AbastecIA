package br.com.uol.pagseguro.plugpag

interface PlugPagEventListener {
    fun onEvent(data: PlugPagEventData)
}
