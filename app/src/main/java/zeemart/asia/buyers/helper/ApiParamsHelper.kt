package zeemart.asia.buyers.helper

import com.google.firebase.crashlytics.FirebaseCrashlytics
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.models.StockageType
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

class ApiParamsHelper {
    private var supplierId = ""
    private var orderStatus = ""
    private var sortField = ""
    private var sortOrder = ""
    private var orderStartDate = ""
    private var orderEndDate = ""
    private var orderPlacedStartDate = ""
    private var orderPlacedEndDate = ""
    private var pageSize = ""
    private var pageNumber = ""
    private var outletId = ""
    private var orderIdText = ""
    private var isReceived = ""
    private var isInvoiced = ""
    private var sortBy = ""
    private var startDate = ""
    private var endDate = ""
    private var includeFields = ""
    private var invoiceNumSearchText = ""
    private var id = ""
    private var groupBy = ""
    private var category = ""
    private var sku = ""
    private var invoiceStartDate = ""
    private var invoiceEndDate = ""
    private var isDetailed = ""
    private var refStartDate = ""
    private var refEndDate = ""
    private var orderEnabled = ""
    private var deliveryStartDate = ""
    private var deliveryEndDate = ""
    private var stockageTypes = ""
    private var shelveIds = ""
    private var shelveId = ""
    private var stockageId = ""
    private var orderId = ""
    private var announcementsStatus = ""
    private var announcementsId = ""
    private var promoCode = ""
    private var status = ""
    var invoiceStatuses = ""
    private var isFavourite = ""
    private var isBelowParLevel = ""
    private var isRejectedOrNonUploaded = ""
    private var searchText = ""
    private var companyId = ""

    // set to true for api which takes comma separated arrays
    private var removeSquareBrackets = false
    private var dueStartDate = ""
    private var dueEndDate = ""
    private var manualPayment = ""
    private var onlyInvoiced = ""
    private var includeOrderChanges = ""
    private var dealNumber = ""
    private var dealNumbers = ""
    private var essentialId = ""
    private var statuses = ""
    private var termAgreementId = ""
    private var policyAgreementId = ""
    private var clientType = ""
    private var ipAddress = ""
    private var platform = ""
    private var userId = ""
    private var essentialProductId = ""
    private var orderType = ""
    private var marketId = ""
    private var certificateId = ""
    private var certifications = ""
    private var categories = ""
    private var tags = ""
    private var tag = ""
    private var timeDelivered = ""
    private var isConversionUpdated = ""
    private var grnId = ""
    private var linkedToOrders = ""
    private var productName = ""
    private var unitSize = ""
    private var uploadRequired = ""
    private var productId = ""
    fun setRemoveSquareBrackets(removeSquareBrackets: Boolean) {
        this.removeSquareBrackets = removeSquareBrackets
    }

    enum class SortOrder(val value: String) {
        DESC("desc"), ASC("asc"), DESC_CAP("DESC"), ASC_CAP("ASC");

        override fun toString(): String {
            return value
        }
    }

    enum class ConversionReview(val value: String) {
        NOT_REQUIRED("NOT_REQUIRED"), INVENTORY_UOM_UPDATE("INVENTORY_UOM_UPDATE"), NON_INVENTORY_UOM_UPDATE(
            "NON_INVENTORY_UOM_UPDATE"
        );

        override fun toString(): String {
            return value
        }
    }

    enum class OrderDisablingReason(val value: String) {
        ORDER_SETTINGS_UN_VERIFIED("OrderSettingsUnverified"), PAYMENT_OUT_STANDING("PaymentOutstanding");

        override fun toString(): String {
            return value
        }
    }

    enum class SortField(val value: String) {
        TIME_UPDATED("timeUpdated"), TIME_CREATED("timeCreated"), TIME_DELIVERED("timeDelivered"), PRODUCT_CODE(
            "productCode"
        ),
        PRODUCT_NAME("productName"), INVOICE_DATE("invoiceDate"), TIME_PUBLISHED("timeToBePublished"), DISPLAY_SEQUENCE(
            "displaySequence"
        ),
        DELIVERY_STATUS("deliveryStatus");

        override fun toString(): String {
            return value
        }
    }

    enum class GroupBy(val value: String) {
        SUPPLIER("supplier"), OUTLET("outlet"), DAILY("daily"), WEEKLY("weekly"), YEARLY("yearly");

    }

