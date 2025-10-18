package com.example.zave

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class required by Hilt.
 * The @HiltAndroidApp annotation triggers Hilt's code generation,
 * setting up the component graph for dependency injection.
 */
@HiltAndroidApp
class ZaveApplication : Application()