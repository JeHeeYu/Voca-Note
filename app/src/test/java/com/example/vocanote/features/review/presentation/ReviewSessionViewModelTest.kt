package com.example.vocanote.features.review.presentation

import com.example.vocanote.core.model.*
import com.example.vocanote.features.review.domain.*
import org.junit.Assert.*
import org.junit.Test

class ReviewSessionViewModelTest {
    private val words = (1..20).map { SavedWord("$it", "word $it", "뜻 $it") }

    @Test fun `questions stay fixed after source changes and duplicate grading is ignored`() {
        val source = words.toMutableList()
        val vm = ReviewSessionViewModel()
        vm.start(source, ReviewMode.Writing, ReviewPresentation.Batch, ReviewScope.All, 20)
        val questions = vm.state.questions
        source.clear()
        assertEquals(20, vm.state.questions.size)
        for (question in questions) vm.input(question.wordId, question.answer)
        assertTrue(vm.checkBatch())
        assertFalse(vm.checkBatch())
        vm.input(questions.first().wordId, "changed")
        assertEquals(questions.first().answer, vm.state.inputs[questions.first().wordId])
        assertEquals(20, vm.state.answers.size)
    }

    @Test fun `save failure keeps answers and retry reuses the same id without duplicate saves`() {
        val vm = ReviewSessionViewModel()
        vm.start(words, ReviewMode.Writing, ReviewPresentation.Single, ReviewScope.All, 1)
        val question = vm.state.current!!
        vm.input(question.wordId, question.answer)
        vm.check()
        vm.check()
        assertEquals(1, vm.state.answers.size)
        vm.advance()
        var calls = 0
        var complete: ((String?) -> Unit)? = null
        val ids = mutableListOf<String>()
        val save: SaveReviewSession = { id, _, _, callback -> calls++; ids += id; complete = callback }
        vm.save(save)
        vm.save(save)
        assertEquals(1, calls)
        complete!!("offline")
        assertFalse(vm.state.saved)
        assertEquals(question.answer, vm.state.inputs[question.wordId])
        vm.save(save)
        complete!!(null)
        vm.save(save)
        assertTrue(vm.state.saved)
        assertEquals(2, calls)
        assertEquals(1, ids.distinct().size)
    }
}