    enum class IncludeFields(val value: String) {
        TOTAL_CHARGE("totalCharge"), PAYMENT_TERMS("paymentTerms"), IMAGES("images"), CREATED_BY("createdBy"), SUPPLIER(
            "supplier"
        ),
        PRODUCT_NAME("productName"), PAR_LEVEL("parLevel"), DELIVERY_INSTRUCTION("deliveryInstruction"), REJECTED_REASON(
            "rejectReason"
        ),
        INVOICE_TYPE("invoiceType"), PAYMENT_STATUS("paymentStatus"), ORDER_DATA("orderData"), AMOUNT(
            "amount"
        ),
        OUTLET("outlet"), ORDER_ID("orderId"), ORDER_IDS("orderIds"), ORDER_STATUS("orderStatus"), PRODUCTS(
            "products"
        ),
        TIME_CREATED("timeCreated"), TIME_DELIVERED("timeDelivered"), IS_RECEIVED("isReceived"), IS_INVOICED(
            "isInvoiced"
        ),
        ORDER_TYPE("orderType"), TIME_CUT_OFF("timeCutOff"), TIME_UPDATED("timeUpdated"), APPROVERS(
            "approvers"
        ),
        TIME_PLACED("timePlaced"), TRANSACTION_IMAGE("transactionImage"), PAYMENT_TYPE("paymentType"), ISGUEST_INVOICE(
            "isGuestInvoice"
        );

        override fun toString(): String {
            return value
        }
    }

    enum class Status(val value: String) {
        ACTIVE("ACTIVE"), REDEEMED("REDEEMED"), EXPIRED("EXPIRED"), SCHEDULED("SCHEDULED"), ENDED("ENDED");

        override fun toString(): String {
            return value
        }
    }

    enum class AppSettingsTime(val value: String) {
        IMMEDIATELY("IMMEDIATELY"), AFTER_5_MINUTES("AFTER FIVE MINUTES"), AFTER_30_MINUTES("AFTER THIRTY MINUTES");

        override fun toString(): String {
            return value
        }
    }

    enum class OrderType(val value: String) {
        SINGLE("Single"), DEAL("Deal"), ESSENTIAL("Essentials");

        override fun toString(): String {
            return value
        }
    }

    fun setSortField(sortField: SortField): ApiParamsHelper {
        this.sortField = PARAMS_SORT_FIELD + sortField.value
        return this
    }

    fun setSortOrder(sortOrder: SortOrder): ApiParamsHelper {
        this.sortOrder = PARAMS_SORT_ORDER + sortOrder.value
        return this
    }

    fun setOutletId(outletId: String): ApiParamsHelper {
        this.outletId = PARAMS_OUTLET_ID + outletId
        return this
    }

    fun setSupplierId(supplierId: String): ApiParamsHelper {
        this.supplierId = PARAMS_SUPPLIER_ID + supplierId
        return this
    }

    fun setProductName(productName: String): ApiParamsHelper {
        this.productName = PARAMS_PRODUCT_NAME + productName
        return this
    }

    fun setUnitSize(unitSize: String): ApiParamsHelper {
        this.unitSize = PARAMS_UNIT_SIZE + unitSize
        return this
    }

    fun setTimeDelivered(timeDelivered: Long): ApiParamsHelper {
        this.timeDelivered = PARAMS_TIME_DELIVERED + timeDelivered
        return this
    }

    fun setSupplierId(supplierId: Array<String?>?): ApiParamsHelper {
        this.supplierId = PARAMS_SUPPLIER_ID + Arrays.toString(supplierId)
        return this
    }

    fun setOrderStatus(orderStatus: Array<OrderStatus?>?): ApiParamsHelper {
        this.orderStatus = PARAMS_ORDER_STATUS + Arrays.toString(orderStatus)
        return this
    }

    // deprecated
    fun setOrderStatus(orderStatus: Array<String>): ApiParamsHelper {
        this.orderStatus = PARAMS_ORDER_STATUS + arraystoString(orderStatus)
        return this
    }

    fun setOrdertype(orderType: Array<String>): ApiParamsHelper {
        this.orderType = PARAMS_ORDER_TYPE + arraystoString(orderType)
        return this
    }

    fun setOrdertype(orderType: String): ApiParamsHelper {
        this.orderType = PARAMS_ORDER_TYPE + orderType
        return this
    }

    fun setDefaultSixtyDaysPeriod(timeZone: TimeZone?): ApiParamsHelper {
        val period = DateHelper.pastDaysStartEndDate(60, true, timeZone)
        orderStartDate = PARAMS_ORDER_START_DATE + period.startDateMillis
        orderEndDate = PARAMS_ORDER_END_DATE + period.endDateMillis
        return this
    }

    fun setOrderStartDate(orderStartDate: Long): ApiParamsHelper {
        this.orderStartDate = PARAMS_ORDER_START_DATE + orderStartDate
        return this
    }

    fun setOrderPlacedStartDate(orderStartDate: Long): ApiParamsHelper {
        orderPlacedStartDate = PARAMS_ORDER_PLACED_START_DATE + orderStartDate
        return this
    }

