package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose

/**
 * Created by ParulBhandari on 1/23/2018.
 *
 */
class ProductAttributeModel {
    @Expose
    var specificAttribute: String? = null

    @Expose
    var specificAttributeValue: List<String>? = null
}