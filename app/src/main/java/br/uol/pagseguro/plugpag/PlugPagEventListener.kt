package br.uol.pagseguro.plugpag

interface PlugPagEventListener {
    fun onEvent(data: PlugPagEventData)
}
