package com.lowbyte.battery.animation.utils

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



}