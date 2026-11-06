package com.example.vocanote.features.review.domain

import java.text.Normalizer
import java.util.Locale

enum class CrosswordDirection { Across, Down }
data class CrosswordEntry(val wordId: String, val answer: String, val row: Int, val col: Int,
                          val direction: CrosswordDirection, val number: Int = 0)
data class CrosswordPuzzle(val entries: List<CrosswordEntry> = emptyList(), val rows: Int = 0, val cols: Int = 0)
fun crosswordAnswer(word: String): String = Normalizer.normalize(word, Normalizer.Form.NFKC)
    .uppercase(Locale.ROOT).filterNot { it.isWhitespace() || it in "'’-" }
    .takeIf { it.matches(Regex("[A-Z]{2,18}")) }.orEmpty()
fun cellKey(row: Int, col: Int) = "$row,$col"
fun entryCells(entry: CrosswordEntry): List<Pair<String, Char>> = entry.answer.mapIndexed { index, letter ->
    cellKey(entry.row + if (entry.direction == CrosswordDirection.Down) index else 0,
        entry.col + if (entry.direction == CrosswordDirection.Across) index else 0) to letter
}
private data class Cell(val letter: Char, val directions: MutableSet<CrosswordDirection>)

fun buildCrossword(candidates: List<Pair<String, String>>, count: Int): CrosswordPuzzle {
    val words = candidates.map { it.first to crosswordAnswer(it.second) }.filter { it.second.isNotBlank() }.distinctBy { it.second }
    val limit = count.coerceIn(0, 30)
    var best = emptyList<CrosswordEntry>()
    for (seed in words.take(12)) {
        val entries = mutableListOf(CrosswordEntry(seed.first, seed.second, 0, 0, CrosswordDirection.Across))
        val board = mutableMapOf<String, Cell>()
        fun place(entry: CrosswordEntry) {
            entryCells(entry).forEach { (key, letter) -> board.getOrPut(key) { Cell(letter, mutableSetOf()) }.directions.add(entry.direction) }
        }
        place(entries.first())
        val pending = words.filter { it.first != seed.first }.toMutableList()
        var changed = true
        while (changed && entries.size < limit) {
            changed = false
            val iterator = pending.iterator()
            while (iterator.hasNext() && entries.size < limit) {
                val (wordId, answer) = iterator.next()
                var chosen: CrosswordEntry? = null
                var bestScore = Int.MIN_VALUE
                for ((key, cell) in board) {
                    val (crossRow, crossCol) = key.split(',').map(String::toInt)
                    for (direction in CrosswordDirection.entries) {
                        if (direction in cell.directions) continue
                        val dr = if (direction == CrosswordDirection.Down) 1 else 0
                        val dc = if (direction == CrosswordDirection.Across) 1 else 0
                        for (offset in answer.indices) {
                            if (answer[offset] != cell.letter) continue
                            val row = crossRow - dr * offset
                            val col = crossCol - dc * offset
                            if (cellKey(row - dr, col - dc) in board || cellKey(row + dr * answer.length, col + dc * answer.length) in board) continue
                            var crosses = 0
                            var valid = true
                            for (i in answer.indices) {
                                val r = row + dr * i
                                val c = col + dc * i
                                val existing = board[cellKey(r, c)]
                                if (existing != null) {
                                    if (existing.letter != answer[i] || direction in existing.directions) { valid = false; break }
                                    crosses++
                                } else if (cellKey(r + dc, c + dr) in board || cellKey(r - dc, c - dr) in board) { valid = false; break }
                            }
                            if (!valid || crosses == 0) continue
                            val trial = CrosswordEntry(wordId, answer, row, col, direction)
                            val all = (board.keys + entryCells(trial).map { it.first }).map { it.split(',').map(String::toInt) }
                            val height = all.maxOf { it[0] } - all.minOf { it[0] } + 1
                            val width = all.maxOf { it[1] } - all.minOf { it[1] } + 1
                            if (height > 25 || width > 25) continue
                            val score = crosses * 1000 - height * width
                            if (score > bestScore) { chosen = trial; bestScore = score }
                        }
                    }
                }
                chosen?.let { entries.add(it); place(it); iterator.remove(); changed = true }
            }
        }
        if (entries.size > best.size) best = entries.toList()
        if (best.size >= limit) break
    }
    if (limit == 0 || best.size < 2) return CrosswordPuzzle()
    val minRow = best.minOf { it.row }
    val minCol = best.minOf { it.col }
    var number = 0
    var last = ""
    best = best.map { it.copy(row = it.row - minRow, col = it.col - minCol) }
        .sortedWith(compareBy<CrosswordEntry> { it.row }.thenBy { it.col }).map {
            val key = cellKey(it.row, it.col)
            if (key != last) { number++; last = key }
            it.copy(number = number)
        }
    return CrosswordPuzzle(best,
        best.maxOf { it.row + if (it.direction == CrosswordDirection.Down) it.answer.length else 1 },
        best.maxOf { it.col + if (it.direction == CrosswordDirection.Across) it.answer.length else 1 })
}
fun crosswordInputs(puzzle: CrosswordPuzzle, cells: Map<String, String>): Map<String, String> =
    puzzle.entries.associate { it.wordId to entryCells(it).joinToString("") { (key, _) -> cells[key] ?: " " } }
fun crosswordComplete(puzzle: CrosswordPuzzle, cells: Map<String, String>): Boolean =
    puzzle.entries.isNotEmpty() && puzzle.entries.all { entry -> entryCells(entry).all { (key, _) -> cells[key]?.matches(Regex("[A-Z]")) == true } }
