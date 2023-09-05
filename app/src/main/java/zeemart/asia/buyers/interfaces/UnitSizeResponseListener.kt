package zeemart.asia.buyers.interfaces

import com.android.volley.VolleyError
import zeemart.asia.buyers.models.UnitSizeModel

/**
 * Created by ParulBhandari on 12/18/2017.
 */
interface UnitSizeResponseListener {
    fun onSuccessResponse(unitSizeMap: Map<String?, UnitSizeModel?>?)
    fun onErrorResponse(error: VolleyError?)
}