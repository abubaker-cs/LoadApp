package com.udacity.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.properties.Delegates

/**
 * Custom Loading Button
 */
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    // Width + Size
    private var widthSize = 0
    private var heightSize = 0

    // This class provides a simple timing engine for running animations which calculate animated
    // values and set them on target objects.
    private var valueAnimator = ValueAnimator()

    // Button Text
    private var btnText = ""

    // Button: Background Color
    private var btnBackgroundColor = 0

    // Button: Progress Status
    private var btnProgressAnimation: Float = 0f

    // Circular Progress
    private var circularProgress = RectF(80f, 16f, 160f, 96f)

    // Circular Progress: Color
    private var circularProgressColor = 0


    /**
     * buttonState()
     * 1. Loading
     * 2. Completed
     * 3. Else -
     */
    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {

            // Loading
            ButtonState.Loading -> {

                /**
                 * ValueAnimator provides a timing engine for running animation which calculates
                 * the animated values and set them on the target objects.
                 */

                valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {

                    //
                    addUpdateListener {
                        btnProgressAnimation = animatedValue as Float
                        invalidate()
                    }

                    //
                    repeatCount = ValueAnimator.INFINITE

                    //
                    repeatMode = ValueAnimator.RESTART

                    // Button Animation Duration: 2.5sec
                    duration = 2500

                    //
                    start()

                }

                // Disable button
                isEnabled = false
            }

            // Completed
            ButtonState.Completed -> {

                // Cancel animator
                valueAnimator.cancel()

                // Update view
                invalidate()

                // Enable button
                isEnabled = true

                // Reset button state
                buttonState = ButtonState.Clicked

            }

            else -> {
                // Enable button
                isEnabled = true
                return@observable
            }
        }
    }

    /**
     * Initial Configuration
     */
    init {

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {

            // Background Color
            btnBackgroundColor =
                getColor(R.styleable.LoadingButton_backgroundButtonColor, Color.GRAY)

            // Progress Circle
            circularProgressColor =
                getColor(R.styleable.LoadingButton_circleProgressColor, Color.YELLOW)

            // Label
            btnText = getString(R.styleable.LoadingButton_text).toString()

        }

        // Set the Button State to: Clicked
        buttonState = ButtonState.Clicked

    }

    /**
     * Button: Text
     */
    private val btnLabelSettings = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {

        // Color Fill Mode
        style = Paint.Style.FILL

        // Bold font weight
        typeface = Typeface.create("", Typeface.BOLD)

        // Font Size
        textSize = 20.0f * resources.displayMetrics.density

        // Text Alignment
        textAlign = Paint.Align.CENTER

        // Text Color
        color = Color.WHITE

    }

    // Button: Background
    private val btnBackgroundSettings = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        // Choose FILL style for the background
        style = Paint.Style.FILL
        color = btnBackgroundColor

    }

    // Circular Shape
    private val circularAnimationSettings = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        // Choose FILL style for the background
        style = Paint.Style.FILL
        color = circularProgressColor

    }

    /**
     * 01 Create the Custom Button - onDraw()
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {

            //
            buttonBackground(canvas)

            when (buttonState) {
                ButtonState.Loading -> {
                    buttonBackground(canvas, btnProgressAnimation)
                    drawArc(
                        circularProgress,
                        -180f,
                        btnProgressAnimation * 360,
                        true,
                        circularAnimationSettings
                    )

                    buttonText(canvas, resources.getString(R.string.button_loading))
                }

                ButtonState.Completed -> {
                    buttonText(canvas, resources.getString(R.string.button_download_completed))
                }

                else -> {
                    buttonText(canvas, btnText)
                }
            }

            invalidate()

        }
    }

    /**
     * 03 Background: drawBackgroundButton
     */
    private fun buttonBackground(canvas: Canvas?, progress: Float? = null) {

        canvas?.apply {

            //
            if (progress != null) {
                btnBackgroundSettings.alpha = 220
            }

            //
            drawRect(
                0f,
                0f,
                width.toFloat() * (progress ?: 1f),
                height.toFloat(),
                btnBackgroundSettings
            )

        }

    }

    /**
     * 04 Button Label: drawTextButton
     */
    private fun buttonText(

        // Pass the reference to the Canvas
        canvas: Canvas?,

        // Set the supplied Label for the Button
        btnLabel: String

    ) {
        canvas?.apply {

            // Position: X
            val posTextX = (width / 2).toFloat()

            // Position: Y
            val posTextY = ((height - (btnLabelSettings.descent() + btnLabelSettings.ascent())) / 2)

            // Draw the Button, with custom parameters
            drawText(

                // Set the button's label
                btnLabel,

                // X Position
                posTextX,

                // Y Position
                posTextY,

                // Apply styles for (1) Fill Style, (2) Typeface, (3) Font Size, (4) Text Alignment, (5) Text Color
                btnLabelSettings
            )
        }
    }

    /**
     * 02 - onMeasure
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth

        //
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)

        //
        val h: Int = resolveSizeAndState(

            //
            MeasureSpec.getSize(w),

            //
            heightMeasureSpec,

            //
            0
        )

        //
        widthSize = w

        //
        heightSize = h

        //
        setMeasuredDimension(w, h)

    }


}
