package zeemart.asia.buyers.models.inventory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref

open class Shelve {
    @SerializedName("shelveId")
    @Expose
    var shelveId: String? = null

    @SerializedName("shelveName")
    @Expose
    var shelveName: String? = null

    @Expose
    var timeLastCounted: Long? = null

    class ShelveFilter : Shelve() {
        @Expose
        var isShelveSelected = false
    }

    companion object {
        @JvmStatic
        fun saveShelveListToSharedPrefs(shelveList: List<ShelveFilter>?) {
            val shelveListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(shelveList)
            SharedPref.write(SharedPref.SHELVE_FILTER_LIST, shelveListJson)
        }

        @JvmStatic
        val shelveListFromSharedPrefs: List<ShelveFilter>
            get() {
                val shelveListJson =
                    SharedPref.read(SharedPref.SHELVE_FILTER_LIST, null)
                if (shelveListJson != null) {
                    val listType = object : TypeToken<List<ShelveFilter>>() {}.type
                    return ZeemartBuyerApp.gsonExposeExclusive.fromJson(shelveListJson, listType)
                } else {
                    // Handle the case where shelveListJson is null
                    // For example, you can return an empty list or throw an exception.
                    return emptyList()
                }

            }

        @JvmStatic
        fun clearSharedPrefShelveList() {
            val shelveFilterList = shelveListFromSharedPrefs
            if (shelveFilterList != null) {
                for (i in shelveFilterList.indices) {
                    shelveFilterList[i].isShelveSelected = false
                }
                saveShelveListToSharedPrefs(shelveFilterList)
            }
        }
    }
}