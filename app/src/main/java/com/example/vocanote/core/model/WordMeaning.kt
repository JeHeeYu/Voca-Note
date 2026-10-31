package com.example.vocanote.core.model

const val MAX_MEANINGS = 10

data class WordMeaning(val meaning: String = "", val partOfSpeech: PartOfSpeech? = null)

val SavedWord.wordMeanings: List<WordMeaning>
    get() = meanings.ifEmpty { listOf(WordMeaning(meaning, partOfSpeech)) }

val WordDraft.wordMeanings: List<WordMeaning>
    get() = meanings.ifEmpty { listOf(WordMeaning(meaning, partOfSpeech)) }

val SavedWord.meaningSummary: String
    get() = wordMeanings.joinToString(" · ") { entry ->
        listOfNotNull(entry.partOfSpeech?.displayLabel, entry.meaning).joinToString(" ")
    }

fun decodeMeanings(value: Any?, legacyMeaning: String, legacyPartOfSpeech: String?): List<WordMeaning> {
    val entries = (value as? List<*>).orEmpty().mapNotNull { item ->
        val entry = item as? Map<*, *> ?: return@mapNotNull null
        val meaning = (entry["meaning"] as? String)?.trim().orEmpty()
        if (meaning.isBlank()) return@mapNotNull null
        WordMeaning(meaning, PartOfSpeech.fromStorageValue(entry["partOfSpeech"] as? String))
    }
    return entries.ifEmpty {
        listOf(WordMeaning(legacyMeaning.trim(), PartOfSpeech.fromStorageValue(legacyPartOfSpeech)))
    }
}

fun List<WordMeaning>.toStorageMeanings(): List<Map<String, String?>> = map {
    mapOf("meaning" to it.meaning, "partOfSpeech" to it.partOfSpeech?.storageValue)
}

fun splitMeaningEntries(entries: List<WordMeaning>, index: Int): List<WordMeaning> {
    val entry = entries.getOrNull(index) ?: return entries
    val pieces = entry.meaning.split(',', '，').map(String::trim).filter(String::isNotBlank)
    if (pieces.size <= 1 || entries.size - 1 + pieces.size > MAX_MEANINGS) return entries
    return entries.take(index) + pieces.map { entry.copy(meaning = it) } + entries.drop(index + 1)
}
