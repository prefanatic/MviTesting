package io.prefanatic.sample.list

import io.prefanatic.sample.Fruit
import com.geckohealth.mvi.MviState

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