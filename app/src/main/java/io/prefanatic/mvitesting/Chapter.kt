package io.prefanatic.mvitesting

import com.geckohealth.Chapter
import com.geckohealth.Diary
import com.geckohealth.LogCompliance

/**
 * A Diary-compliant "relay".  This Chapter will act as a pass-through to an assigned
 * [Chapter] from [Diary]
 */
object Chapter : LogCompliance {
    var chapter: Chapter? = null

    override fun a(msg: String, vararg args: Any) {
        chapter?.a(msg, *args)
    }

    override fun d(msg: String, vararg args: Any) {
        chapter?.d(msg, *args)
    }

    override fun e(msg: String, vararg args: Any) {
        chapter?.e(msg, *args)
    }

    override fun e(e: Throwable, msg: String, vararg args: Any) {
        chapter?.e(e, msg, *args)
    }

    override fun i(msg: String, vararg args: Any) {
        chapter?.i(msg, *args)
    }

    override fun v(msg: String, vararg args: Any) {
        chapter?.v(msg, *args)
    }

    override fun w(msg: String, vararg args: Any) {
        chapter?.w(msg, *args)
    }
}