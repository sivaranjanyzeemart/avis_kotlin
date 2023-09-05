package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.UnitSizeModel
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Created by ParulBhandari on 12/8/2017.
 */
open class Product() {
    @Expose
    @SerializedName("sku")
    var sku: String? = null

    @Expose
    @SerializedName("productName")
    var productName: String? = null

    @Expose
    @SerializedName("supplierProductCode")
    var supplierProductCode: String? = null

    @SerializedName("customProductCode")
    @Expose
    var customProductCode: String? = null

    @Expose
    @SerializedName("quantity")
    var quantity: Double = 0.0
    get() = field
    set(value) {
        val df = DecimalFormat("#########.###")
        df.roundingMode = RoundingMode.DOWN
        val qty = df.format(value).replace(",".toRegex(), ".").toDouble()
        field = qty
    }

    @Expose
    @SerializedName("unitSize")
    var unitSize = ""

    @Expose
    @SerializedName("unitPrice")
    var unitPrice: PriceDetails? = null

    @Expose
    @SerializedName("totalPrice")
    var totalPrice: PriceDetails? = null

    @Expose
    @SerializedName("notes")
    var notes: String? = null

    @Expose
    @SerializedName("customName")
    var customName: String? = null


    @Expose
    @SerializedName("moq")
    var moq: Double? = 0.0

    @Expose
    @SerializedName("isItemAvailable")
    var isItemAvailable = true

    //order date for the product added for
    // the date wise sorting while merging SKU when creating repeat orders
    @Expose
    @SerializedName("orderDate")
    var orderDate: Long? = null

    @Expose
    @SerializedName("isProductManuallyAdded")
    var isProductManuallyAdded = false

    @Expose
    var isSelected = false

    @Expose
    var isCustom = false

    @Expose
    var unitSizes: List<ProductPriceList>? = null

//    fun getMoq(): Double {
//        return moq ?: 0.0
//    }
//
//    fun setMoq(moq: Double?) {
//        this.moq = moq
//    }

    val moqDisplayValue: String
        get() = UnitSizeModel.Companion.getValueDecimalAllowedCheck(unitSize, moq!!)

    //        return getMoq()+"";
    val moqEditValue: String
        get() =//        return getMoq()+"";
            UnitSizeModel.Companion.getValueDecimalAllowedCheckWithoutThousandSeparator(
                unitSize,
                moq!!
            )
    

//    fun setQuantity(quantity: Double?) {
//        val df = DecimalFormat("#########.###")
//        df.roundingMode = RoundingMode.DOWN
//        val qty = df.format(quantity).replace(",".toRegex(), ".").toDouble()
//        this.quantity = (qty)
//    }

    val quantityDisplayValue: String
        get() = UnitSizeModel.Companion.getValueDecimalAllowedCheck(unitSize, quantity)

    //        return getQuantity()+"";
    val quantityEditValue: String
        get() =//        return getQuantity()+"";
            UnitSizeModel.Companion.getValueDecimalAllowedCheckWithoutThousandSeparator(
                unitSize,
                quantity
            )

//    fun getUnitPrice(): PriceDetails {
//        if (unitPrice == null) {
//            unitPrice = PriceDetails()
//        }
//        return unitPrice!!
//    }

//    fun setUnitPrice(unitPrice: PriceDetails?) {
//        this.unitPrice = unitPrice
//    }

    val quantityWithUnitSizeValue: String
        get() = if (!StringHelper.isStringNullOrEmpty(quantityDisplayValue) && !StringHelper.isStringNullOrEmpty(
                unitSize
            )
        ) {
            quantityDisplayValue + " " + UnitSizeModel.Companion.returnShortNameValueUnitSize(
                unitSize
            )
        } else {
            quantityDisplayValue
        }

    override fun toString(): String {
        return "Product{" +
                "sku='" + sku + '\'' +
                ", productName='" + productName + '\'' +
                ", supplierProductCode='" + supplierProductCode + '\'' +
                ", customProductCode='" + customProductCode + '\'' +
                ", quantity=" + quantity +
                ", unitSize='" + unitSize + '\'' +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", notes='" + notes + '\'' +
                ", customName='" + customName + '\'' +
                ", moq=" + moq +
                ", isItemAvailable=" + isItemAvailable +
                ", orderDate=" + orderDate +
                ", isProductManuallyAdded=" + isProductManuallyAdded +
                ", isSelected=" + isSelected +
                ", isCustom=" + isCustom +
                ", unitSizes=" + unitSizes +
                '}'
    }

    init {
        unitPrice = PriceDetails()
    }
}