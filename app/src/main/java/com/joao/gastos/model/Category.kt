package com.joao.gastos.model

enum class Category(val label: String) {
    Comida("Comida"),
    Transporte("Transporte"),
    Hogar("Hogar"),
    Ocio("Ocio"),
    Salud("Salud"),
    Compras("Compras"),
    Servicios("Servicios"),
    Otros("Otros"),
    ;

    companion object {
        fun fromStored(name: String): Category? = entries.find { it.name == name }
    }
}
