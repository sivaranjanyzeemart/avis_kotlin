package zeemart.asia.buyers.network

import com.android.volley.NetworkResponse
import com.android.volley.VolleyError

/**
 * Created by ParulBhandari on 11/22/2017.
 */

class VolleyErrorHelper constructor(
    var errorType: String,
    var errorMessage: String,
    response: NetworkResponse?
) : VolleyError() {
    val networkResponse: NetworkResponse = response ?:
    NetworkResponse(0, null, false, 0, emptyList())
}

//class VolleyErrorHelper constructor(
//    var errorType: String,
//    var errorMessage: String,
//    response: NetworkResponse?
//) : VolleyError() {
//    var networkResponse: NetworkResponse = TODO()
//        get() {
//            return networkResponse
//        }
//
//    init {
//        this.networkResponse = response
//    }
//}