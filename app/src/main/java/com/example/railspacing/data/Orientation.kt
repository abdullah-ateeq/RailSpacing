package com.example.railspacing.data

enum class Orientation(val label: String) {
    HORIZONTAL("HORIZONTAL"),
    VERTICAL("VERTICAL");

    companion object {
        fun fromName(name: String): Orientation {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: HORIZONTAL
        }
    }
}