    fun setOrderEndDate(orderEndDate: Long): ApiParamsHelper {
        this.orderEndDate = PARAMS_ORDER_END_DATE + orderEndDate
        return this
    }

    fun setOrderPlacedEndDate(orderEndDate: Long): ApiParamsHelper {
        orderPlacedEndDate = PARAMS_ORDER_PLACED_END_DATE + orderEndDate
        return this
    }

    fun setStartDate(startDate: Long): ApiParamsHelper {
        this.startDate = PARAMS_START_DATE + startDate
        return this
    }

    fun setEndDate(endDate: Long): ApiParamsHelper {
        this.endDate = PARAMS_END_DATE + endDate
        return this
    }

    fun setIncludeOrderChanges(includeOrderChanges: Boolean): ApiParamsHelper {
        this.includeOrderChanges = PARAMS_INCLUDE_ORDER_CHANGE + includeOrderChanges
        return this
    }

    fun setLinkedToOrder(linkedToOrder: Boolean): ApiParamsHelper {
        linkedToOrders = PARAMS_LINKED_TO_ORDER + linkedToOrder
        return this
    }

    fun setPageSize(pageSize: Int): ApiParamsHelper {
        this.pageSize = PARAMS_ORDERS_PAGE_SIZE + pageSize
        return this
    }

    fun setPageNumber(pageNumber: Int): ApiParamsHelper {
        this.pageNumber = PARAMS_ORDERS_PAGE_NUM + pageNumber
        return this
    }

    fun setOutletIds(outletIds: Array<String>): ApiParamsHelper {
        outletId = PARAMS_ORDERS_GET_BY_OUTLET_ID + arraystoString(outletIds)
        return this
    }

    fun setOrderIdText(orderIdText: String): ApiParamsHelper {
        this.orderIdText = PARAMS_ORDERS_SEARCH_TEXT + orderIdText
        return this
    }

    fun setIsReceived(isReceived: Boolean): ApiParamsHelper {
        this.isReceived = PARAMS_ORDER_IS_RECEIVED + isReceived
        return this
    }

    fun setIsInvoiced(isInvoiced: Boolean): ApiParamsHelper {
        this.isInvoiced = PARAMS_ORDER_IS_INVOICED + isInvoiced
        return this
    }

    fun setIsConversionUpdated(isConversionUpdated: Boolean): ApiParamsHelper {
        this.isConversionUpdated = PARAMS_IS_CONVERSION_UPDATED + isConversionUpdated
        return this
    }

    fun setIsRejectedOrNonUploaded(isFavourite: Boolean): ApiParamsHelper {
        isRejectedOrNonUploaded = PARAMS_IS_REJECTED_NON_UPLOADED + isFavourite
        return this
    }

    fun setIsFavourite(isFavourite: Boolean): ApiParamsHelper {
        this.isFavourite = PARAMS_ORDER_IS_FAVOURITE + isFavourite
        return this
    }

    fun setIsBelowPar(isBelowPar: Boolean): ApiParamsHelper {
        isBelowParLevel = PARAMS_ORDER_IS_BELOW_PAR + isBelowPar
        return this
    }

    fun setSortBy(sortBy: SortField): ApiParamsHelper {
        this.sortBy = PARAMS_SORT_BY + sortBy.value
        return this
    }

    fun setUserId(userId: String): ApiParamsHelper {
        this.userId = PARAMS_USER_ID + userId
        return this
    }

    fun setSearchText(searchText: String): ApiParamsHelper {
        this.searchText = PARAMS_SEARCH_TEXT + searchText
        return this
    }

    fun setInvoiceNumSearchText(invoiceSearchText: String): ApiParamsHelper {
        invoiceNumSearchText = PARAMS_INVOICE_SEARCH_TEXT + invoiceSearchText
        return this
    }

    fun setIncludeFields(includeFields: Array<IncludeFields?>?): ApiParamsHelper {
        this.includeFields = PARAMS_INCLUDE_FIELDS + Arrays.toString(includeFields)
        return this
    }

    fun setOrderIncludeFields(includeFields: Array<IncludeFields>): ApiParamsHelper {
        this.includeFields = PARAMS_INCLUDE_FIELDS + arraysToString(includeFields)
        return this
    }

    fun setEssentialProductId(essentialProductId: String): ApiParamsHelper {
        this.essentialProductId = PARAMS_ESSENTIAL_PRODUCT_ID + essentialProductId
        return this
    }

    fun setSku(sku: String): ApiParamsHelper {
        this.sku = PARAMS_SKU + sku
        return this
    }

    fun setId(id: String): ApiParamsHelper {
        this.id = PARAMS_ID + id
        return this
    }

