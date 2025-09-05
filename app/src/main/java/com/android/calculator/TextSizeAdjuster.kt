    package com.android.calculator

    import android.content.Context
    import android.content.res.Configuration
    import android.graphics.Rect
    import android.util.TypedValue
    import android.widget.TextView

    class TextSizeAdjuster(private val context: Context) {

        enum class AdjustableTextType {
            Input,
            Output,
        }

        fun adjustTextSize(textView: TextView, adjustableTextType: AdjustableTextType) {
            val screenWidth = context.resources.displayMetrics.widthPixels

            // Text size will be reduced a bit before reaching the screen width, for a smoother experience
            val maxWidth = screenWidth - dpToPx(25f)

            // Get the min and max text sizes
            val (minTextSize, maxTextSize) = getTextSizeBounds(context.resources.configuration, adjustableTextType)

            var textSize = maxTextSize
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)

            val textBounds = Rect()
            val text = textView.text.toString()

            while (textSize > minTextSize) {
                textView.paint.getTextBounds(text, 0, text.length, textBounds)
                if (textBounds.width() <= maxWidth) {
                    break
                }
                textSize -= 1f
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            }
        }

        private fun getTextSizeBounds(configuration: Configuration, adjustableTextType: AdjustableTextType): Pair<Float, Float> {
            return when (adjustableTextType) {
                AdjustableTextType.Input -> {
                    when (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_SMALL -> Pair(14f, 18f)
                        Configuration.SCREENLAYOUT_SIZE_NORMAL -> Pair(16f, 20f)
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> Pair(18f, 22f)
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> Pair(20f, 24f)
                        else -> Pair(16f, 20f)
                    }
                }
                AdjustableTextType.Output -> {
                    when (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
                        Configuration.SCREENLAYOUT_SIZE_SMALL -> Pair(20f, 32f)
                        Configuration.SCREENLAYOUT_SIZE_NORMAL -> Pair(24f, 40f)
                        Configuration.SCREENLAYOUT_SIZE_LARGE -> Pair(28f, 48f)
                        Configuration.SCREENLAYOUT_SIZE_XLARGE -> Pair(32f, 56f)
                        else -> Pair(24f, 40f)
                    }
                }
            }
        }

        private fun dpToPx(dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
            ).toInt()
        }
    }
