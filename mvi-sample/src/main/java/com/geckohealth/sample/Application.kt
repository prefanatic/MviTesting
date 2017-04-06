package com.geckohealth.sample

import android.app.Application
import com.geckohealth.AndroidPen
import com.geckohealth.AndroidStyle
import com.geckohealth.Chapter
import com.geckohealth.Diary
import com.geckohealth.mvi.Chapter

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

        val chapter = Chapter.Builder()
                .setId("MviFramework")
                .build()
    }
}