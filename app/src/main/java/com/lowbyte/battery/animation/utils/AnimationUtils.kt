package com.lowbyte.battery.animation.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.model.Language

object AnimationUtils {

    val combinedAnimationList: List<String> by lazy {
       animationListNew
    }

    // Lotti Animations
    val animationList: List<String> by lazy {
        List(14) { index -> "anim_${index + 1}" }
    }
  // Lotti Animations
    val animationListNew: List<String> by lazy {
        List(15) { index -> "a_${index + 1}" }
    }

    // Total widgets
    val allWidgets: List<String> = List(54) { i -> "widget_${i + 1}" }

    // Force even count per group
    val widgetEvenCount = (54 / 5) / 2 * 2 // = 10
    val widgetLists = List(5) { i ->
        allWidgets.subList(i * widgetEvenCount, (i + 1) * widgetEvenCount)
    }

    // Final categorized widget lists
    val widgetListFantasy = widgetLists[0]
    val widgetListAction = widgetLists[1]
    val widgetListBasic = widgetLists[2]
    val widgetListCute = widgetLists[3]
    val widgetListFashion = widgetLists[4]



    //  Emoji List
    val evenCount = (92 / 6) / 2 * 2 // Force even count
    val emojis = List(92) { i -> "emoji_${i + 1}" }

    val lists = List(6) { i ->
        emojis.subList(i * evenCount, (i + 1) * evenCount)
    }

    val allEmojis = List(92) { index -> "emoji_${index + 1}" }

    val emojiListFantasy = lists[0]
    val emojiAnimListFantasy = lists[1]
    val emojiBasicListFantasy = lists[2]
    val emojiCuteListFantasy = lists[3]
    val emojiFashionListFantasy = lists[4]
    val emojiComicListFantasy = lists[5]


    fun getTabTitlesEmoji(context: Context): List<String> {
        return listOf(
            context.getString(R.string.all),
            context.getString(R.string.cat_1),
            context.getString(R.string.cat_2),
            context.getString(R.string.cat_3),
            context.getString(R.string.cat_4),
            context.getString(R.string.cat_5),
            context.getString(R.string.cat_6)
        )
    }

  fun getTabTitlesWidget(context: Context): List<String> {
        return listOf(
            context.getString(R.string.all),
            context.getString(R.string.cat_1),
            context.getString(R.string.cat_2),
            context.getString(R.string.cat_3),
            context.getString(R.string.cat_4),
            context.getString(R.string.cat_5)
        )
    }

    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language("English", "en"),
            Language("العربية", "ar"),           // Arabic
            Language("Español", "es-rES"),       // Spanish (Spain)
            Language("Français", "fr-rFR"),      // French (France)
            Language("हिंदी", "hi"),             // Hindi
            Language("Italiano", "it-rIT"),      // Italian
            Language("日本語", "ja"),             // Japanese
            Language("한국어", "ko"),             // Korean
            Language("Bahasa Melayu", "ms-rMY"), // Malay (Malaysia)
            Language("Filipino", "phi"),         // Filipino
            Language("ไทย", "th"),               // Thai
            Language("Türkçe", "tr-rTR"),        // Turkish (Turkey)
            Language("Tiếng Việt", "vi"),        // Vietnamese
            Language("Português", "pt-rPT"),     // Portuguese (Portugal)
            Language("Bahasa Indonesia", "in")   // Indonesian
        )
    }

    fun Context.vibrateClick(duration: Long = 5000) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }
}