    fun setTermAgreementId(id: String): ApiParamsHelper {
        termAgreementId = PARAMS_TERM_AGREEMENT_ID + id
        return this
    }

    fun setPolicyAgreementId(id: String): ApiParamsHelper {
        policyAgreementId = PARAMS_POLICY_AGREEMENT_ID + id
        return this
    }

    fun setClientType(id: String): ApiParamsHelper {
        clientType = PARAMS_CLIENT_TYPE + id
        return this
    }

    fun setIpAddress(id: String): ApiParamsHelper {
        ipAddress = PARAMS_IP_ADDRESS + id
        return this
    }

    fun setPlatForm(id: String): ApiParamsHelper {
        platform = PARAMS_PLAT_FORM + id
        return this
    }

    fun setGroupBy(groupBy: GroupBy): ApiParamsHelper {
        this.groupBy = PARAMS_GROUP_BY + groupBy.value
        return this
    }

    fun setMarketId(marketId: String): ApiParamsHelper {
        this.marketId = PARAMS_MARKET_ID + marketId
        return this
    }

    fun setCategory(category: String): ApiParamsHelper {
        includeFields = PARAMS_CATEGORY + category
        return this
    }

    fun setCategory(category: List<String>): ApiParamsHelper {
        this.category = PARAMS_CATEGORY + arraysToString(category)
        return this
    }

    fun setCertifications(category: List<String>): ApiParamsHelper {
        certifications = PARAMS_CERTIFICATIONS + arraysToString(category)
        return this
    }

    fun setInvoiceStartDate(invoiceStartDate: Long): ApiParamsHelper {
        this.invoiceStartDate = PARAMS_INVOICE_START_DATE + invoiceStartDate
        return this
    }

    fun setInvoiceEndDate(invoiceEndDate: Long): ApiParamsHelper {
        this.invoiceEndDate = PARAMS_INVOICE_END_DATE + invoiceEndDate
        return this
    }

    fun setIsDetailed(isDetailed: Boolean): ApiParamsHelper {
        this.isDetailed = PARAMS_IS_DETAILED + isDetailed
        return this
    }

    fun setRefStartDate(refStartDate: Long): ApiParamsHelper {
        this.refStartDate = PARAMS_REF_START_DATE + refStartDate
        return this
    }

    fun setRefEndDate(refEndDate: Long): ApiParamsHelper {
        this.refEndDate = PARAMS_REF_END_DATE + refEndDate
        return this
    }

    fun setOrderEnabled(isOrderEnabled: Boolean): ApiParamsHelper {
        orderEnabled = PARAMS_ORDER_ENABLED + isOrderEnabled
        return this
    }

    fun setDeliveryStartDate(deliveryStartDate: Long): ApiParamsHelper {
        this.deliveryStartDate = PARAMS_DELIVERY_START_DATE + deliveryStartDate
        return this
    }

    fun setDeliveryEndDate(deliveryEndDate: Long): ApiParamsHelper {
        this.deliveryEndDate = PARAMS_DELIVERY_END_DATE + deliveryEndDate
        return this
    }

    fun setStockageTypes(stockageTypes: Array<StockageType?>?): ApiParamsHelper {
        this.stockageTypes = PARAMS_STOCKAGE_TYPE + Arrays.toString(stockageTypes)
        return this
    }

    fun setShelveIds(shelveIds: Array<String?>?): ApiParamsHelper {
        this.shelveIds = PARAMS_SHELVE_IDS + Arrays.toString(shelveIds)
        return this
    }

    fun setShelveId(shelveId: String): ApiParamsHelper {
        this.shelveId = PARAMS_SHELVE_ID + shelveId
        return this
    }

    fun setStockageId(stockageId: String): ApiParamsHelper {
        this.stockageId = PARAMS_STOCKAGE_ID + stockageId
        return this
    }

    fun setOrderId(orderIds: Array<String?>?): ApiParamsHelper {
        orderId = PARAMS_ORDER_ID + Arrays.toString(orderIds)
        return this
    }

    fun setOrderIds(orderIds: Array<String>): ApiParamsHelper {
        orderId = PARAMS_ORDER_ID + arraystoString(orderIds)
        return this
    }

    fun setMultipleOrderIds(orderIds: Array<String>): ApiParamsHelper {
        orderId = PARAMS_ORDER_IDS + arraystoString(orderIds)
        return this
    }

    fun setOrderIds(orderIds: List<String>): ApiParamsHelper {
        orderId = PARAMS_ORDER_ID + arraysToString(orderIds)
        return this
    }

    fun setOrderId(orderId: String): ApiParamsHelper {
        this.orderId = PARAMS_ORDER_ID + orderId
        return this
    }

    fun setGrnId(grnId: String): ApiParamsHelper {
        this.grnId = PARAMS_GRN_ID + grnId
        return this
    }

