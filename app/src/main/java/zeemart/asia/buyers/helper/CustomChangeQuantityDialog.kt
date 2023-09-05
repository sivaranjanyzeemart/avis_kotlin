package zeemart.asia.buyers.helperimportimport

import android.app.Dialog
import android.content.Context

/**
 * Created by ParulBhandari on 8/13/2018.
 */
abstract class CustomChangeQuantityDialog(context: Context?, themeResId: Int) : Dialog(
    context!!, themeResId
) {
    abstract fun onUnitSizeSelectedCalled(d: CustomChangeQuantityDialog)
}