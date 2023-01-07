package com.algorand.android.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import network.voi.hera.R
import network.voi.hera.databinding.CustomPassphraseValidatorBinding
import com.algorand.android.utils.getXmlStyledString
import com.algorand.android.utils.viewbinding.viewBinding

class PassphraseValidatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var selectedWord: String? = null
    private var correctWord: String? = null
    private var words: List<String> = listOf()
    private var listener: Listener? = null

    private val binding = viewBinding(CustomPassphraseValidatorBinding::inflate)

    init {
        orientation = VERTICAL
        with(binding) {
            firstWordTextView.setOnClickListener { onWordToggled(FIRST_WORD_POSITION) }
            secondWordTextView.setOnClickListener { onWordToggled(SECOND_WORD_POSITION) }
            thirdWordTextView.setOnClickListener { onWordToggled(THIRD_WORD_POSITION) }
        }
    }

    fun setup(words: List<String>, correctWord: String, correctWordPosition: Int, listener: Listener) {
        this.correctWord = correctWord
        this.listener = listener
        this.words = words
        setDescription(correctWordPosition)
        with(binding) {
            firstWordTextView.text = words.component1()
            secondWordTextView.text = words.component2()
            thirdWordTextView.text = words.component3()
        }
    }

    private fun setDescription(correctWordPosition: Int) {
        binding.selectTextView.text = context.getXmlStyledString(
            R.string.select_word,
            replacementList = listOf("word_number" to (correctWordPosition + 1).toString())
        )
    }

    private fun onWordToggled(buttonPosition: Int) {
        if (selectedWord != words[buttonPosition]) {
            unselectAllWords()
            selectWord(buttonPosition)
        }
    }

    private fun selectWord(buttonPosition: Int) {
        selectedWord = words[buttonPosition]
        when (buttonPosition) {
            FIRST_WORD_POSITION -> {
                binding.firstWordTextView.isSelected = true
            }
            SECOND_WORD_POSITION -> {
                binding.secondWordTextView.isSelected = true
            }
            THIRD_WORD_POSITION -> {
                binding.thirdWordTextView.isSelected = true
            }
        }
        listener?.onWordSelected()
    }

    private fun unselectAllWords() {
        with(binding) {
            firstWordTextView.isSelected = false
            secondWordTextView.isSelected = false
            thirdWordTextView.isSelected = false
        }
    }

    fun isValidated(): Boolean {
        return isWordSelected() && selectedWord == correctWord
    }

    fun isWordSelected(): Boolean {
        return selectedWord != null
    }

    fun interface Listener {
        fun onWordSelected()
    }

    companion object {
        private const val FIRST_WORD_POSITION = 0
        private const val SECOND_WORD_POSITION = 1
        private const val THIRD_WORD_POSITION = 2
    }
}