    fun setAnnouncementsStatus(announcementsStatus: String): ApiParamsHelper {
        this.announcementsStatus = PARAMS_STATUS + announcementsStatus
        return this
    }

    fun setAnnouncementsId(announcementsIds: String): ApiParamsHelper {
        announcementsId = PARAMS_ANNOUNCEMENT_ID + announcementsIds
        return this
    }

    fun setPromoCode(promoCode: String): ApiParamsHelper {
        this.promoCode = PARAMS_PROMO_CODE + promoCode
        return this
    }

    fun setStatus(status: Status): ApiParamsHelper {
        this.status = PARAMS_STATUS + status.value
        return this
    }

    fun setCompanyId(companyId: String): ApiParamsHelper {
        this.companyId = PARAMS_COMPANY_ID + companyId
        return this
    }

    fun setEssentialsId(essentialId: String): ApiParamsHelper {
        this.essentialId = PARAMS_ESSENTIALS_ID + essentialId
        return this
    }

//    fun getInvoiceStatuses(): String {
//        return invoiceStatuses
//    }
//
//    fun setInvoiceStatuses(invoiceStatuses: String) {
//        this.invoiceStatuses = PARAMS_INVOICE_STATUSES + invoiceStatuses
//    }

    fun setInvoiceStatuses(invoiceStatuses: Array<String>) {
        this.invoiceStatuses = PARAMS_INVOICE_STATUSES + arraystoString(invoiceStatuses)
    }

    fun setDueStartDate(dueStartDate: Long): ApiParamsHelper {
        this.dueStartDate = PARAMS_DUE_START_DATE + dueStartDate
        return this
    }

    fun setDueEndDate(dueEndDate: Long): ApiParamsHelper {
        this.dueEndDate = PARAMS_DUE_END_DATE + dueEndDate
        return this
    }

    fun setManualPayment(isManualPayment: Boolean): ApiParamsHelper {
        manualPayment = PARAMS_MANUAL_PAYMENT + isManualPayment
        return this
    }

    fun setUploadRequired(uploadRequired: Boolean): ApiParamsHelper {
        this.uploadRequired = PARAMS_UPLOADREQUIRED + uploadRequired
        return this
    }

    fun setOnlyInvoices(isInvoiced: Boolean): ApiParamsHelper {
        onlyInvoiced = PARAMS_ONLY_INVOICED + isInvoiced
        return this
    }

    fun setDealNumber(dealNumber: String): ApiParamsHelper {
        this.dealNumber = PARAMS_DEAL_NUMBER + dealNumber
        return this
    }

    fun setDealNumbers(dealNumber: String): ApiParamsHelper {
        dealNumbers = PARAMS_DEAL_NUMBERS + dealNumber
        return this
    }

    fun setStatuses(status: String): ApiParamsHelper {
        statuses = PARAMS_STATUSES + status
        return this
    }

    fun setCertificateId(certificateId: List<String>): ApiParamsHelper {
        this.certificateId = PARAMS_CERTIFICATION_ID + arraysToString(certificateId)
        return this
    }

    fun setCategories(categories: List<String>): ApiParamsHelper {
        this.categories = PARAMS_CATEGORIES + arraysToString(categories)
        return this
    }

    fun setTags(tags: List<String>): ApiParamsHelper {
        this.tags = PARAMS_TAGS + arraysToString(tags)
        return this
    }

    fun setTag(tags: String): ApiParamsHelper {
        tag = PARAMS_TAG + tags
        return this
    }

    fun setProductId(productId: String): ApiParamsHelper {
        this.productId = PARAMS_PRODUCT_ID + productId
        return this
    }

    /**
     * return outlets as comma seperated list
     *
     * @return
     */
    val outletIdList: String
        get() {
            if (!StringHelper.isStringNullOrEmpty(outletId)) {
                var returnOutlet = outletId
                returnOutlet =
                    returnOutlet.replace(PARAMS_ORDERS_GET_BY_OUTLET_ID, "").replace("[", "")
                        .replace("]", "")
                return returnOutlet
            }
            return outletId
        }

    fun arraysToString(data: Array<IncludeFields>): String {
        var str = ""
        for (i in data.indices) {
            str = if (i != data.size - 1) {
                str + data[i].toString() + ","
            } else {
                str + data[i].toString()
            }
        }
        return str
    }

