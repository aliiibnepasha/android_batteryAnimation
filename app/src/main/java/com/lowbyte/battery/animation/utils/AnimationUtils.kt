package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.model.Language

object AnimationUtils {

    var finishingLang = false
    var remoteJsonKey = "ads_config_v17"



    var  isNativeSplashEnabled = true //
    var  isBannerHomeEnabled = true //

    var  isBannerPermissionSettings = true //
    var  isNativeHomeEnabled = true //
    var  isFullscreenSplashEnabled = true //
    var  isRewardedEnabled = true //
    var  isFullscreenStatusEnabled = true //
    var  isFullscreenDynamicDoneEnabled = true //
    var  isFullscreenGestureEnabled = true //
    var  isFullscreenApplyEmojiEnabled = true //
    var  isFullscreenApplyWidgetEnabled = true//
    var  isFullscreenApplyAnimEnabled = true//
    var  isNativeLangFirstEnabled = true  //
    var  isNativeLangSecondEnabled = true //
    var  isNativeIntroEnabled = true //
    var  isNativeApplyEmojiEnabled = true //
    var  isNativeApplyWidgetEnabled = true//
    var  isNativeApplyAnimEnabled = true//
    var  isNativeStatusEnabled = true //

    var  isNativeDynamicEnabled = true //
    var  isNativeGestureEnabled = true



    const val BROADCAST_ACTION = "com.lowbyte.UPDATE_STATUSBAR"
    const val BROADCAST_ACTION_REMOVE = "com.lowbyte.UPDATE_REMOVE"
    const val BROADCAST_ACTION_NOTIFICATION = "com.lowbyte.CUSTOM_NOTIFICATION_UPDATE"
    const val BROADCAST_ACTION_DYNAMIC = "com.lowbyte.BROADCAST_ACTION_DYNAMIC"
    const val EXTRA_POSITION = "EXTRA_POSITION"
    const val EXTRA_LABEL = "EXTRA_LABEL"

    const val SKU_WEEKLY = "weekly"
    const val SKU_MONTHLY = "monthly"
    const val SKU_YEARLY = "annual"
     var initialLanguageCode = ""

// TODO Update Native Id
fun getRewardedId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            "ca-app-pub-9844943887550892/8218608589"
        }
}

    fun getNativeLanguageId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/2606120023"
        }
    }
    fun getNativeSplashId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/7637591449"
        }
    }

    fun getNativeOnBoardingId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/1653200236"
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

       fun getNativeHomeId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-9844943887550892/7239519082"
        }
    }






// TODO Update Banner Id


      fun getBannerId(isCollapsable: Boolean= false): String {
        return if (BuildConfig.DEBUG) {
           if (!isCollapsable) "ca-app-pub-3940256099942544/9214589741" else "ca-app-pub-3940256099942544/2014213617"
        } else {
            "ca-app-pub-9844943887550892/2195014922"
        }
    }




