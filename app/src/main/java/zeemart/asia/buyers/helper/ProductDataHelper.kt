package zeemart.asia.buyers.helper


import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.roundUpDoubleToLong
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.StocksList
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.marketlist.Favourite
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.marketlist.ProductDetailsBySearch
import zeemart.asia.buyers.models.orderimport.CreateDraftOrder
import zeemart.asia.buyers.models.orderimportimportimport.DealsProductsPaginated
import zeemart.asia.buyers.models.orderimportimportimport.EssentialProductsPaginated
import zeemart.asia.buyers.models.reportsimport.Products
import zeemart.asia.buyers.modelsimport.SearchedEssentialAndDealsPruducts
import zeemart.asia.buyers.modelsimport.SelfOnBoardingSearchEssentialAndDealsModel
import zeemart.asia.buyers.network.MarketListApi
import zeemart.asia.buyers.network.MarketListApi.addingFavouritesOutlet
import java.util.*

/**
 * Created by ParulBhandari on 8/10/2018.
 */
object ProductDataHelper {
    private const val FAVORITE_ID_SEPERATOR = "-"
    @JvmStatic
    fun updateFavSkuStatus(context: Context?, sku: String?, isFav: Boolean) {
        //call the API to update the favorite status
        val favItemList: MutableList<Favourite?> = ArrayList()
        val favRequestObject = Favourite()
        favRequestObject.isFavourite
        favRequestObject.sku = sku
        favItemList.add(favRequestObject)
        addingFavouritesOutlet(
            context!!,
            favItemList,
            object : MarketListApi.RequestResponseListener {
                override fun requestSuccessful() {}
                override fun requestError() {}
            })
    }

    fun getDisplayProductList(
        context: Context?,
        products: List<ProductDetailBySupplier>,
        outletId: String?,
        supplierId: String?
    ): List<ProductDetailBySupplier> {
        if (SharedPref.read(SharedPref.FAVORITE_ITEMS_MAP, null) != null) {
            val favSkus: MutableList<String?> = ArrayList()
            val savedJson = SharedPref.read(SharedPref.FAVORITE_ITEMS_MAP, "")
            val favItemSet = ZeemartBuyerApp.gsonExposeExclusive.fromJson<Set<String?>>(
                savedJson,
                object : TypeToken<Set<String?>?>() {}.type
            )
            for (i in products.indices) {
                val favItemKeyForProduct = createFavItemKey(outletId, supplierId, products[i].sku)
                if (favItemSet != null && favItemSet.contains(favItemKeyForProduct)) {
                    products[i].isFavourite = true
                    favSkus.add(products[i].sku)
                } else {
                    products[i].isFavourite = false
                }
            }
            //add the favorites from local to server
            val favoriteSkuList: MutableList<Favourite?> = ArrayList()
            if (favSkus != null && favSkus.size > 0) {
                for (i in favSkus.indices) {
                    val addFavRequest = Favourite()
                    addFavRequest.sku = favSkus[i]
                    addFavRequest.isFavourite
                    favoriteSkuList.add(addFavRequest)
                }
            }
            addingFavouritesOutlet(
                context!!,
                favoriteSkuList,
                object : MarketListApi.RequestResponseListener {
                    override fun requestSuccessful() {
                        SharedPref.removeVal(SharedPref.FAVORITE_ITEMS_MAP)
                    }

                    override fun requestError() {}
                })
        }
        Collections.sort(products, Comparator { o1, o2 ->
            val isFav1 = o1.isFavourite
            val isFav2 = o2.isFavourite
            val sComp1 = isFav2.compareTo(isFav1)
            if (sComp1 == 0) {
                var prodNameOne: String? = null
                var prodNameTwo: String? = null
                prodNameOne = if (StringHelper.isStringNullOrEmpty(o1.productCustomName)) {
                    o1.productName!!.trim { it <= ' ' }.lowercase(Locale.getDefault())
                } else {
                    o1.productCustomName?.trim { it <= ' ' }?.lowercase(Locale.getDefault())
                }
                prodNameTwo = if (StringHelper.isStringNullOrEmpty(o2.productCustomName)) {
                    o2.productName!!.trim { it <= ' ' }.lowercase(Locale.getDefault())
                } else {
                    o2.productCustomName?.trim { it <= ' ' }?.lowercase(Locale.getDefault())
                }
                /*String productName1 = o1.getProductName().trim().toLowerCase();
                        String productName2 = o2.getProductName().trim().toLowerCase();*/return@Comparator prodNameOne?.compareTo(
                    prodNameTwo!!
                )!!
            }
            sComp1
        })
        return products
    }

