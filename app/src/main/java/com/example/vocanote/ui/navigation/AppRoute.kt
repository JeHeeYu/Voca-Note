package com.example.vocanote.ui.navigation

object AppRoute {
    const val Home = "home"
    const val Library = "library"
    const val Review = "review"
    const val Settings = "settings"
    const val Editor = "editor"
    const val WordIdArgument = "wordId"
    const val NewWordId = "new"
    const val EditorPattern = "$Editor/{$WordIdArgument}"

    fun editor(wordId: String? = null): String = "$Editor/${wordId ?: NewWordId}"
}
