package com.android.calculator.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.calculator.R
import com.android.calculator.MyPreferences
import com.android.calculator.TextSizeAdjuster
import com.android.calculator.Themes
import com.android.calculator.calculator.Calculator
import com.android.calculator.calculator.*
import com.android.calculator.calculator.parser.NumberFormatter
import com.android.calculator.calculator.parser.NumberingSystem.Companion.toNumberingSystem
import com.android.calculator.history.History
import com.android.calculator.history.HistoryAdapter
import com.android.calculator.util.ScientificMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import com.android.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var preferences: MyPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var calculator: Calculator
    private lateinit var textSizeAdjuster: TextSizeAdjuster
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var scientificMode: ScientificMode
    
    private val historyList = mutableListOf<History>()
    private var cursorPosition = 0
    private var isEqualLastAction = false
    private var isDegreeModeActivated = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme
        Themes.applyTheme(this)
        super.onCreate(savedInstanceState)
        
        // Initialize binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize preferences
        preferences = MyPreferences(this)
        
        // Initialize components
        calculator = Calculator(preferences.numberPrecision)
        textSizeAdjuster = TextSizeAdjuster(this)
        historyAdapter = HistoryAdapter(historyList) { history ->
            // Handle history item click
            binding.input.setText(history.calculation)
            cursorPosition = binding.input.text.length
        }
        
        // Load history
        historyList.addAll(preferences.getHistory())
        
        // Apply preferences
        applyPreferences()
        
        // Setup UI components
        setupButtonClickListeners()
    }
    
    private fun setupButtonClickListeners() {
        // Operator buttons
        binding.addButton.setOnClickListener { operatorButtonClicked("+") }
        binding.subtractButton.setOnClickListener { operatorButtonClicked("-") }
        binding.multiplyButton.setOnClickListener { operatorButtonClicked("×") }
        binding.divideButton.setOnClickListener { operatorButtonClicked("÷") }

        // Special buttons
        binding.equalsButton.setOnClickListener { equalsButtonClicked() }
        binding.clearButton.setOnClickListener { clearButtonClicked() }
        binding.backspaceButton.setOnClickListener { backspaceButtonClicked() }
        binding.pointButton.setOnClickListener { dotButtonClicked() }

        // Parentheses
        binding.leftParenthesisButton.setOnClickListener { leftParenthesisButtonClicked() }
        binding.rightParenthesisButton.setOnClickListener { rightParenthesisButtonClicked() }
        
        // Number buttons
        binding.zeroButton.setOnClickListener { numberButtonClicked("0") }
        binding.oneButton.setOnClickListener { numberButtonClicked("1") }
        binding.twoButton.setOnClickListener { numberButtonClicked("2") }
        binding.threeButton.setOnClickListener { numberButtonClicked("3") }
        binding.fourButton.setOnClickListener { numberButtonClicked("4") }
        binding.fiveButton.setOnClickListener { numberButtonClicked("5") }
        binding.sixButton.setOnClickListener { numberButtonClicked("6") }
        binding.sevenButton.setOnClickListener { numberButtonClicked("7") }
        binding.eightButton.setOnClickListener { numberButtonClicked("8") }
        binding.nineButton.setOnClickListener { numberButtonClicked("9") }

        // Scientific buttons (will be set up in updateScientificMode)

        // Input field
        binding.input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCursorPosition()
                textSizeAdjuster.adjustTextSize(binding.input, TextSizeAdjuster.AdjustableTextType.Input)
            }
        })

        // Long click listeners
        setupLongClickListeners()

        // Menu button
        binding.menuButton.setOnClickListener { showMenu() }

        // Back button handling
        onBackPressedDispatcher.addCallback(this) {
            // TODO: Fix sliding panel references
            // if (binding.slidingLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            //     binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            // } else {
                finish()
            // }
        }
    }

    private fun setupLongClickListeners() {
        if (preferences.longClickToCopyValue) {
            binding.resultDisplay.setOnLongClickListener {
                copyToClipboard(binding.resultDisplay.text.toString())
                true
            }
        }
    }

    private fun applyPreferences() {
        // Keep screen on
        if (preferences.preventPhoneFromSleeping) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // Show on lock screen
        if (preferences.showOnLockScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        }

        // Scientific mode
        scientificMode = ScientificMode.fromInt(preferences.scientificMode)
        updateScientificMode()

        // Degree/Radian mode
        isDegreeModeActivated = !preferences.useRadiansByDefault
    }

    private fun updateScientificMode() {
        // Implementation for scientific mode UI updates
        // This would include showing/hiding scientific buttons based on mode
    }

    private fun numberButtonClicked(number: String) {
        addToInput(number)
        isEqualLastAction = false
    }

    private fun operatorButtonClicked(operator: String) {
        addToInput(operator)
        isEqualLastAction = false
    }

    private fun addToInput(text: String) {
        val currentText = binding.input.text.toString()
        val newText = currentText.substring(0, cursorPosition) + text + currentText.substring(cursorPosition)
        binding.input.setText(newText)
        cursorPosition += text.length
        binding.input.setSelection(cursorPosition)
        
        if (preferences.vibrationMode) {
            binding.input.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun equalsButtonClicked() {
        val input = binding.input.text.toString()
        if (input.isNotEmpty()) {
            calculate(input)
        }
    }

    private fun calculate(input: String) {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    calculator.evaluate(input, isDegreeModeActivated)
                }
                
                binding.resultDisplay.text = formatResult(result)
                textSizeAdjuster.adjustTextSize(binding.resultDisplay, TextSizeAdjuster.AdjustableTextType.Output)
                
                // Save to history
                if (preferences.autoSaveCalculationWithoutEqualButton || isEqualLastAction) {
                    saveToHistory(input, binding.resultDisplay.text.toString())
                }
                
                isEqualLastAction = true
                
            } catch (e: Exception) {
                handleCalculationError(e)
            }
        }
    }

    private fun formatResult(result: BigDecimal): String {
        val numberFormatter = NumberFormatter()
        return numberFormatter.format(
            result,
            preferences.numberPrecision,
            preferences.writeNumberIntoScientificNotation,
            preferences.numberingSystem.toNumberingSystem()
        )
    }

    private fun handleCalculationError(error: Exception) {
        val errorMessage = when {
            error.message?.contains(syntax_error) == true -> getString(R.string.syntax_error)
            error.message?.contains(domain_error) == true -> getString(R.string.domain_error)
            error.message?.contains(division_by_0) == true -> getString(R.string.division_by_0)
            error.message?.contains(require_real_number) == true -> getString(R.string.require_real_number)
            error.message?.contains(is_infinity) == true -> getString(R.string.is_infinity)
            else -> getString(R.string.syntax_error)
        }
        binding.resultDisplay.text = errorMessage
    }

    private fun saveToHistory(calculation: String, result: String) {
        val history = History(calculation, result)
        historyList.add(0, history)
        
        // Limit history size
        if (historyList.size > preferences.historySize) {
            historyList.removeAt(historyList.size - 1)
        }
        
        preferences.saveHistory(historyList)
        historyAdapter.notifyItemInserted(0)
        // binding.historyRecyclerView.scrollToPosition(0)
    }

    private fun clearButtonClicked() {
        binding.input.setText("")
        binding.resultDisplay.text = ""
        cursorPosition = 0
        isEqualLastAction = false
    }

    private fun backspaceButtonClicked() {
        val currentText = binding.input.text.toString()
        if (currentText.isNotEmpty() && cursorPosition > 0) {
            val newText = currentText.substring(0, cursorPosition - 1) + currentText.substring(cursorPosition)
            binding.input.setText(newText)
            cursorPosition--
            binding.input.setSelection(cursorPosition)
        }
        isEqualLastAction = false
    }

    private fun dotButtonClicked() {
        addToInput(".")
    }

    private fun leftParenthesisButtonClicked() {
        addToInput("(")
    }

    private fun rightParenthesisButtonClicked() {
        addToInput(")")
    }

    private fun updateCursorPosition() {
        cursorPosition = binding.input.selectionStart
    }

    private fun showMenu() {
        val popupMenu = PopupMenu(this, findViewById(R.id.menuButton))
        popupMenu.menuInflater.inflate(R.menu.app_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_menu_settings_button -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.app_menu_about_button -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.app_menu_clear_history_button -> {
                    clearHistory()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun clearHistory() {
        historyList.clear()
        preferences.clearHistory()
        historyAdapter.notifyDataSetChanged()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("calculation", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.value_copied), Toast.LENGTH_SHORT).show()
    }

    private fun handleIntent(intent: Intent?) {
        // Handle any special intents (like from quick settings tile)
    }

    override fun onResume() {
        super.onResume()
        // Refresh preferences in case they were changed in settings
        applyPreferences()
    }
}