//  fun getBannerSplashId(): String {
//        return if (BuildConfig.DEBUG) {
//            "ca-app-pub-3940256099942544/9214589741"
//        } else {
//            "ca-app-pub-9844943887550892/5668247690"
//        }
//    }
     fun getBannerPermissionId(isCollapsable: Boolean= false): String {
         return if (BuildConfig.DEBUG) {
             if (!isCollapsable) "ca-app-pub-3940256099942544/9214589741" else "ca-app-pub-3940256099942544/2014213617"
         } else {
            "ca-app-pub-9844943887550892/5016648603"
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
    fun getFullscreenHome2Id(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-9844943887550892/7946381505"
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
  val animationListNew: List<String> = listOf(
      "a_5", "a_11", "a_14", "a_6", "a_4", "a_8", "a_12", "a_15", "a_1", "a_2", "a_3",
      "a_7", "a_9", "a_10",
      "a_13",
  )

    // Final categorized widget lists
    val widgetListFantasy = listOf(
        "widget_1", "widget_2", "widget_3", "widget_4", "widget_5",
        "widget_25",  "widget_36","widget_32", "widget_9",  "widget_27"
    )

    val widgetListAction = listOf(
        "widget_11", "widget_12", "widget_13", "widget_14", "widget_15",
        "widget_16", "widget_17", "widget_18", "widget_19", "widget_20"
    )

    val widgetListBasic = listOf(
        "widget_21", "widget_22", "widget_23", "widget_24", "widget_7",
        "widget_26", "widget_10" , "widget_28", "widget_29", "widget_30"
    )

    val widgetListCute = listOf(
        "widget_31","widget_6",  "widget_33", "widget_34", "widget_35",
         "widget_37", "widget_38", "widget_39", "widget_40","widget_8",
    )

    val widgetListFashion = listOf(
        "widget_41", "widget_42", "widget_43", "widget_44", "widget_45",
        "widget_46", "widget_47", "widget_48", "widget_49", "widget_50"
    )


    //0
    val emojiFashionListFantasy = listOf(
        "emoji_65", "emoji_78", "emoji_74",
        "emoji_75", "emoji_76", "emoji_71",
        "emoji_70", "emoji_69", "emoji_72",
        "emoji_73", "emoji_77", "emoji_79",
        "emoji_66", "emoji_67", "emoji_68", "emoji_80"
    )

    //1
    val emojiAnimListFantasy = listOf(

        "emoji_21", "emoji_24", "emoji_17", "emoji_18",
        "emoji_19", "emoji_20", "emoji_22", "emoji_23",
        "emoji_28", "emoji_29", "emoji_30", "emoji_31",
        "emoji_32","emoji_25", "emoji_26", "emoji_27"
    )

    //2
    val emojiBasicListFantasy = listOf(
        "emoji_33", "emoji_34", "emoji_35", "emoji_36",
        "emoji_37", "emoji_38", "emoji_39", "emoji_40",
        "emoji_41", "emoji_42", "emoji_43", "emoji_44",
        "emoji_45", "emoji_46", "emoji_47", "emoji_48")



    //3
    val emojiCuteListFantasy = listOf(

        "emoji_61",  "emoji_59","emoji_58", "emoji_63",
        "emoji_64", "emoji_60" , "emoji_57","emoji_49", "emoji_50",
        "emoji_51", "emoji_52", "emoji_53", "emoji_54",
        "emoji_55", "emoji_56", "emoji_62")



    //4
    val emojiListCartoon = listOf(
        "emoji_1", "emoji_15", "emoji_16", "emoji_14",
        "emoji_12", "emoji_9", "emoji_3",
        "emoji_4", "emoji_6", "emoji_7", "emoji_5",
        "emoji_8", "emoji_10", "emoji_11", "emoji_13", "emoji_2")


    //5
    val emojiComicListFantasy = listOf(
        "emoji_81", "emoji_82", "emoji_83", "emoji_84",
        "emoji_85", "emoji_86", "emoji_87", "emoji_88",
        "emoji_89", "emoji_90", "emoji_91", "emoji_92")


    val trendy = listOf(
        "trendy_1", "trendy_2", "trendy_3",
        "trendy_4", "trendy_5", "trendy_6",
        "trendy_7", "trendy_8", "trendy_9",
        "trendy_10", "trendy_11", "trendy_12",
        "trendy_13", "trendy_14", "trendy_15", "trendy_16",)


    val toy = listOf(
        "toy_1", "toy_2", "toy_3",
        "toy_4", "toy_5", "toy_6",
        "toy_7", "toy_8", "toy_9",
        "toy_10", "toy_11", "toy_12",
        "toy_13", "toy_14", "toy_15",
        "toy_16", "toy_17", "toy_18", "toy_19",)

    val emojiFace = listOf(
        "emoji_face_1", "emoji_face_2", "emoji_face_3",
        "emoji_face_4", "emoji_face_5", "emoji_face_6",
        "emoji_face_7", "emoji_face_8", "emoji_face_9",
        "emoji_face_10", "emoji_face_11", "emoji_face_12",
        "emoji_face_13", "emoji_face_14", "emoji_face_15",
        "emoji_face_16", "emoji_face_17", "emoji_face_18", "emoji_face_19",
    )
    val pet = listOf(
        "cat_1", "cat_2", "cat_3",
        "cat_4", "cat_5", "cat_6",
        "cat_7", "cat_8", "cat_9",
        "cat_10", "cat_11",
        "pet_1", "pet_2", "pet_3",
        "pet_4", "pet_5", "pet_6",
        "pet_7", "pet_8", "pet_9",
        "pet_10", "pet_11", "pet_12",)

    val cute = listOf(

        "cute_1", "cute_2", "cute_3",
        "cute_4", "cute_5", "cute_6", "cute_7",
        "cute_8", "cute_9", "cute_10", "cute_11",
        "cute_12", "cute_13", "cute_14", "cute_15",
    )


    fun getTabTitlesEmoji(context: Context): List<String> {
        return listOf(
            context.getString(R.string.trendy),
            context.getString(R.string.toy),
            context.getString(R.string.emoji_face),
            context.getString(R.string.pet),
            context.getString(R.string.cute),

            context.getString(R.string.cat_3),
            context.getString(R.string.cat_4),
            context.getString(R.string.cat_5),
            context.getString(R.string.cat_2),
            context.getString(R.string.cat_1)
        )
    }

  fun getTabTitlesWidget(context: Context): List<String> {
        return listOf(
            //  context.getString(R.string.all),
            context.getString(R.string.cat_6),
            context.getString(R.string.cat_3),
            context.getString(R.string.cat_4),
            context.getString(R.string.cat_5),
            context.getString(R.string.cat_1)
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

//     fun showAdLoadingDialog(activity: Activity, durationMillis: Long, onDialogDismiss: () -> Unit) {
//        val dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
//        dialog.setContentView(R.layout.dialog_ad_loading)
//        dialog.setCancelable(false)
//        try {
//            dialog.show()
//            activity.window?.decorView?.postDelayed({
//                if (dialog.isShowing){
//                    dialog.dismiss()
//                }
//                onDialogDismiss()
//            }, durationMillis)
//        } catch (e: Exception) {
//            dialog.dismiss()
//            Log.e("AdManager", "Failed to show loading dialog: ${e.localizedMessage}")
//            onDialogDismiss()
//        }
//    }
fun Activity?.isValid(): Boolean {
    return this != null && !isFinishing && !isDestroyed
}
fun Activity?.is15SDK(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}


}