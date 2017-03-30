package io.prefanatic.mvitesting

import io.prefanatic.mvitesting.persistence.PersistencePresenter
import io.prefanatic.mvitesting.persistence.PersistencePresenterImpl

/**
 * This just mimics how scoped DI would work, so we can test Presenter persistence and such.
 */
object DependencyInjection {
    val persistencePresenter: PersistencePresenter by lazy { PersistencePresenterImpl() }
}