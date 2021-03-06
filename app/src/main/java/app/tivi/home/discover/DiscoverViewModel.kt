/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.home.discover

import android.arch.lifecycle.MutableLiveData
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.datasources.trakt.PopularDataSource
import app.tivi.datasources.trakt.TrendingDataSource
import app.tivi.home.HomeFragmentViewModel
import app.tivi.home.HomeNavigator
import app.tivi.interactors.PopularShowsInteractor
import app.tivi.interactors.TrendingShowsInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.trakt.TraktManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.NetworkDetector
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
    schedulers: AppRxSchedulers,
    popularDataSource: PopularDataSource,
    private val popularShowsInteractor: PopularShowsInteractor,
    trendingDataSource: TrendingDataSource,
    private val trendingShowsInteractor: TrendingShowsInteractor,
    traktManager: TraktManager,
    tmdbManager: TmdbManager,
    private val networkDetector: NetworkDetector,
    logger: Logger
) : HomeFragmentViewModel(traktManager, logger) {

    val data = MutableLiveData<DiscoverViewState>()

    init {
        disposables += Flowables.combineLatest(
                trendingDataSource.data(0),
                popularDataSource.data(0),
                tmdbManager.imageProvider,
                ::DiscoverViewState)
                .observeOn(schedulers.main)
                .subscribe(data::setValue, logger::e)

        refresh()
    }

    private fun refresh() {
        disposables += networkDetector.waitForConnection()
                .subscribe({ onRefresh() }, logger::e)
    }

    private fun onRefresh() {
        launchInteractor(popularShowsInteractor.asRefreshInteractor(), Unit)
        launchInteractor(trendingShowsInteractor.asRefreshInteractor(), Unit)
    }

    fun onTrendingHeaderClicked(navigator: HomeNavigator, sharedElementHelper: SharedElementHelper? = null) {
        navigator.showTrending(sharedElementHelper)
    }

    fun onPopularHeaderClicked(navigator: HomeNavigator, sharedElementHelper: SharedElementHelper? = null) {
        navigator.showPopular(sharedElementHelper)
    }

    fun onItemPostedClicked(navigator: HomeNavigator, show: TiviShow, sharedElements: SharedElementHelper?) {
        navigator.showShowDetails(show, sharedElements)
    }
}
