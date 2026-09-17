package com.example.temacker.core.data.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

// Credential Manager needs an Activity context (Koin's androidContext() only gives Application) to attach its bottom sheet.
class CurrentActivityHolder(app: Application) : Application.ActivityLifecycleCallbacks {

    private var current: WeakReference<Activity>? = null

    val activity: Activity? get() = current?.get()

    init {
        app.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        current = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (current?.get() === activity) current = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
