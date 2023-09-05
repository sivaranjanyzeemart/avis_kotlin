package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref

enum class StockageType(val displayName: String, val resId: Int) {
    STOCK_COUNT("STOCK_COUNT", R.string.txt_stock_count), ADJUSTMENT(
        "ADJUSTMENT",
        R.string.txt_adjustment
    ),
    STOCK_COUNT_AMENDMENTS(
        "STOCK_COUNT_AMENDMENTS",
        R.string.txt_stock_count_amendment
    ),
    UOM_CONVERSION("UOM_CONVERSION", R.string.txt_stock_count_uom_conversion);

    class StockageTypeFilter {
        @Expose
        var stockageType: StockageType? = null

        @Expose
        var isStockageTypeSelected = false

//        fun isStockageTypeSelected(): Boolean {
//            return isStockageTypeSelected
//        }
//
//        fun setStockageTypeSelected(stockageTypeSelected: Boolean) {
//            isStockageTypeSelected = stockageTypeSelected
//        }
    }

    companion object {
        @JvmStatic
        fun initializeStockTypeListToSharedPrefs() {
            val stockageTypeFilterList: MutableList<StockageTypeFilter> = ArrayList()
            for (stockageType in values()) {
                val stock = StockageTypeFilter()
                stock.stockageType = stockageType
                stock.isStockageTypeSelected = false
                stockageTypeFilterList.add(stock)
            }
            val stockTypeJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(stockageTypeFilterList)
            SharedPref.write(SharedPref.STOCK_TYPES, stockTypeJson)
        }

        @JvmStatic
        fun updateStockListInSharedPrefs(stockageTypeFilterList: List<StockageTypeFilter?>?) {
            val stockTypeJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(stockageTypeFilterList)
            SharedPref.write(SharedPref.STOCK_TYPES, stockTypeJson)
        }

        @JvmStatic
        val stockTypeListFromSharedPrefs: List<StockageTypeFilter>
            get() {
                val stockTypeJson = SharedPref.read(SharedPref.STOCK_TYPES, null)

                if (stockTypeJson != null) {
                    return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                        stockTypeJson,
                        object : TypeToken<List<StockageTypeFilter?>?>() {}.type
                    )
                } else {
                    return emptyList()
                }
            }
    }
}
