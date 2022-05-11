package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

// Vibration is controlled by passing in an array representing the number of milliseconds
// each interval of buzzing and non-buzzing takes.
// So the array [0, 200, 100, 300] will wait 0 milliseconds,
// then buzz for 200ms, then wait 100ms, then buzz fo 300ms.

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel : ViewModel() {

    private val _buzzer = MutableLiveData<BuzzType>()
    val buzzer: LiveData<BuzzType> get() = _buzzer

    // The current word
    private val _word = MutableLiveData<String>("")
    val word: LiveData<String> get() = _word

    // The hint for the current word
    val wordHint: LiveData<String> = Transformations.map(word) { currentWord ->
        val randomLetterIndex = (1..currentWord.length).random()
        val letterAtIndex = currentWord[randomLetterIndex - 1].uppercaseChar()
        "Current word has ${currentWord.length} letters " +
                "\nThe letter at position $randomLetterIndex is $letterAtIndex"
    }

    // The current score
    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> get() = _score

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    // Event which triggers the end of the game
    private val _eventGameFinish = MutableLiveData<Boolean>(false)
    val eventGameFinish: LiveData<Boolean> get() = _eventGameFinish

    // Countdown time
    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long> get() = _currentTime

    // The String version of the current time
    val currentTimeString: LiveData<String> =
        Transformations.map(currentTime) { time ->
            DateUtils.formatElapsedTime(time)
        }

    private val timer: CountDownTimer

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    init {
        Log.i("GameViewModel", "GameViewModel created!")
        resetList()
        nextWord()
        // Creates a timer which triggers the end of the game when it finishes
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            /**
             *
             * @param millisUntilFinished Long the amount of time until the
             * timer is finished in milliseconds
             */
            override fun onTick(millisUntilFinished: Long) {
                // Convert the millisUntilFinished to seconds
                _currentTime.value = millisUntilFinished / ONE_SECOND
                if (millisUntilFinished / ONE_SECOND <= COUNTDOWN_PANIC_SECONDS) {
                    _buzzer.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _buzzer.value = BuzzType.GAME_OVER
                onGameFinish()
            }

        }
        timer.start()
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = score.value?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = score.value?.plus(1)
        _buzzer.value = BuzzType.CORRECT
        nextWord()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        if (wordList.isEmpty()) {
            resetList()
        }
        //Select and remove a word from the list
        _word.value = wordList.removeAt(0)

    }

    /**
     * Method for the game completed event
     */
    fun onGameFinish() {
        _eventGameFinish.value = true
    }

    /**
     * Method for the game complete event
     */
    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }

    fun onBuzzComplete() {
        _buzzer.value = BuzzType.NO_BUZZ
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel the timer
        timer.cancel()
    }

    companion object {
        // Time when the game is over
        private const val DONE = 0L

        // Countdown time interval
        private const val ONE_SECOND = 1000L

        // Total time for the game
        private const val COUNTDOWN_TIME = 60_000L

        // This is the time when the phone will start buzzing each second
        private const val COUNTDOWN_PANIC_SECONDS = 10L
    }

    enum class BuzzType(val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }
}