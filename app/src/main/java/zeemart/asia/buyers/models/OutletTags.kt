package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import java.util.*

/**
 * Created by Rajprudhvi Marella on 07/9/2018.
 */
class OutletTags {
    @Expose
    var categoryName: String? = null

    @Expose
    var isCategorySelected = false

    @Expose
    var id: String? = null

    internal constructor(f: String?, b: Boolean, id: String?) {
        categoryName = f
        isCategorySelected = b
        this.id = id
    }

    constructor(o: OutletTags) : this((o.categoryName), o.isCategorySelected, o.id) {}
    constructor() {}

    override fun toString(): String {
        return "OutletTags{" +
                "categoryName='" + categoryName + '\'' +
                ", isCategorySelected=" + isCategorySelected +
                ", id='" + id + '\'' +
                '}'
    }

    companion object {
        @JvmStatic
        fun sortByCategoryName(outletTags: List<OutletTags>?) {
            if (outletTags != null) {
                Collections.sort(outletTags) { o1, o2 ->
                    o1.categoryName!!.compareTo(
                        o2.categoryName!!
                    )
                }
            }
        }
    }
}