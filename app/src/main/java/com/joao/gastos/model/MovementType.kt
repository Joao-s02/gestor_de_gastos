package com.joao.gastos.model

enum class MovementType(val label: String) {
    GASTO("Gasto"),
    INGRESO("Ingreso"),
    ;

    companion object {
        fun fromStored(name: String): MovementType? = entries.find { it.name == name }
    }
}