    fun createSelectedProductMap(selectedProducts: List<Product>?): Map<String, Product>? {
        return if (selectedProducts != null && selectedProducts.size > 0) {
            val selectedProductMap: MutableMap<String, Product> =
                HashMap()
            for (i in selectedProducts.indices) {
                val key = getKeyProductMap(
                    selectedProducts[i].sku,
                    selectedProducts[i].unitSize
                )
                selectedProductMap[key] = selectedProducts[i]
            }
            selectedProductMap
        } else {
            null
        }
    }

    @JvmStatic
    fun getKeyProductMap(sku: String?, unitSize: String): String {
        return sku + unitSize
    }

    /**
     * create Product object to be passed to Review order screen
     *
     * @param productDetailBySupplier
     * @return
     */
    fun createSelectedProductObject(productDetailBySupplier: ProductDetailBySupplier?): Product? {
        return if (productDetailBySupplier != null) {
            val item = Product()
            item.sku = productDetailBySupplier.sku
            item.productName = productDetailBySupplier.productName
            item.supplierProductCode = productDetailBySupplier.supplierProductCode
            item.customName = (productDetailBySupplier.productCustomName)
            val productPriceLists = productDetailBySupplier.priceList
            for (j in productPriceLists!!.indices) {
                if (productPriceLists[j].isStatus(ProductPriceList.UnitSizeStatus.VISIBLE) || productPriceLists[j].isStatus(
                        ProductPriceList.UnitSizeStatus.ACTIVE
                    )
                ) {
                    item.unitPrice = (productPriceLists[j].price)
                    item.unitSize = productPriceLists[j].unitSize!!
                    item.moq = (productPriceLists[j].moq)
                    var totalPrice: Double? = 0.0
                    if (productPriceLists[j].price != null) {
                        totalPrice = productPriceLists[j].price!!.amount
                        if (productPriceLists[j].moq != null) totalPrice =
                            totalPrice!! * productPriceLists[j].moq!!
                    }
                    val totalPriceDetails = PriceDetails()
                    totalPriceDetails.amount = totalPrice
                    totalPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.totalPrice = totalPriceDetails
                    item.quantity = (productPriceLists[j].moq!!)
                    item.unitSize = productPriceLists[j].unitSize!!
                    val unitPriceDetails = PriceDetails()
                    unitPriceDetails.amount = productPriceLists[j].price!!.amount
                    unitPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.unitPrice = (unitPriceDetails)
                    break
                }
            }
            item
        } else {
            null
        }
    }

