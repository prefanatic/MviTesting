package io.prefanatic.mvitesting

/**
 * Created by cgoldberg02 on 3/23/17.
 */
sealed class MviState
data class Error(val throwable: Throwable) : MviState()
class Loading() : MviState()