    fun getUrl(u: String?): String {
        var url = "?"
        if (u != null && u.contains("?")) {
            url = u
        } else if (u != null && u.length > 0) {
            url = "$u?"
        }
        if (!StringHelper.isStringNullOrEmpty(outletId)) url += outletId
        if (!StringHelper.isStringNullOrEmpty(supplierId)) url += supplierId
        if (!StringHelper.isStringNullOrEmpty(sortField)) url += sortField
        if (!StringHelper.isStringNullOrEmpty(sortOrder)) url += sortOrder
        if (!StringHelper.isStringNullOrEmpty(orderStatus)) url += orderStatus
        if (!StringHelper.isStringNullOrEmpty(orderType)) url += orderType
        if (!StringHelper.isStringNullOrEmpty(orderStartDate)) url += orderStartDate
        if (!StringHelper.isStringNullOrEmpty(orderEndDate)) url += orderEndDate
        if (!StringHelper.isStringNullOrEmpty(pageNumber)) url += pageNumber
        if (!StringHelper.isStringNullOrEmpty(pageSize)) url += pageSize
        if (!StringHelper.isStringNullOrEmpty(orderIdText)) url += orderIdText
        if (!StringHelper.isStringNullOrEmpty(isReceived)) url += isReceived
        if (!StringHelper.isStringNullOrEmpty(uploadRequired)) url += uploadRequired
        if (!StringHelper.isStringNullOrEmpty(isInvoiced)) url += isInvoiced
        if (!StringHelper.isStringNullOrEmpty(isFavourite)) url += isFavourite
        if (!StringHelper.isStringNullOrEmpty(isBelowParLevel)) url += isBelowParLevel
        if (!StringHelper.isStringNullOrEmpty(sortBy)) url += sortBy
        if (!StringHelper.isStringNullOrEmpty(invoiceNumSearchText)) url += invoiceNumSearchText
        if (!StringHelper.isStringNullOrEmpty(includeFields)) url += includeFields
        if (!StringHelper.isStringNullOrEmpty(groupBy)) url += groupBy
        if (!StringHelper.isStringNullOrEmpty(startDate)) url += startDate
        if (!StringHelper.isStringNullOrEmpty(endDate)) url += endDate
        if (!StringHelper.isStringNullOrEmpty(category)) url += category
        if (!StringHelper.isStringNullOrEmpty(id)) url += id
        if (!StringHelper.isStringNullOrEmpty(sku)) url += sku
        if (!StringHelper.isStringNullOrEmpty(invoiceStartDate)) url += invoiceStartDate
        if (!StringHelper.isStringNullOrEmpty(invoiceEndDate)) url += invoiceEndDate
        if (!StringHelper.isStringNullOrEmpty(isDetailed)) url += isDetailed
        if (!StringHelper.isStringNullOrEmpty(refStartDate)) url += refStartDate
        if (!StringHelper.isStringNullOrEmpty(refEndDate)) url += refEndDate
        if (!StringHelper.isStringNullOrEmpty(orderEnabled)) url += orderEnabled
        if (!StringHelper.isStringNullOrEmpty(deliveryStartDate)) url += deliveryStartDate
        if (!StringHelper.isStringNullOrEmpty(deliveryEndDate)) url += deliveryEndDate
        if (!StringHelper.isStringNullOrEmpty(stockageTypes)) url += stockageTypes
        if (!StringHelper.isStringNullOrEmpty(shelveIds)) url += shelveIds
        if (!StringHelper.isStringNullOrEmpty(shelveId)) url += shelveId
        if (!StringHelper.isStringNullOrEmpty(stockageId)) url += stockageId
        if (!StringHelper.isStringNullOrEmpty(orderId)) url += orderId
        if (!StringHelper.isStringNullOrEmpty(announcementsId)) url += announcementsId
        if (!StringHelper.isStringNullOrEmpty(announcementsStatus)) url += announcementsStatus
        if (!StringHelper.isStringNullOrEmpty(promoCode)) url += promoCode
        if (!StringHelper.isStringNullOrEmpty(status)) url += status
        if (!StringHelper.isStringNullOrEmpty(searchText)) url += searchText
        if (!StringHelper.isStringNullOrEmpty(invoiceStatuses)) url += invoiceStatuses
        if (!StringHelper.isStringNullOrEmpty(companyId)) url += companyId
        if (!StringHelper.isStringNullOrEmpty(dueStartDate)) url += dueStartDate
        if (!StringHelper.isStringNullOrEmpty(dueEndDate)) url += dueEndDate
        if (!StringHelper.isStringNullOrEmpty(includeOrderChanges)) url += includeOrderChanges
        if (!StringHelper.isStringNullOrEmpty(dealNumber)) url += dealNumber
        if (!StringHelper.isStringNullOrEmpty(essentialId)) url += essentialId
        if (!StringHelper.isStringNullOrEmpty(statuses)) url += statuses
        if (!StringHelper.isStringNullOrEmpty(dealNumbers)) url += dealNumbers
        if (!StringHelper.isStringNullOrEmpty(manualPayment)) url += manualPayment
        if (!StringHelper.isStringNullOrEmpty(policyAgreementId)) url += policyAgreementId
        if (!StringHelper.isStringNullOrEmpty(termAgreementId)) url += termAgreementId
        if (!StringHelper.isStringNullOrEmpty(clientType)) url += clientType
        if (!StringHelper.isStringNullOrEmpty(ipAddress)) url += ipAddress
        if (!StringHelper.isStringNullOrEmpty(platform)) url += platform
        if (!StringHelper.isStringNullOrEmpty(userId)) url += userId
        if (!StringHelper.isStringNullOrEmpty(marketId)) url += marketId
        if (!StringHelper.isStringNullOrEmpty(essentialProductId)) url += essentialProductId
        if (!StringHelper.isStringNullOrEmpty(categories)) url += categories
        if (!StringHelper.isStringNullOrEmpty(certificateId)) url += certificateId
        if (!StringHelper.isStringNullOrEmpty(timeDelivered)) url += timeDelivered
        if (!StringHelper.isStringNullOrEmpty(certifications)) url += certifications
        if (!StringHelper.isStringNullOrEmpty(tags)) url += tags
        if (!StringHelper.isStringNullOrEmpty(tag)) url += tag
        if (!StringHelper.isStringNullOrEmpty(isConversionUpdated)) url += isConversionUpdated
        if (!StringHelper.isStringNullOrEmpty(grnId)) url += grnId
        if (!StringHelper.isStringNullOrEmpty(isRejectedOrNonUploaded)) url += isRejectedOrNonUploaded
        if (!StringHelper.isStringNullOrEmpty(orderPlacedStartDate)) url += orderPlacedStartDate
        if (!StringHelper.isStringNullOrEmpty(orderPlacedEndDate)) url += orderPlacedEndDate
        if (!StringHelper.isStringNullOrEmpty(linkedToOrders)) url += linkedToOrders
        if (!StringHelper.isStringNullOrEmpty(onlyInvoiced)) url += onlyInvoiced
        if (!StringHelper.isStringNullOrEmpty(unitSize)) url += unitSize
        if (!StringHelper.isStringNullOrEmpty(productName)) url += productName
        if (!StringHelper.isStringNullOrEmpty(productId)) url += productId
        if (removeSquareBrackets) url =
            url.replace("\\[".toRegex(), "").replace("\\]".toRegex(), "")
        url = encodeUrl(url)
        return url
    }

