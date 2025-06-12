package com.elasticrock.keepscreenon.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode

fun reviewPrompt(context: Context, activity: Activity) {
    val reviewManager = ReviewManagerFactory.create(context)
    reviewManager.requestReviewFlow().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            reviewManager.launchReviewFlow(activity, task.result)
        } else {
            @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
            Log.e("ReviewPrompt", "The review process failed with error code $reviewErrorCode")
        }
    }
}