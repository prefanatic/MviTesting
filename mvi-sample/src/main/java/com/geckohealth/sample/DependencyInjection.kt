package io.prefanatic.sample

import io.prefanatic.sample.persistence.PersistencePresenter
import io.prefanatic.sample.persistence.PersistencePresenterImpl

/**
 * This just mimics how scoped DI would work, so we can test Presenter persistence and such.
 */
object DependencyInjection {
    val persistencePresenter: PersistencePresenter by lazy { PersistencePresenterImpl() }
}