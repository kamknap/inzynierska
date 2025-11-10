package com.example.fithub

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.example.fithub.data.UserWeightHistoryDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.graphics.Paint
import android.graphics.Path
import android.text.TextPaint
import java.util.Date


// TODO wykres crashuje jak nie ma danych na dany tydzien
class WeightChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr){

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private var data: List<UserWeightHistoryDto> = emptyList()

    data class ParsedPoint(
        val date: Date,
        val weight: Float
    )

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = dp(2f)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3F51B5")
        strokeWidth = dp(3f)
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722")
        style = Paint.Style.FILL
    }

    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(11f)
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
    }

    private val weightPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(11f)
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    // Marginesy
    private val leftPadding = dp(40f)
    private val rightPadding = dp(20f)
    private val topPadding = dp(30f)
    private val bottomPadding = dp(50f)

    private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    fun setData(weightHistory: List<UserWeightHistoryDto>) {
        this.data = weightHistory.sortedBy { it.measuredAt }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) {
            drawEmptyState(canvas)
            return
        }

        val parsedData = data.mapNotNull { history ->
            try {
                ParsedPoint(
                    date = isoFormat.parse(history.measuredAt)!!,
                    weight = history.weightKg.toFloat()
                )
            } catch (e: Exception) {
                null
            }
        }

        if (parsedData.isEmpty()) {
            drawEmptyState(canvas)
            return
        }

        val chartWidth = width - leftPadding - rightPadding
        val chartHeight = height - topPadding - bottomPadding
        val bottomY = height - bottomPadding

        // Zakresy
        val minWeight = parsedData.minOf { it.weight } - 1
        val maxWeight = parsedData.maxOf { it.weight } + 1


        // Osie
        canvas.drawLine(leftPadding, topPadding, leftPadding, bottomY, axisPaint)
        canvas.drawLine(leftPadding, bottomY, width - rightPadding, bottomY, axisPaint)

        // Linia wykresu
        val path = Path()
        parsedData.forEachIndexed { index, point ->
            val x = leftPadding + (index.toFloat() / (parsedData.size - 1).coerceAtLeast(1)) * chartWidth
            val y = topPadding + (1f - (point.weight - minWeight) / (maxWeight - minWeight)) * chartHeight
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        val maxLabels = 5
        val step = if (parsedData.size > 1) {
            (parsedData.size - 1) / (maxLabels - 1).coerceAtLeast(1)
        } else {
            1
        }

        // Punkty i etykiety
        parsedData.forEachIndexed { index, point ->
            val x = leftPadding + (index.toFloat() / (parsedData.size - 1).coerceAtLeast(1)) * chartWidth
            val y = topPadding + (1f - (point.weight - minWeight) / (maxWeight - minWeight)) * chartHeight

            canvas.drawCircle(x, y, dp(5f), pointPaint)

            canvas.drawText("${point.weight}", x, y - dp(8f), weightPaint)

            if (index == 0 || index == parsedData.size - 1 || (index > 0 && index % step == 0)) {
                canvas.drawText(dateFormat.format(point.date), x, bottomY + dp(20f), labelPaint)
            }
        }

        //os Y
        val weightStep = 2.0f
        var currentWeight = (minWeight / weightStep).toInt() * weightStep
        while (currentWeight <= maxWeight) {
            if (currentWeight > minWeight) { // Dodaj ten warunek
                val y = topPadding + (1f - (currentWeight - minWeight) / (maxWeight - minWeight)) * chartHeight

                canvas.drawText("${currentWeight.toInt()}kg", leftPadding - dp(10f), y, labelPaint.apply {
                    textAlign = Paint.Align.RIGHT
                })
            }
            currentWeight += weightStep
        }
        labelPaint.textAlign = Paint.Align.CENTER
    }

    private fun drawEmptyState(canvas: Canvas) {
        val text = "Brak danych do wyświetlenia"
        val x = width / 2f
        val y = height / 2f
        canvas.drawText(text, x, y, labelPaint)
    }

    private fun dp(dp: Float): Float = dp * resources.displayMetrics.density
    private fun sp(sp: Float): Float = sp * resources.displayMetrics.scaledDensity
}