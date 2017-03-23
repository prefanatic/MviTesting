package io.prefanatic.mvitesting.list

import io.prefanatic.mvitesting.Fruit
import io.prefanatic.mvitesting.MviState

/**
 * Created by cgoldberg02 on 3/22/17.
 */
sealed class FruitListState

data class Error(
        val throwable: Throwable
) : FruitListState()

class Loading : FruitListState()

data class Result(
        val items: List<Fruit>
) : FruitListState()

data class NetworkResult(
        val imageUrl: String
) : FruitListState()