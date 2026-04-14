package com.example.vocanote.features.words.presentation

import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.vocanote.features.words.data.SavedWord
import com.example.vocanote.ui.theme.Canvas
import com.example.vocanote.ui.theme.InkSoft

@Composable
fun WordDetailPage(
    word: SavedWord,
    isSaving: Boolean,
    helperMessage: String?,
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentWord by rememberSaveable(word.id) { mutableStateOf(word.word) }
    var currentMeaning by rememberSaveable(word.id) { mutableStateOf(word.meaning) }
    var currentNote by rememberSaveable(word.id) { mutableStateOf(word.note) }
    var ttsReady by remember { mutableStateOf(false) }
    var ttsHelperMessage by remember { mutableStateOf<String?>(null) }
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    val speechEngine = remember {
        TextToSpeech(context) { status ->
            val engine = textToSpeech
            if (engine == null) {
                ttsReady = false
                ttsHelperMessage = "TTS를 초기화하지 못했어요."
                return@TextToSpeech
            }

            if (status == TextToSpeech.SUCCESS) {
                val localeResult = engine.setLanguage(Locale.US)
                val preferredVoice = engine.voices
                    ?.firstOrNull { voice ->
                        voice.locale == Locale.US && !voice.isNetworkConnectionRequired
                    }
                    ?: engine.voices?.firstOrNull { voice ->
                        voice.locale.language == Locale.ENGLISH.language && !voice.isNetworkConnectionRequired
                    }

                if (preferredVoice != null) {
                    engine.voice = preferredVoice
                }

                engine.setPitch(1.0f)
                engine.setSpeechRate(0.92f)

                ttsReady = localeResult != TextToSpeech.LANG_MISSING_DATA &&
                    localeResult != TextToSpeech.LANG_NOT_SUPPORTED

                if (!ttsReady) {
                    ttsHelperMessage = "미국 영어 TTS 음성이 없어서 기기 설정에서 영어 음성을 먼저 받아야 해요."
                }
            } else {
                ttsReady = false
                ttsHelperMessage = "TTS를 초기화하지 못했어요."
            }
        }
    }
    textToSpeech = speechEngine

    DisposableEffect(speechEngine) {
        onDispose {
            speechEngine.stop()
            speechEngine.shutdown()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Text(
                    text = "단어 상세",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Text(
                text = helperMessage ?: ttsHelperMessage ?: "단어를 수정하거나 발음을 들어볼 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )

            Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = currentWord,
                        onValueChange = { currentWord = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("단어") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    OutlinedTextField(
                        value = currentMeaning,
                        onValueChange = { currentMeaning = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("뜻") },
                        shape = RoundedCornerShape(16.dp),
                        minLines = 4,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    OutlinedTextField(
                        value = currentNote,
                        onValueChange = { currentNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("설명") },
                        placeholder = { Text("뜻 차이, 뉘앙스, 예문 느낌") },
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    Button(
                        onClick = {
                            if (ttsReady && currentWord.isNotBlank()) {
                                speechEngine.speak(
                                    currentWord.trim(),
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "word_preview"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null
                        )
                        Text(
                            text = "발음 듣기",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (currentWord.isNotBlank() && currentMeaning.isNotBlank()) {
                                onSave(currentWord, currentMeaning, currentNote)
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isSaving) "수정 중..." else "수정 저장",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