    @JvmStatic
    fun createSelectedProductObjectForBelowPar(productDetailBySupplier: ProductDetailBySupplier?): Product? {
        return if (productDetailBySupplier != null) {
            val item = Product()
            item.sku = productDetailBySupplier.sku
            item.productName = productDetailBySupplier.productName
            item.supplierProductCode = productDetailBySupplier.supplierProductCode
            item.customName = (productDetailBySupplier.productCustomName)
            val productPriceLists = productDetailBySupplier.priceList
            for (j in productPriceLists!!.indices) {
                if (productPriceLists[j].isStatus(ProductPriceList.UnitSizeStatus.VISIBLE) || productPriceLists[j].isStatus(
                        ProductPriceList.UnitSizeStatus.ACTIVE
                    )
                ) {
                    item.unitPrice = (productPriceLists[j].price)
                    item.unitSize = productPriceLists[j].unitSize!!
                    item.moq = (productPriceLists[j].moq)
                    var totalPrice: Double? = 0.0
                    if (productPriceLists[j].price != null) {
                        totalPrice = productPriceLists[j].price!!.amount
                        if (productPriceLists[j].moq != null) totalPrice =
                            totalPrice!! * productPriceLists[j].moq!!
                    }
                    val totalPriceDetails = PriceDetails()
                    totalPriceDetails.amount = totalPrice
                    totalPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.totalPrice = totalPriceDetails
                    var onHand: Double? = 0.0
                    if (productPriceLists[j].onHandQty != null) {
                        onHand = productPriceLists[j].onHandQty
                    }
                    var value = productPriceLists[j].parLevel!! - onHand!!
                    value = Math.ceil(value)
                    if (value > productPriceLists[j].moq!!) {
                        item.quantity = (value)
                    } else {
                        item.quantity = (productPriceLists[j].moq!!)
                    }
                    //                    item.quantity = (productPriceLists.get(j).getMoq());
                    item.unitSize = productPriceLists[j].unitSize!!
                    val unitPriceDetails = PriceDetails()
                    unitPriceDetails.amount = productPriceLists[j].price!!.amount
                    unitPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.unitPrice = (unitPriceDetails)
                    break
                }
            }
            item
        } else {
            null
        }
    }

    /**
     * create Product object to be passed to Review order screen
     *
     * @param productDetailBySupplier
     * @return
     */
    fun createSelectedProductObjectForGRN(productDetailBySupplier: ProductDetailBySupplier?): Product? {
        return if (productDetailBySupplier != null) {
            val item = Product()
            item.sku = productDetailBySupplier.sku
            item.productName = productDetailBySupplier.productName
            item.supplierProductCode = productDetailBySupplier.supplierProductCode
            item.customName = (productDetailBySupplier.productCustomName)
            val productPriceLists = productDetailBySupplier.priceList
            item.unitSizes = productPriceLists
            for (j in productPriceLists!!.indices) {
                if (productPriceLists[j].isStatus(ProductPriceList.UnitSizeStatus.VISIBLE) || productPriceLists[j].isStatus(
                        ProductPriceList.UnitSizeStatus.ACTIVE
                    )
                ) {
                    item.unitPrice = (productPriceLists[j].price)
                    item.unitSize = productPriceLists[j].unitSize!!
                    item.moq = (productPriceLists[j].moq)
                    var totalPrice: Double? = 0.0
                    if (productPriceLists[j].price != null) {
                        totalPrice = productPriceLists[j].price!!.amount
                        if (productPriceLists[j].moq != null) totalPrice =
                            totalPrice!! * productPriceLists[j].moq!!
                    }
                    val totalPriceDetails = PriceDetails()
                    totalPriceDetails.amount = totalPrice
                    totalPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.totalPrice = totalPriceDetails
                    item.quantity = (productPriceLists[j].moq!!)
                    item.unitSize = productPriceLists[j].unitSize!!
                    val unitPriceDetails = PriceDetails()
                    unitPriceDetails.amount = productPriceLists[j].price!!.amount
                    unitPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.unitPrice = (unitPriceDetails)
                    break
                }
            }
            item
        } else {
            null
        }
    }

