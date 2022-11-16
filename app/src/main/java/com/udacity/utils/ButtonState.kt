package com.udacity.utils

/**
 * We are defining 3 different states for our custom Button States:
 * 1. Clicked,
 * 2. Loading and
 * 3. Completed
 */
sealed class ButtonState {

    // State: Clicked ?
    object Clicked : ButtonState()

    // State: Loading ?
    object Loading : ButtonState()

    // State: Completed ?
    object Completed : ButtonState()

}
