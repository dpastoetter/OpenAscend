package com.openascend.app.share

import android.content.Context
import com.openascend.app.R

object ShareCardCopy {
    fun tagline(context: Context): String = context.getString(R.string.share_tagline)
    fun storeCta(context: Context): String = context.getString(R.string.share_store_cta)
    fun disclaimer(context: Context): String = context.getString(R.string.share_disclaimer)
}