    fun createSelectedProductForDraftObject(selectedProductsJson: String?): List<CreateDraftOrder.Product> {
        return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
            selectedProductsJson,
            object : TypeToken<List<CreateDraftOrder.Product?>?>() {}.type
        )
    }

    /**
     * calculates the total price by multiplying unitPrice and quantity
     * round up the totalprice if in decimal
     * save the rounded cent value to the total Price object
     */
    @JvmStatic
    fun calculateTotalPriceChanged(product: Product?): Product? {
        if (product != null) {
            val totalPrice = product.unitPrice?.amount!! * product.quantity
            //Create new total PriceDetail Object
            val totPrice = PriceDetails()
            totPrice.currencyCode = (product.unitPrice?.currencyCode)
            totPrice.amount = totalPrice
            product.totalPrice = totPrice
        }
        return product
    }

    @JvmStatic
    fun changeQuantityUOMTextFontSize(text: String?, context: Context): SpannableString {
        val unitSize = returnShortNameValueUnitSize(text)
        val ss1 = SpannableString(unitSize)
        ss1.setSpan(AbsoluteSizeSpan(12, true), 0, ss1.length, 0)
        ss1.setSpan(
            ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
            0,
            ss1.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss1
    }
    //stock related utilities
    /**
     * returns a Map key-UOM value-PriceListWithstock  for a product with price and stock details per uom
     *
     * @param product
     * @return
     */
    @JvmStatic
    fun getStocksAvailableUOM(product: ProductDetailBySupplier): Map<String?, PriceListwithStock> {
        val isItemAvailable = false
        val productPriceList = product.priceList
        val productMap: MutableMap<String?, PriceListwithStock> = HashMap()
        if (productPriceList != null && productPriceList.size > 0) {
            for (i in productPriceList.indices) {
                if (productPriceList[i].status == ZeemartAppConstants.VISIBLE) {
                    val uomStockMap = createUnitSizeStockMap(product)
                    if (uomStockMap.containsKey(productPriceList[i].unitSize)) {
                        val stockAvailable = calculateStockAvailable(
                            uomStockMap[productPriceList[i].unitSize]
                        )
                        if (stockAvailable > 0 && stockAvailable > productPriceList[i].moq!!) {
                            val priceListwithStock = PriceListwithStock()
                            priceListwithStock.stocksAmountAvailable = stockAvailable
                            priceListwithStock.moq = productPriceList[i].moq
                            priceListwithStock.price = productPriceList[i].price
                            priceListwithStock.unitSize = productPriceList[i].unitSize
                            priceListwithStock.status = productPriceList[i].status
                            productMap[productPriceList[i].unitSize] = priceListwithStock
                        }
                    }
                }
            }
        }
        return productMap
    }

    /**
     * Calculate the total stock available
     *
     * @param stock
     * @return
     */
    fun calculateStockAvailable(stock: StocksList?): Double {
        var totalBought = 0.0
        var stockLeft = 0.0
        if (stock!!.orderedQuantity != null && stock.orderedQuantity!!.size > 0) for (i in stock.orderedQuantity!!.indices) {
            totalBought = totalBought + stock.orderedQuantity!![i]
        }
        if (stock.stockQuantity != null) {
            stockLeft = stock.stockQuantity!! - totalBought
        }
        return stockLeft
    }

    /**
     * returns the map key - uom , value - stock data for a particular map from the stock info.
     *
     * @param product
     * @return
     */
    fun createUnitSizeStockMap(product: ProductDetailBySupplier): Map<String?, StocksList> {
        val unitSizeStockMap: MutableMap<String?, StocksList> = HashMap()
        if (product.stocks != null && product.stocks!!.size > 0) {
            for (i in product.stocks!!.indices) {
                unitSizeStockMap[product.stocks!![i].unitSize] = product.stocks!![i]
            }
        }
        return unitSizeStockMap
    }

    fun createSelectedProductObject(products: Products?): Product? {
        return if (products != null) {
            val item = Product()
            item.sku = products.sku
            item.productName = products.productName
            item.supplierProductCode = products.supplierProductCode
            item.customName = (products.productCustomName)
            val productPriceLists: List<ProductPriceList>? = products.priceList
            for (j in productPriceLists?.indices!!) {
                if (productPriceLists[j].status == ZeemartAppConstants.VISIBLE) {
                    item.unitPrice = (productPriceLists[j].price)
                    item.unitSize = productPriceLists[j].unitSize!!
                    item.moq = (productPriceLists[j].moq)
                    val totalPrice =
                        productPriceLists[j].price!!.amount!! * productPriceLists[j].moq!!
                    val totalPriceDetails = PriceDetails()
                    totalPriceDetails.amount = roundUpDoubleToLong(totalPrice)
                    totalPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.totalPrice = totalPriceDetails
                    item.quantity = (productPriceLists[j].moq!!)
                    item.unitSize = productPriceLists[j].unitSize!!
                    val unitPriceDetails = PriceDetails()
                    unitPriceDetails.amount = productPriceLists[j].price!!.amount
                    unitPriceDetails.currencyCode = (productPriceLists[j].price!!.currencyCode)
                    item.unitPrice = (unitPriceDetails)
                    break
                }
            }
            item
        } else {
            null
        }
    }

    /**
     * create a unique key of combination "outletId-supplierId-sku"
     *
     * @param outletId
     * @param supplierId
     * @param sku
     * @return
     */
    fun createFavItemKey(outletId: String?, supplierId: String?, sku: String?): String? {
        var favoriteItem: String? = null
        if (outletId != null && supplierId != null && sku != null) {
            favoriteItem =
                outletId + FAVORITE_ID_SEPERATOR + supplierId + FAVORITE_ID_SEPERATOR + sku
        }
        return favoriteItem
    }

    fun getDealProducts(dealProducts: DealsProductsPaginated.DealProducts): ProductDetailBySupplier {
        val productDetailBySupplier = ProductDetailBySupplier()
        productDetailBySupplier.sku = dealProducts.sku
        productDetailBySupplier.productName = dealProducts.productName
        productDetailBySupplier.dealProductId = dealProducts.dealProductId
        productDetailBySupplier.supplierProductCode = dealProducts.supplierProductCode
        productDetailBySupplier.categoryPath = dealProducts.categoryPath
        productDetailBySupplier.categoryTags = dealProducts.categoryTags
        productDetailBySupplier.certifications = dealProducts.certifications
        val imagesModels: MutableList<ImagesModel> = ArrayList()
        if (dealProducts.images != null) for (i in 0 until dealProducts.images!!.size) {
            val imagesModel = ImagesModel()
            imagesModel.imageURL = dealProducts.images!!.get(i)
            imagesModels.add(imagesModel)
        }
        productDetailBySupplier.images = imagesModels
        productDetailBySupplier.supplierId = dealProducts.supplier?.supplierId
        productDetailBySupplier.supplier = dealProducts.supplier
        return productDetailBySupplier
    }

    fun getSearchProduct(dealProducts: ProductDetailsBySearch): ProductDetailBySupplier {
        val productDetailBySupplier = ProductDetailBySupplier()
        productDetailBySupplier.sku = dealProducts.sku
        productDetailBySupplier.productName = dealProducts.productName
        productDetailBySupplier.dealProductId = dealProducts.dealProductId
        productDetailBySupplier.supplierProductCode = dealProducts.supplierProductCode
        productDetailBySupplier.categoryPath = dealProducts.categoryPath
        productDetailBySupplier.categoryTags = dealProducts.categoryTags
        productDetailBySupplier.certifications = dealProducts.certifications
        val imagesModels: MutableList<ImagesModel> = ArrayList()
        if (dealProducts.images != null) for (i in dealProducts.images!!.indices) {
            val imagesModel = ImagesModel()
            imagesModel.imageURL = dealProducts.images!![i].imageURL
            imagesModels.add(imagesModel)
        }
        val productPriceLists: MutableList<ProductPriceList>? = ArrayList()
        dealProducts.priceList?.let { productPriceLists?.add(it) }
        productDetailBySupplier.priceList = productPriceLists
        productDetailBySupplier.isHasMultipleUom = true
        productDetailBySupplier.images = imagesModels
        productDetailBySupplier.supplierId = dealProducts.supplierId
        productDetailBySupplier.supplier = dealProducts.supplier
        return productDetailBySupplier
    }

    fun getEssentialProducts(essentialsProducts: EssentialProductsPaginated.EssentialsProducts): ProductDetailBySupplier {
        val productDetailBySupplier = ProductDetailBySupplier()
        productDetailBySupplier.sku = essentialsProducts.sku
        productDetailBySupplier.productName = essentialsProducts.productName
        productDetailBySupplier.supplierProductCode = essentialsProducts.supplierProductCode
        productDetailBySupplier.essentialsProductId = essentialsProducts.essentialsProdId
        productDetailBySupplier.isFavourite = essentialsProducts.isFavourite
        productDetailBySupplier.categoryTags = essentialsProducts.categoryTags
        productDetailBySupplier.categoryPath = essentialsProducts.categoryPath
        productDetailBySupplier.certifications = essentialsProducts.certifications
        val imagesModels: MutableList<ImagesModel> = ArrayList()
        if (essentialsProducts.images != null) for (i in 0 until essentialsProducts.images!!
            .size) {
            val imagesModel = ImagesModel()
            imagesModel.imageURL = essentialsProducts.images!!.get(i)
            imagesModels.add(imagesModel)
        }
        productDetailBySupplier.images = imagesModels
        productDetailBySupplier.supplierId = essentialsProducts.supplier?.supplierId
        return productDetailBySupplier
    }

    fun getEssentialProducts(essentialsProducts: SearchedEssentialAndDealsPruducts): ProductDetailBySupplier {
        val productDetailBySupplier = ProductDetailBySupplier()
        productDetailBySupplier.sku = essentialsProducts.sku
        productDetailBySupplier.productName = essentialsProducts.productName
        productDetailBySupplier.supplierProductCode = essentialsProducts.supplierProductCode
        productDetailBySupplier.isFavourite = essentialsProducts.isFavourite
        val imagesModels: MutableList<ImagesModel> = ArrayList()
        if (essentialsProducts.images != null) for (i in 0 until essentialsProducts.images!!
            .size) {
            val imagesModel = ImagesModel()
            imagesModel.imageURL = essentialsProducts.images!!.get(i)
            imagesModels.add(imagesModel)
        }
        productDetailBySupplier.images = imagesModels
        productDetailBySupplier.supplierId = essentialsProducts.supplier?.supplierId
        productDetailBySupplier.supplier = essentialsProducts.supplier
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.dealNumber)) productDetailBySupplier.dealNumber =
            essentialsProducts.dealNumber
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.essentialsId)) productDetailBySupplier.essentialsId =
            essentialsProducts.essentialsId
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.essentialsProdId)) productDetailBySupplier.essentialsProductId =
            essentialsProducts.essentialsProdId
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.dealProductId)) productDetailBySupplier.dealProductId =
            essentialsProducts.dealProductId
        return productDetailBySupplier
    }

    fun getEssentialProducts(essentialsProducts: SelfOnBoardingSearchEssentialAndDealsModel.SelfOnBoardingSearchedEssentialAndDealsPruducts): ProductDetailBySupplier {
        val productDetailBySupplier = ProductDetailBySupplier()
        productDetailBySupplier.sku = essentialsProducts.sku
        productDetailBySupplier.productName = essentialsProducts.productName
        productDetailBySupplier.supplierProductCode = essentialsProducts.supplierProductCode
        productDetailBySupplier.isFavourite = essentialsProducts.isFavourite
        val imagesModels: MutableList<ImagesModel> = ArrayList()
        if (essentialsProducts.images != null) for (i in 0 until essentialsProducts.images!!
            .size) {
            val imagesModel = ImagesModel()
            imagesModel.imageURL = essentialsProducts.images!!.get(i)
            imagesModels.add(imagesModel)
        }
        productDetailBySupplier.images = imagesModels
        productDetailBySupplier.supplierId = essentialsProducts.supplier?.supplierId
        productDetailBySupplier.supplier = essentialsProducts.supplier
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.dealNumber)) productDetailBySupplier.dealNumber =
            essentialsProducts.dealNumber
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.essentialsId)) productDetailBySupplier.essentialsId =
            essentialsProducts.essentialsId
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.essentialsProdId)) productDetailBySupplier.essentialsProductId =
            essentialsProducts.essentialsProdId
        if (!StringHelper.isStringNullOrEmpty(essentialsProducts.dealProductId)) productDetailBySupplier.dealProductId =
            essentialsProducts.dealProductId
        return productDetailBySupplier
    }

    /**
     * Model for the PriceList including the stock for a product as per the UOM
     */
    class PriceListwithStock : ProductPriceList() {
        var stocksAmountAvailable = 0.0
    }
}