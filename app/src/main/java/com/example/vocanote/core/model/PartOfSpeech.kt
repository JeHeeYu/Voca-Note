package com.example.vocanote.core.model

enum class PartOfSpeech(
    val storageValue: String,
    val label: String
) {
    Noun("noun", "명사"),
    Pronoun("pronoun", "대명사"),
    Verb("verb", "동사"),
    Adjective("adjective", "형용사"),
    Adverb("adverb", "부사"),
    Preposition("preposition", "전치사"),
    Conjunction("conjunction", "접속사"),
    Interjection("interjection", "감탄사");

    companion object {
        fun fromStorageValue(value: String?): PartOfSpeech? = entries.firstOrNull {
            it.storageValue == value
        }
    }
}
