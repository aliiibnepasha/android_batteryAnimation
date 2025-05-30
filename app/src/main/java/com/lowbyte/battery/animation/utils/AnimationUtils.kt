package com.lowbyte.battery.animation.utils

object AnimationUtils {
    // Lotti Animations
    val animationList: List<String> by lazy {
        List(14) { index -> "anim_${index + 1}" }
    }
  // Lotti Animations
    val animationListNew: List<String> by lazy {
        List(17) { index -> "a_${index + 1}" }
    }

    //  Widgets List all Cat
    val widgetListW: List<String> by lazy {
        List(12) { index -> "widget_${index + 1}" }
    }

    val widgetListAction: List<String> by lazy {
        List(12) { index -> "widget_${index + 24}" }
    }

    val widgetListBasic: List<String> by lazy {
        List(12) { index -> "widget_${index + 36}" }
    }

    val widgetListCute: List<String> by lazy {
        List(12) { index -> "widget_${index + 48}" }
    }

    val widgetListFantasy: List<String> by lazy {
        List(12) { index -> "widget_${index + 1}" }
    }

    //  Emoji List

    val emojiListFantasy: List<String> by lazy {
        List(22) { index -> "emoji_${index + 1}" }
    }
    val emojiAnimListFantasy: List<String> by lazy {
        List(22) { index -> "emoji_${index + 22}" }
    }
    val emojiBasicListFantasy: List<String> by lazy {
        List(22) { index -> "emoji_${index + 44}" }
    }
    val emojiCuteListFantasy: List<String> by lazy {
        List(22) { index -> "emoji_${index + 67}" }
    }
    val emojiFashionListFantasy: List<String> by lazy {
        List(22) { index -> "emoji_${index + 90}" }
    }
    val emojiComicListFantasy: List<String> by lazy {
        List(23) { index -> "emoji_${index + 1}" }
    }


}