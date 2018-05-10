package com.bradcypert.textico.adapters

interface SearchAndRemove {
    fun removeItem(position: Int)

    fun search(query: String)
}
