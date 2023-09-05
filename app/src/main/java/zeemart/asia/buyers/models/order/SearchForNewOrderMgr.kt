package zeemart.asia.buyers.models.order

import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.modelsimport.SearchEssentialAndDealsModel
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsSuppliers
import java.util.*

class SearchForNewOrderMgr {
    var isHeader = false
    var header: String? = null
    var isViewAll = false
    var viewAllFor: String? = null
    var detailSupplierDataModel: DetailSupplierDataModel? = null
    lateinit var productDetailBySupplier: ProductDetailBySupplier
    var suppliersList: SearchedEssentialAndDealsSuppliers? = null
    var categoriesList: SearchEssentialAndDealsModel.Category? = null
    var headerCount = 0


    companion object {
        fun sortByProductName(productList: List<SearchForNewOrderMgr?>?) {
            Collections.sort(productList, object : Comparator<SearchForNewOrderMgr?> {
                override fun compare(o1: SearchForNewOrderMgr?, o2: SearchForNewOrderMgr?): Int {
                    return o1?.productDetailBySupplier!!.productName!!.compareTo(o2?.productDetailBySupplier!!.productName!!)
                }
            })
        }
    }
}