package pl.fithubapp.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.NumberPicker
import pl.fithubapp.R

/**
 * Tworzy AlertDialog.Builder z niestandardowym stylem Fithub
 */
fun Context.createStyledDialogBuilder(): AlertDialog.Builder {
    return AlertDialog.Builder(this, R.style.ThemeOverlay_Fithub_Dialog)
}

/**
 * Ustawia czarny kolor tekstu dla NumberPicker (fix dla białego tekstu na białym tle)
 */
fun NumberPicker.setTextColorToPrimary(context: Context) {
    try {
        val selectorWheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
        selectorWheelPaintField.isAccessible = true
        (selectorWheelPaintField.get(this) as? android.graphics.Paint)?.color = 
            context.resources.getColor(R.color.text_primary, null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