    companion object {
        const val PARAMS_SORT_FIELD = "&sortField="
        const val PARAMS_SORT_ORDER = "&sortOrder="
        const val PARAMS_SUPPLIER_ID = "&supplierId="
        const val PARAMS_OUTLET_ID = "outletId="
        const val PARAMS_ORDER_STATUS = "&orderStatus="
        const val PARAMS_ORDER_TYPE = "&orderType="
        const val PARAMS_ORDER_START_DATE = "&orderStartDate="
        const val PARAMS_ORDER_END_DATE = "&orderEndDate="
        const val PARAMS_ORDERS_PAGE_SIZE = "&pageSize="
        const val PARAMS_ORDERS_PAGE_NUM = "&pageNumber="
        const val PARAMS_ORDERS_GET_BY_OUTLET_ID = "&outletId="
        const val PARAMS_ONLY_INVOICED = "&onlyInvoicedOrders="
        const val PARAMS_ORDERS_SEARCH_TEXT = "&orderIdText="
        const val PARAMS_ORDER_IS_RECEIVED = "&isReceived="
        const val PARAMS_ORDER_IS_INVOICED = "&isInvoiced="
        const val PARAMS_ORDER_IS_FAVOURITE = "&favourite="
        const val PARAMS_ORDER_IS_BELOW_PAR = "&belowParLevel="
        const val PARAMS_SORT_BY = "&sortBy="
        const val PARAMS_INVOICE_SEARCH_TEXT = "&invoiceNumSearchText="
        const val PARAMS_INCLUDE_FIELDS = "&includeFields="
        const val PARAMS_GROUP_BY = "&groupBy="
        const val PARAMS_START_DATE = "&startDate="
        const val PARAMS_END_DATE = "&endDate="
        const val PARAMS_CATEGORY = "&category="
        const val PARAMS_ID = "&id="
        const val PARAMS_USER_ID = "&userId="
        const val PARAMS_SKU = "&sku="
        const val PARAMS_INVOICE_START_DATE = "&invoiceStartDate="
        const val PARAMS_INVOICE_END_DATE = "&invoiceEndDate="
        const val PARAMS_IS_DETAILED = "&isDetailed="
        const val PARAMS_REF_END_DATE = "&refEndDate="
        const val PARAMS_REF_START_DATE = "&refStartDate="
        const val PARAMS_ORDER_ENABLED = "&orderEnabled="
        const val PARAMS_DELIVERY_START_DATE = "&deliveryStartDate="
        const val PARAMS_DELIVERY_END_DATE = "&deliveryEndDate="
        const val PARAMS_STOCKAGE_TYPE = "&stockageTypes="
        const val PARAMS_SHELVE_IDS = "&shelveIds="
        const val PARAMS_SHELVE_ID = "&shelveId="
        const val PARAMS_STOCKAGE_ID = "&stockageId="
        const val PARAMS_ORDER_ID = "&orderId="
        const val PARAMS_ORDER_IDD = "&orderId="
        const val PARAMS_ORDER_IDS = "&orderIds="
        const val PARAMS_GRN_ID = "&grnId="
        const val PARAMS_ANNOUNCEMENT_ID = "&announcementId="
        const val PARAMS_STATUS = "&status="
        const val PARAMS_PROMO_CODE = "&promoCode="
        const val PARAMS_INVOICE_STATUSES = "&invoiceStatuses="
        const val PARAMS_SEARCH_TEXT = "&searchText="
        const val PARAMS_COMPANY_ID = "&companyId="
        const val PARAMS_DUE_START_DATE = "&dueStartDate="
        const val PARAMS_DUE_END_DATE = "&dueEndDate="
        const val PARAMS_MANUAL_PAYMENT = "&manualPayment="
        const val PARAMS_INCLUDE_ORDER_CHANGE = "&includeOrderChanges="
        const val PARAMS_DEAL_NUMBER = "&dealNumber="
        const val PARAMS_DEAL_NUMBERS = "&dealNumbers="
        const val PARAMS_ESSENTIALS_ID = "&essentialsId="
        const val PARAMS_STATUSES = "&statuses="
        const val PARAMS_TERM_AGREEMENT_ID = "&termAgreementId="
        const val PARAMS_POLICY_AGREEMENT_ID = "&policyAgreementId="
        const val PARAMS_CLIENT_TYPE = "&clientType="
        const val PARAMS_IP_ADDRESS = "&ipAddress="
        const val PARAMS_PLAT_FORM = "&platform="
        const val PARAMS_ESSENTIAL_PRODUCT_ID = "&essentialProductId="
        const val PARAMS_MARKET_ID = "&market="
        const val PARAMS_TAGS = "&tags="
        const val PARAMS_TAG = "&tag="
        const val PARAMS_CATEGORIES = "&categories="
        const val PARAMS_CERTIFICATION_ID = "&certificationId="
        const val PARAMS_CERTIFICATIONS = "&certifications="
        const val PARAMS_TIME_DELIVERED = "&timeDelivered="
        const val PARAMS_IS_CONVERSION_UPDATED = "&isConversionUpdated="
        const val PARAMS_IS_REJECTED_NON_UPLOADED = "&rejectedAndNotUploaded="
        const val PARAMS_ORDER_PLACED_START_DATE = "&orderPlacedStartDate="
        const val PARAMS_ORDER_PLACED_END_DATE = "&orderPlacedEndDate="
        const val PARAMS_LINKED_TO_ORDER = "&linkedToOrders="
        const val PARAMS_UNIT_SIZE = "&unitSize="
        const val PARAMS_PRODUCT_NAME = "&productName="
        const val PARAMS_UPLOADREQUIRED = "&uploadRequired="
        const val PARAMS_PRODUCT_ID = "&productId="

        fun arraystoString(data: Array<String>): String {
            var str = ""
            for (i in data.indices) {
                str = if (i != data.size - 1) {
                    str + data[i] + ","
                } else {
                    str + data[i]
                }
            }
            return str
        }

        fun arraysToString(data: List<String>): String {
            var str = ""
            for (i in data.indices) {
                str = if (i != data.size - 1) {
                    str + data[i] + ","
                } else {
                    str + data[i]
                }
            }
            return str
        }

        /**
         * encodes the url in correct format.
         *
         * @param urlStr
         * @return
         */
        private fun encodeUrl(urlStr: String): String {
            var urlEncoded = urlStr
            try {
                var url = URL(urlStr)
                val uri = URI(
                    url.protocol,
                    url.userInfo,
                    url.host,
                    url.port,
                    url.path,
                    url.query,
                    url.ref
                )
                url = uri.toURL()
                urlEncoded = url.toString()
            } catch (e: MalformedURLException) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception(urlStr + "and url malform or wrong syntax error: " + e.message))
                e.printStackTrace()
            } catch (e: URISyntaxException) {
                FirebaseCrashlytics.getInstance()
                    .recordException(Exception(urlStr + "and url malform or wrong syntax error: " + e.message))
                e.printStackTrace()
            }
            return urlEncoded
        }
    }
}