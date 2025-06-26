package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.model.Language

object AnimationUtils {
    const val BROADCAST_ACTION = "com.lowbyte.UPDATE_STATUSBAR"
    const val BROADCAST_ACTION_NOTIFICATION = "com.lowbyte.CUSTOM_NOTIFICATION_UPDATE"
    const val EXTRA_POSITION = "EXTRA_POSITION"
    const val EXTRA_LABEL = "EXTRA_LABEL"

    const val SKU_WEEKLY = "weekly"
    const val SKU_MONTHLY = "monthly"
    const val SKU_YEARLY = "annual"
     var initialLanguageCode = ""

// TODO Update Native Id
    fun getNativeLanguageId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/2606120023"
        }
    }
    fun getNativeOnBoardingId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/3042084357"
        }
    }
    fun getNativeCustomizeId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/2298998267"
        }
    }

     fun getNativeInsideId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/1729002688"
        }
    }


// TODO Update Banner Id


      fun getBannerId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9214589741"
        } else {
            "ca-app-pub-9844943887550892/2195014922"
        }
    }




  fun getBannerSplashId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9214589741"
        } else {
            "ca-app-pub-9844943887550892/5668247690"
        }
    }
// TODO Update Fullscreen Ad Id
    fun getFullscreenId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-9844943887550892/2857359904"
        }
    }

    fun getFullscreenSplashId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-9844943887550892/3919201698"
        }
    }
// TODO Update Open Ad Id

    fun getOpenAppId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9257395921"
        } else {
            "ca-app-pub-9844943887550892/3065243460"
        }
    }


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
    const val widgetEvenCount = (54 / 5) / 2 * 2 // = 10

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
    const val evenCount = (92 / 6) / 2 * 2 // Force even count


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
    fun Fragment.showCustomToast(strMessage: String) {
        requireActivity().let { mContext ->
            Toast.makeText(mContext.applicationContext, strMessage, Toast.LENGTH_SHORT).show()

        }
    }

    fun Activity.showCustomToast(strMessage: String) {
        this.let { mContext ->

            Toast.makeText(mContext.applicationContext, strMessage, Toast.LENGTH_SHORT).show()

        }
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

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open URL", Toast.LENGTH_SHORT).show()
        }
    }

}