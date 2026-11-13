package com.example.data.model

enum class AskOutcome(val displayName: String) {
    REJECTED("Rejected"),
    ACCEPTED("Accepted"),
    PENDING("Awaiting Answer");

    companion object {
        fun fromString(value: String): AskOutcome {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}
