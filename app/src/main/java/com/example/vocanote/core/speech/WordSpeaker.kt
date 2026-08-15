package com.example.vocanote.core.speech

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class WordSpeaker(
    val isReady: Boolean,
    val errorMessage: String?,
    val speak: (String) -> Unit
)

@Composable
fun rememberWordSpeaker(): WordSpeaker {
    val context = LocalContext.current
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isReady by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        lateinit var textToSpeech: TextToSpeech
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                errorMessage = "기기의 음성 엔진을 시작하지 못했어요."
                return@TextToSpeech
            }

            val languageResult = textToSpeech.setLanguage(Locale.US)
            isReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                languageResult != TextToSpeech.LANG_NOT_SUPPORTED
            if (isReady) {
                textToSpeech.setSpeechRate(0.9f)
                textToSpeech.setPitch(1f)
                errorMessage = null
            } else {
                errorMessage = "기기 설정에서 영어 음성을 설치해 주세요."
            }
        }
        engine = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            engine = null
        }
    }

    return WordSpeaker(
        isReady = isReady,
        errorMessage = errorMessage,
        speak = { text ->
            if (isReady && text.isNotBlank()) {
                engine?.speak(text.trim(), TextToSpeech.QUEUE_FLUSH, null, "voca-note-word")
            }
        }
    )
}
