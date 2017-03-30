package io.prefanatic.mvitesting

import android.app.Application
import com.geckohealth.AndroidPen
import com.geckohealth.AndroidStyle
import com.geckohealth.Diary

/**
 * Created by cgoldberg02 on 3/24/17.
 */
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        val page = Diary.buildPage {
            pen = AndroidPen()
            style = AndroidStyle()
            withCallingClass = true
        }
        Diary.addPage(page)

        Chapter.chapter = com.geckohealth.Chapter.Builder()
                .setId("MviFramework")
                .build()
    }
}