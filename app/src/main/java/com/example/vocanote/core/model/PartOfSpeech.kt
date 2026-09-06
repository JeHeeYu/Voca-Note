package com.example.vocanote.core.model

enum class PartOfSpeech(
    val storageValue: String,
    val label: String,
    val abbreviation: String
) {
    Noun("noun", "명사", "n"),
    Pronoun("pronoun", "대명사", "pron"),
    Verb("verb", "동사", "v"),
    Adjective("adjective", "형용사", "adj"),
    Adverb("adverb", "부사", "adv"),
    Preposition("preposition", "전치사", "prep"),
    Conjunction("conjunction", "접속사", "conj"),
    Interjection("interjection", "감탄사", "interj");

    val displayLabel: String
        get() = "$label ($abbreviation)"

    companion object {
        fun fromStorageValue(value: String?): PartOfSpeech? = entries.firstOrNull {
            it.storageValue == value
        }
    }
}
