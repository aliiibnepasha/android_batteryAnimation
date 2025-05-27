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
        List(16) { index -> "w_emoji_${index + 1}" }
    }

    val widgetListAction: List<String> by lazy {
        List(11) { index -> "widget_action_${index + 1}" }
    }

    val widgetListBasic: List<String> by lazy {
        List(7) { index -> "widget_basic_${index + 1}" }
    }

    val widgetListCute: List<String> by lazy {
        List(10) { index -> "widget_cute_${index + 1}" }
    }

    val widgetListFantasy: List<String> by lazy {
        List(8) { index -> "widget_fantasy_${index + 1}" }
    }

    //  Emoji List

    val emojiListFantasy: List<String> by lazy {
        List(25) { index -> "emoji_${index + 1}" }
    }
    val emojiAnimListFantasy: List<String> by lazy {
        List(8) { index -> "emoji_anim_${index + 1}" }
    }
    val emojiBasicListFantasy: List<String> by lazy {
        List(4) { index -> "emoji_basic_${index + 1}" }
    }
    val emojiCuteListFantasy: List<String> by lazy {
        List(28) { index -> "emoji_cute_${index + 1}" }
    }
    val emojiFashionListFantasy: List<String> by lazy {
        List(10) { index -> "emoji_fashion_${index + 1}" }
    }
    val emojiComicListFantasy: List<String> by lazy {
        List(14) { index -> "emoji_comic_${index + 1}" }
    }


}