package zeemart.asia.buyers.models.marketlist

import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref
import java.util.*

/**
 * Created by ParulBhandari on 7/25/2018.
 */
object DetailSupplierMgr : DetailSupplierDataModel() {
    //    @Expose
    //    private boolean isSupplierSelected;
    //
    //    public boolean isSupplierSelected() {
    //        return isSupplierSelected;
    //    }
    //
    //    public void setSupplierSelected(boolean supplierSelected) {
    //        isSupplierSelected = supplierSelected;
    //    }//sort the supplier list
    /**
     * save the list of suppliers to shared preferences
     *
     * @param supplierFiltersList
     */
    /**
     * fetch the supplier list for filtering on supplier
     * in Deliveries and View Order screen
     *
     * @return List of supplier filters
     */
    @JvmStatic
    var supplierFilters: MutableList<DetailSupplierMgr>?
        get() {
            val supplierFiltersJson = SharedPref.read(SharedPref.SUPPLIER_FILTERS_LIST, "")
            val supplierFiltersList =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<MutableList<DetailSupplierMgr>>(
                    supplierFiltersJson,
                    object : TypeToken<List<DetailSupplierMgr?>?>() {}.type
                )
            //sort the supplier list
            if (supplierFiltersList != null) {
                supplierFiltersList.sortWith(Comparator { o1, o2 ->
                    o2.supplier.supplierName?.lowercase(Locale.getDefault())?.let {
                        o1.supplier.supplierName?.lowercase(
                            Locale.getDefault()
                        )?.compareTo(it)
                    }!!
                })
            } else {
                return ArrayList()
            }
            return supplierFiltersList
        }
        set(supplierFiltersList) {
            val deDupStringList: List<DetailSupplierMgr> = ArrayList(HashSet(supplierFiltersList))
            val filterSupplierListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(deDupStringList)
            SharedPref.write(SharedPref.SUPPLIER_FILTERS_LIST, filterSupplierListJson)
        }

    @JvmStatic
    fun clearAllSupplierFilterData() {
        SharedPref.removeVal(SharedPref.SUPPLIER_FILTERS_LIST)
    }

    /**
     * get the list of selected supplier list
     *
     * @return
     */
    @JvmStatic
    val selectedSupplierFilter: List<DetailSupplierMgr>?
        get() {
            val supplierList: MutableList<DetailSupplierMgr>? = supplierFilters
            if (supplierList != null) {
                val itr = supplierList.iterator()
                while (itr.hasNext()) {
                    val supplierData = itr.next()
                    if (!supplierData.isSupplierSelected) {
                        itr.remove()
                    }
                }
            }
            return supplierList
        }
}