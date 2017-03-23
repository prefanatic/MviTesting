package io.prefanatic.mvitesting.detail

/**
 * Created by cgoldberg02 on 3/23/17.
 */

sealed class FruitDetailState

data class Result(
        val imageUrl: String
) : FruitDetailState()

data class Error(
        val throwable: Throwable
) : FruitDetailState()

class Loading() : FruitDetailState()