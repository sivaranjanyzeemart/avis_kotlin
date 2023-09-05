package zeemart.asia.buyers.helper

import android.view.View

open interface LoaderInteface {
    fun displayLoader(view: View?)
    fun hideLoader(view: View?)
}