package com.example.vocanote

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.input.key.Key
import com.example.vocanote.core.model.*
import com.example.vocanote.features.editor.presentation.WordEditorScreen
import com.example.vocanote.features.review.domain.*
import com.example.vocanote.features.review.presentation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class LearningFlowTest {
    @get:Rule val compose = createComposeRule()
    private val words = (1..20).map { SavedWord("$it", "word $it", "뜻 $it") }

    @Test fun editAllMeaningsAndKeepNewWordEntryOpen() {
        var saved: WordDraft? = null
        var left = false
        compose.setContent { MaterialTheme {
            WordEditorScreen(null, emptyList(), false, { left = true }, { draft, success -> saved = draft; success() }, null)
        } }
        compose.onNodeWithText("영어 단어").performTextInput("record")
        compose.onNodeWithText("뜻 1 입력").performTextInput("기록")
        compose.onNodeWithText("뜻 추가 (1/10)").performScrollTo().performClick()
        compose.onNodeWithText("뜻 2 입력").performScrollTo().performTextInput("기록하다")
        compose.onNodeWithText("저장하고 다음 단어").performClick()
        compose.runOnIdle {
            assertEquals(listOf("기록", "기록하다"), saved!!.meanings.map { it.meaning })
            assertFalse(left)
        }
        compose.onNodeWithText("뜻 추가 (1/10)").assertExists()
        compose.onNodeWithText("record", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun twentyQuestionBatchGradesOnceAndKeepsAnswersWhileScrolling() {
        val session = ReviewSessionViewModel()
        session.start(words, ReviewMode.Writing, ReviewPresentation.Batch, ReviewScope.All, 20)
        var saves = 0
        compose.setContent { MaterialTheme {
            ReviewScreen(words, ReviewSettings(), { _, _, _, complete -> saves++; complete(null) }, {}, session = session)
        } }
        compose.onNodeWithText("1번 답").performTextInput("wrong")
        compose.onNodeWithTag("batch_questions").performScrollToIndex(20)
        compose.onNodeWithText("20번 답").assertExists()
        compose.onNodeWithTag("batch_questions").performScrollToIndex(0)
        compose.onNodeWithText("1번 답").assertTextContains("wrong")
        compose.runOnIdle {
            for (question in session.state.questions.drop(1)) session.input(question.wordId, question.answer)
        }
        compose.onNodeWithText("전체 정답 확인").performClick()
        compose.onNodeWithText("20문제 중 19개 정답").assertExists()
        compose.onNodeWithText("학습 요약 보기").performClick()
        compose.onNodeWithText("전체 답안 다시 보기").performClick()
        compose.runOnIdle { assertEquals(1, saves); assertEquals(20, session.state.answers.size) }
    }

    @Test fun enterChecksThenAdvancesAndSaveFailureCanRetry() {
        val session = ReviewSessionViewModel()
        session.start(words, ReviewMode.Writing, ReviewPresentation.Single, ReviewScope.All, 2)
        var saves = 0
        compose.setContent { MaterialTheme {
            ReviewScreen(words, ReviewSettings(), { _, _, _, complete -> saves++; complete(if (saves == 1) "테스트 저장 실패" else null) }, {}, session = session)
        } }
        val first = session.state.current!!.answer
        compose.onNodeWithText("정답", substring = false).performTextInput(first)
        compose.onNodeWithText("정답", substring = false).performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithText("1 / 2").assertExists()
        compose.onNodeWithText("정답", substring = false).performKeyInput { pressKey(Key.Enter) }
        compose.onNodeWithText("2 / 2").assertExists()
        compose.onNodeWithText("정답", substring = false).performTextReplacement(session.state.current!!.answer)
        compose.onNodeWithText("정답", substring = false).performImeAction()
        compose.onNodeWithText("결과 보기").performScrollTo().performClick()
        compose.onNodeWithText("테스트 저장 실패").assertExists()
        compose.onNodeWithText("완료", substring = false).assertIsNotEnabled()
        compose.onNodeWithText("저장 다시 시도").performClick()
        compose.onNodeWithText("학습 기록과 다음 복습 시간을 저장했어요.").assertExists()
        compose.runOnIdle { assertEquals(2, saves); assertEquals(2, session.state.answers.size) }
    }
}
