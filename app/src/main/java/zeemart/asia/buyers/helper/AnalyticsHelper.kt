package zeemart.asia.buyers.helper

import android.content.Context
import android.provider.Settings
import com.google.firebase.analytics.FirebaseAnalytics
import zeemart.asia.buyers.helper.MixPanelHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.EssentialsBaseResponse.Essential
import zeemart.asia.buyers.models.GrnRequest.CustomLineItem
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse
import zeemart.asia.buyers.modelsimport.CreateCompanyOutletRequest
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes.Announcements
import java.util.*

object AnalyticsHelper {
    const val CLICK_EVENT = FirebaseAnalytics.Event.SELECT_CONTENT
    const val TAP_ORDERS_TAB = "TAP_ORDERS_TAB"
    const val TAP_INVENTORY_TAB = "TAP_INVENTORY_TAB"
    const val TAP_INVOICE_TAB = "TAP_INVOICE_TAB"
    const val TAP_REPORTS_TAB = "TAP_REPORTS_TAB"
    const val TAP_MORE_TAB = "TAP_MORE_TAB"
    const val TAP_NOTIFICATIONS = "TAP_NOTIFICATIONS"
    const val TAP_DASHBOARD_REPEAT_ORDER = "TAP_DASHBOARD_REPEAT_ORDER"
    const val TAP_DASHBOARD_NEW_ORDER = "TAP_DASHBOARD_NEW_ORDER"
    const val TAP_DASHBOARD_VIEW_ORDERS = "TAP_DASHBOARD_VIEW_ORDERS"
    const val TAP_DASHBOARD_DELIVERIES = "TAP_DASHBOARD_DELIVERIES"
    const val TAP_ITEM_DASHBOARD_ACTION_REQUIRED = "TAP_ITEM_DASHBOARD_ACTION_REQUIRED"
    const val TAP_ITEM_DASHBOARD_CONTINUE_DRAFT = "TAP_ITEM_DASHBOARD_CONTINUE_DRAFT"
    const val SLIDE_ITEM_DASHBOARD_DELETE_DRAFT = "SLIDE_ITEM_DASHBOARD_DELETE_DRAFT"
    const val TAP_ITEM_DASHBOARD_ORDERS_PENDING = "TAP_ITEM_DASHBOARD_ORDERS_PENDING"
    const val TAP_DASHBOARD_VIEW_PLACED_ORDERS = "TAP_DASHBOARD_VIEW_PLACED_ORDERS"
    const val TAP_ITEM_DASHBOARD_DELIVERIES_TODAY = "TAP_ITEM_DASHBOARD_DELIVERIES_TODAY"
    const val SLIDE_ITEM_DASHBOARD_RECEIVE_DELIVERIES = "SLIDE_ITEM_DASHBOARD_RECEIVE_DELIVERIES"
    const val TAP_DASHBOARD_VIEW_ALL_DELIVERIES = "TAP_DASHBOARD_VIEW_ALL_DELIVERIES"
    const val TAP_CREATE_ORDER_SEARCH = "TAP_CREATE_ORDER_SEARCH"
    const val TAP_ADD_TO_ORDER_SEARCH_PRODUCT = "TAP_ADD_TO_ORDER_SEARCH_PRODUCT"
    const val TAP_ADD_TO_ORDER_FAVOURITE = "TAP_ADD_TO_ORDER_FAVOURITE"
    const val TAP_ITEM_ADD_TO_ORDER_PRODUCT_DETAILS = "TAP_ITEM_ADD_TO_ORDER_PRODUCT_DETAILS"
    const val TAP_ORDER_REVIEW_DELIVER_ON = "TAP_ORDER_REVIEW_DELIVER_ON"
    const val TAP_ORDER_REVIEW_ADD_NOTES = "TAP_ORDER_REVIEW_ADD_NOTES"
    const val TAP_ORDER_REVIEW_SPECIAL_REQUEST = "TAP_ORDER_REVIEW_SPECIAL_REQUEST"
    const val TAP_ORDER_REVIEW_USE_PROMO_CODE = "TAP_ORDER_REVIEW_USE_PROMO_CODE"
    const val TAP_ORDER_REVIEW_PAGE_DELETE_DRAFT = "TAP_ORDER_REVIEW_PAGE_DELETE_DRAFT"
    const val TAP_ORDER_REVIEW_PLACE_ORDER = "TAP_ORDER_REVIEW_PLACE_ORDER"
    const val TAP_ORDERS_LIST_SEARCH_ORDER = "TAP_ORDERS_LIST_SEARCH_ORDER"
    const val TAP_ORDERS_LIST_FILTER_ORDER = "TAP_ORDERS_LIST_FILTER_ORDER"
    const val TAP_ITEM_ORDERS_LIST_ORDER = "TAP_ITEM_ORDERS_LIST_ORDER"
    const val SLIDE_ITEM_ORDERS_LIST_CANCEL = "SLIDE_ITEM_ORDERS_LIST_CANCEL"
    const val SLIDE_ITEM_ORDERS_LIST_REPEAT = "SLIDE_ITEM_ORDERS_LIST_REPEAT"
    const val TAP_ORDER_DETAILS_MORE = "TAP_ORDER_DETAILS_MORE"
    const val TAP_ORDER_DETAILS_CANCEL_ORDER = "TAP_ORDER_DETAILS_CANCEL_ORDER"
    const val TAP_ORDER_DETAILS_VIEW_AS_PDF = "TAP_ORDER_DETAILS_VIEW_AS_PDF"
    const val TAP_ORDER_DETAILS_REPORT_ISSUE = "TAP_ORDER_DETAILS_REPORT_ISSUE"
    const val TAP_ORDER_DETAILS_ORDER_STATUS = "TAP_ORDER_DETAILS_ORDER_STATUS"
    const val TAP_ORDER_DETAILS_CONTACT_SUPPLIER = "TAP_ORDER_DETAILS_CONTACT_SUPPLIER"
    const val TAP_ORDER_DETAILS_REPEAT_ORDER = "TAP_ORDER_DETAILS_REPEAT_ORDER"
    const val TAP_ORDER_DETAILS_RECEIVE_ORDER = "TAP_ORDER_DETAILS_RECEIVE_ORDER"
    const val TAP_DELIVERIES_SEARCH_DELIVERY = "TAP_DELIVERIES_SEARCH_DELIVERY"
    const val TAP_DELIVERIES_FILTER_DELIVERY = "TAP_DELIVERIES_FILTER_DELIVERY"
    const val TAP_DELIVERIES_PAST_TAB = "TAP_DELIVERIES_PAST_TAB"
    const val TAP_DELIVERIES_UPCOMING_TAB = "TAP_DELIVERIES_UPCOMING_TAB"
    const val TAP_ITEM_DELIVERIES_UPCOMING_ORDER = "TAP_ITEM_DELIVERIES_UPCOMING_ORDER"
    const val TAP_ITEM_DELIVERIES_PAST_ORDER = "TAP_ITEM_DELIVERIES_PAST_ORDER"
    const val SLIDE_ITEM_DELIVERIES_RECEIVE = "SLIDE_ITEM_DELIVERIES_RECEIVE"
    const val TAP_INVOICES_INVOICE_TAB = "TAP_INVOICES_INVOICE_TAB"
    const val TAP_INVOICES_UPLOADS_TAB = "TAP_INVOICES_UPLOADS_TAB"
    const val TAP_INVOICES_ADD_INVOICE = "TAP_INVOICES_ADD_INVOICE"
    const val TAP_ITEM_INVOICES_INVOICE = "TAP_ITEM_INVOICES_INVOICE"
    const val TAP_INVOICE_DETAIL_LINKED_ORDER = "TAP_INVOICE_DETAIL_LINKED_ORDER"
    const val TAP_INVOICE_DETAIL_DOWNLOAD_PDF = "TAP_INVOICE_DETAIL_DOWNLOAD_PDF"
    const val TAP_INVOICE_DETAIL_VIEW_ORIGINAL_INVOICE = "TAP_INVOICE_DETAIL_VIEW_ORIGINAL_INVOICE"
    const val TAP_INVOICE_DETAIL_MORE = "TAP_INVOICE_DETAIL_MORE"
    const val TAP_INVOICE_DETAIL_DELETE_INVOICE = "TAP_INVOICE_DETAIL_DELETE_INVOICE"
    const val TAP_INVOICE_DETAIL_REPORT = "TAP_INVOICE_DETAIL_REPORT"
    const val TAP_UPLOADS_INVOICES_EDIT = "TAP_UPLOADS_INVOICES_EDIT"
    const val TAP_UPLOADS_INVOICES_EDIT_DELETE = "TAP_UPLOADS_INVOICES_EDIT_DELETE"
    const val TAP_UPLOADS_INVOICES_EDIT_MERGE = "TAP_UPLOADS_INVOICES_EDIT_MERGE"
    const val TAP_ITEM_UPLOADS_INVOICES_INVOICE = "TAP_ITEM_UPLOADS_INVOICES_INVOICE"
    const val SLIDE_ITEM_UPLOADS_INVOICES_DELETE = "SLIDE_ITEM_UPLOADS_INVOICES_DELETE"
    const val TAP_UPLOADS_INVOICES_DETAIL_MORE = "TAP_UPLOADS_INVOICES_DETAIL_MORE"
    const val TAP_UPLOADS_INVOICES_DETAIL_DELETE = "TAP_UPLOADS_INVOICES_DETAIL_DELETE"
    const val TAP_UPLOADS_INVOICES_DETAIL_SHARE = "TAP_UPLOADS_INVOICES_DETAIL_SHARE"
    const val TAP_REPORTS_SEARCH = "TAP_REPORTS_SEARCH"
    const val TAP_REPORTS_WEEK_TAB = "TAP_REPORTS_WEEK_TAB"
    const val TAP_REPORTS_MONTH_TAB = "TAP_REPORTS_MONTH_TAB"
    const val TAP_REPORTS_PREVIOUS_WEEK = "TAP_REPORTS_PREVIOUS_WEEK"
    const val TAP_REPORTS_PREVIOUS_TWO_WEEK = "TAP_REPORTS_PREVIOUS_TWO_WEEK"
    const val TAP_REPORTS_PREVIOUS_MONTH = "TAP_REPORTS_PREVIOUS_MONTH"
    const val TAP_REPORTS_PREVIOUS_TWO_MONTH = "TAP_REPORTS_PREVIOUS_TWO_MONTH"
    const val TAP_REPORTS_MONTH_VIEW_SPENDING_DETAILS = "TAP_REPORTS_MONTH_VIEW_SPENDING_DETAILS"
    const val TAP_REPORTS_WEEK_VIEW_SPENDING_DETAILS = "TAP_REPORTS_WEEK_VIEW_SPENDING_DETAILS"
    const val TAP_REPORTS_VIEW_ALL_PRICE_CHANGES = "TAP_REPORTS_VIEW_ALL_PRICE_CHANGES"
    const val TAP_TOTAL_SPENDING_SUPPLIER_TAB = "TAP_TOTAL_SPENDING_SUPPLIER_TAB"
    const val TAP_ITEM_TOTAL_SPENDING_SUPPLIER_LIST = "TAP_ITEM_TOTAL_SPENDING_SUPPLIER_LIST"
    const val TAP_TOTAL_SPENDING_CATEGORY_TAB = "TAP_TOTAL_SPENDING_CATEGORY_TAB"
    const val TAP_ITEM_TOTAL_SPENDING_CATEGORY_LIST = "TAP_ITEM_TOTAL_SPENDING_CATEGORY_LIST"
    const val TAP_TOTAL_SPENDING_SKU_TAB = "TAP_TOTAL_SPENDING_SKU_TAB"
    const val TAP_ITEM_TOTAL_SPENDING_SKU_LIST = "TAP_ITEM_TOTAL_SPENDING_SKU_LIST"
    const val TAP_TOTAL_SPENDING_SHARE_REPORT = "TAP_TOTAL_SPENDING_SHARE_REPORT"
    const val TAP_TOTAL_SPENDING_SPENDING_SUMMARY = "TAP_TOTAL_SPENDING_SPENDING_SUMMARY"
    const val TAP_TOTAL_SPENDING_SPENDING_RAW_DATA = "TAP_TOTAL_SPENDING_SPENDING_RAW_DATA"
    const val TAP_TOTAL_SPENDING_LIST_OF_INVOICE = "TAP_TOTAL_SPENDING_LIST_OF_INVOICE"
    const val TAP_REPORTS_SKU_SPENDING_DETAILS = "TAP_REPORTS_SKU_SPENDING_DETAILS"
    const val TAP_REPORTS_SKU_PRICE_HISTORY = "TAP_REPORTS_SKU_PRICE_HISTORY"
    const val TAP_REPEAT_ORDER_ORDER_DETAILS = "TAP_REPEAT_ORDER_ORDER_DETAILS"
    const val TAP_REPEAT_ORDER_REVIEW_ORDER = "TAP_REPEAT_ORDER_REVIEW_ORDER"
    const val TAP_REPEAT_ORDER_REVIEW_COMBINED_ORDER = "TAP_REPEAT_ORDER_REVIEW_COMBINED_ORDER"
    const val TAP_REPEAT_ORDER_DETAIL_SELECT_ORDER = "TAP_REPEAT_ORDER_DETAIL_SELECT_ORDER"
    const val TAP_ORDER_STATUS_ACTIVITY_EMAIL = "TAP_ORDER_STATUS_ACTIVITY_EMAIL"
    const val TAP_ORDER_STATUS_ACTIVITY_SEND_EMAIL = "TAP_ORDER_STATUS_ACTIVITY_SEND_EMAIL"
    const val TAP_ORDER_STATUS_ACTIVITY_COPY_ADDRESS = "TAP_ORDER_STATUS_ACTIVITY_COPY_ADDRESS"
    const val TAP_ORDER_STATUS_ACTIVITY_PHONE = "TAP_ORDER_STATUS_ACTIVITY_PHONE"
    const val TAP_ORDER_STATUS_ACTIVITY_MESSAGE = "TAP_ORDER_STATUS_ACTIVITY_MESSAGE"
    const val TAP_ORDER_STATUS_ACTIVITY_CALL = "TAP_ORDER_STATUS_ACTIVITY_CALL"
    const val LOGIN_SUCCESS = "LOGIN_SUCCESS"
    const val TAP_CREATE_ORDER_SELECT_OUTLET = "TAP_CREATE_ORDER_SELECT_OUTLET"
    const val TAP_ITEM_CREATE_ORDER_OUTLET = "TAP_ITEM_CREATE_ORDER_OUTLET"
    const val TAP_ITEM_CREATE_ORDER_SUPPLIER = "TAP_ITEM_CREATE_ORDER_SUPPLIER"
    const val TAP_ITEM_ADD_TO_ORDER_ADD_TO_ORDER = "TAP_ITEM_ADD_TO_ORDER_ADD_TO_ORDER"
    const val TAP_ITEM_ADD_TO_ORDER_FILTER = "TAP_ITEM_ADD_TO_ORDER_FILTER"
    const val TAP_ORDER_REVIEW_DELETE_PRODUCT = "TAP_ORDER_REVIEW_DELETE_PRODUCT"
    const val TAP_ORDER_REVIEW_SELECT_FROM_CATALOG = "TAP_ORDER_REVIEW_SELECT_FROM_CATALOG"
    const val TAP_ORDER_REVIEW_BACK = "TAP_ORDER_REVIEW_BACK"
    const val TAP_CREATE_ORDER_FAVOURITE_LIST = "TAP_CREATE_ORDER_FAVOURITE_LIST"
    const val TAP_NOTIFICATIONS_NEWS_VIEW_PAGE = "TAP_NOTIFICATIONS_NEWS_VIEW_PAGE"
    const val TAP_NOTIFICATIONS_ACTIVITY_VIEW_PAGE = "TAP_NOTIFICATIONS_ACTIVITY_VIEW_PAGE"
    const val TAP_NOTIFICATIONS_NOTIFICATION_SETTINGS = "TAP_NOTIFICATIONS_NOTIFICATION_SETTINGS"
    const val TAP_NOTIFICATIONS_NEWS_DETAIL = "TAP_NOTIFICATIONS_NEWS_DETAIL"
    const val TAP_NOTIFICATIONS_NEWS_CTA = "TAP_NOTIFICATIONS_NEWS_CTA"
    const val TAP_MORE_TAB_INVENTORY = "TAP_MORE_TAB_INVENTORY"
    const val TAP_MORE_TAB_CHANGE_PASSWORD = "TAP_MORE_TAB_CHANGE_PASSWORD"
    const val TAP_MORE_TAB_LANGUAGE = "TAP_MORE_TAB_LANGUAGE"
    const val TAP_MORE_TAB_PROMO_CODE = "TAP_MORE_TAB_PROMO_CODE"
    const val TAP_MORE_TAB_HELP = "TAP_MORE_TAB_HELP"
    const val TAP_MORE_TAB_ASK_ZEEMART = "TAP_MORE_TAB_ASK_ZEEMART"
    const val TAP_MORE_TAB_SEND_FEEDBACK = "TAP_MORE_TAB_SEND_FEEDBACK"
    const val TAP_MORE_TAB_TERMS_OF_USE = "TAP_MORE_TAB_TERMS_OF_USE"
    const val TAP_MORE_TAB_PRIVACY_POLICY = "TAP_MORE_TAB_PRIVACY_POLICY"
    const val TAP_MORE_TAB_SIGN_OUT = "TAP_MORE_TAB_SIGN_OUT"
    const val TAP_UPLOAD_INVOICE = "TAP_UPLOAD_INVOICE"
    const val TAP_ORDER_REVIEW_APPROVE_ORDER = "TAP_ORDER_REVIEW_APPROVE_ORDER"
    const val TAP_ADD_INVOICE_FROM_GALLERY = "TAP_ADD_INVOICE_FROM_GALLERY"
    const val TAP_ADD_INVOICE_TAKE_PHOTO = "TAP_ADD_INVOICE_TAKE_PHOTO"
    const val TAP_INVOICE_PAY = "TAP_INVOICE_PAY"
    const val TAP_ORDER_REVIEW_PLACE_ORDER_DEALS = "TAP_ORDER_REVIEW_PLACE_ORDER_DEALS"
    const val TAP_CREATE_ORDER_DEALS = "TAP_CREATE_ORDER_DEALS"
    const val TAP_CREATE_ORDER_ESSENTIALS = "TAP_CREATE_ORDER_ESSENTIALS"
    const val TAP_ORDER_REVIEW_PLACE_ORDER_ESSENTIALS = "TAP_ORDER_REVIEW_PLACE_ORDER_ESSENTIALS"
    const val TAP_ORDER_PAY_BY_CARD = "TAP_ORDER_PAY_BY_CARD"
    const val TAP_ORDER_PAY_BY_PAYNOW = "TAP_ORDER_PAY_BY_PAYNOW"
    const val TAP_ORDER_PAY_BY_BANKTRANSFER = "TAP_ORDER_PAY_BY_BANKTRANSFER"
    const val TAP_ORDER_UPLOAD_PROOF = "TAP_ORDER_UPLOAD_PROOF"
    const val TAP_WELCOME1_NEXT = "TAP_WELCOME1_NEXT"
    const val TAP_WELCOME1_LETS_START = "TAP_WELCOME1_LETS_START"
    const val TAP_WELCOME2_SEEWHATSAVAILABLE = "TAP_WELCOME2_SEEWHATSAVAILABLE"
    const val TAP_WELCOME1_LOGIN = "TAP_WELCOME1_LOGIN"
    const val TAP_WELCOME2_LOGIN = "TAP_WELCOME2_LOGIN"
    const val TAP_WELCOME1_SIGNUP = "TAP_WELCOME1_SIGNUP"
    const val TAP_WELCOME2_SIGNUP = "TAP_WELCOME2_SIGNUP"
    const val TAP_GUEST_ENTER_ADDRESS_DONE = "TAP_GUEST_ENTER_ADDRESS_DONE"
    const val TAP_GUEST_CREATE_ORDER_SEARCH = "TAP_GUEST_CREATE_ORDER_SEARCH"
    const val TAP_GUEST_CREATE_ORDER_ESSENTIALS = "TAP_GUEST_CREATE_ORDER_ESSENTIALS"
    const val TAP_GUEST_MKTLIST_SIGNUP_TO_ORDER = "TAP_GUEST_MKTLIST_SIGNUP_TO_ORDER"
    const val TAP_GUEST_CREATE_ORDER_DEALS = "TAP_GUEST_CREATE_ORDER_DEALS"
    const val TAP_SIGNUP_EMAIL_USE_MOBILE = "TAP_SIGNUP_EMAIL_USE_MOBILE"
    const val TAP_SIGNUP_EMAIL_USE_EMAIL = "TAP_SIGNUP_EMAIL_USE_EMAIL"
    const val TAP_SIGNUP_MOBILE_SIGNUP = "TAP_SIGNUP_MOBILE_SIGNUP"
    const val TAP_SIGNUP_EMAIL_SIGNUP = "TAP_SIGNUP_EMAIL_SIGNUP"
    const val TAP_SIGNUP_EMAIL_VERIFY_CONTINUE = "TAP_SIGNUP_EMAIL_VERIFY_CONTINUE"
    const val TAP_SIGNUP_MOBILE_VERIFY_CONTINUE = "TAP_SIGNUP_MOBILE_VERIFY_CONTINUE"
    const val TAP_SIGNUP_EMAIL_PASSWORD_CONTINUE = "TAP_SIGNUP_EMAIL_PASSWORD_CONTINUE"
    const val TAP_SIGNUP_MOBILE_PASSWORD_CONTINUE = "TAP_SIGNUP_MOBILE_PASSWORD_CONTINUE"
    const val TAP_SIGNUP_MOBILE_COMPLETE_CREATE_ACC = "TAP_SIGNUP_MOBILE_COMPLETE_CREATE_ACC"
    const val TAP_SIGNUP_EMAIL_COMPLETE_CREATE_ACC = "TAP_SIGNUP_EMAIL_COMPLETE_CREATE_ACC"
    const val TAP_GRN_PAGE_SAVE_GRN = "TAP_GRN_PAGE_SAVE_GRN"
    const val TAP_MERGE_INVOICES = "TAP_MERGE_INVOICES"
    const val TAP_VIEW_ALL_REJECTED_INVOICES = "TAP_VIEW_ALL_REJECTED_INVOICES"
    const val TAP_DELETE_RE_UPLOAD_INVOICE = "TAP_DELETE_RE_UPLOAD_INVOICE"
    const val TAP_VIEW_RELATED_ORDER_TO_INVOICE = "TAP_VIEW_RELATED_ORDER_TO_INVOICE"
    const val TAP_INVOICES_SUPPLIER_FILTER = "TAP_UPLOAD_BYORDER_FILTERSUPPLIER"
    const val TAP_INVOICES_SORT_BY_FILTER = "TAP_UPLOAD_BYORDER_SHOWBY"
    const val TAP_INVOICES_ONLY_REJECTED_UPLOADS_FILTER = "TAP_UPLOAD_BYORDER_UPLOADSTATUS"
    const val TAP_ORDER_INVOICE_ITEM = "TAP_ORDER_INVOICE_ITEM"
    const val TAP_UPLOAD_BYORDER_UPLOAD = "TAP_UPLOAD_BYORDER_UPLOAD"
    const val TAP_UPLOAD_BYORDER_DETAIL_UPLOAD = "TAP_UPLOAD_BYORDER_DETAIL_UPLOAD"
    const val TAP_UPLOAD_NONZM_UPLOAD = "TAP_UPLOAD_NONZM_UPLOAD"
    const val TAP_VIEW_ORDER_DETAILS_INVOICE_ORDER_ITEM = "TAP_UPLOAD_BYORDER_DETAIL_VIEWORDER"
    const val TAP_NON_ZM_ORDERS_INVOICES_MENU_OPTION = "TAP_UPLOAD_BYORDER_NONZM"
    const val TAP_MERGE_INVOICES_REVIEW_INVOICES_SCREEN =
        "TAP_MERGE_INVOICES_REVIEW_INVOICES_SCREEN"
    const val TAP_GRN_PAGE_SELECT_PHOTO = "TAP_GRN_PAGE_SELECT_PHOTO"
    const val TAP_INVENTORY_LISTS = "TAP_INVENTORY_LISTS"
    const val TAP_INVENTORY_LISTS_LIST = "TAP_INVENTORY_LISTS_LIST"
    const val TAP_INVENTORY_LISTS_COUNT = "TAP_INVENTORY_LISTS_COUNT"
    const val TAP_INVENTORY_LIST_SEARCH = "TAP_INVENTORY_LIST_SEARCH"
    const val TAP_INVENTORY_LIST_ADJUSTMENT = "TAP_INVENTORY_LIST_ADJUSTMENT"
    const val TAP_INVENTORY_LIST_EDITQTY = "TAP_INVENTORY_LIST_EDITQTY"
    const val TAP_INVENTORY_LIST_ADJUSTMENT_SAVE = "TAP_INVENTORY_LIST_ADJUSTMENT_SAVE"
    const val TAP_INVENTORY_LIST_EDITQTY_SAVE = "TAP_INVENTORY_LIST_EDITQTY_SAVE"
    const val TAP_INVENTORY_LISTS_LIST_COUNT = "TAP_INVENTORY_LISTS_LIST_COUNT"
    const val TAP_INVENTORY_LISTS_LIST_COUNT_SAVE = "TAP_INVENTORY_LISTS_LIST_COUNT_SAVE"
    const val TAP_INVENTORY_ACTIVITY = "TAP_INVENTORY_ACTIVITY"
    const val TAP_INVENTORY_ACTIVITY_DETAIL = "TAP_INVENTORY_ACTIVITY_DETAIL"
    const val TAP_INVENTORY_ACTIVITY_FILTER = "TAP_INVENTORY_ACTIVITY_FILTER"
    fun logGuestAction(context: Context, action: String?) {
        val logProperties = HashMap<String?, Any?>()
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(context: Context, action: String?, deal: DealsBaseResponse.Deals) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_POST_CODE] =
            SharedPref.read(SharedPref.USER_PIN_CODE, "")
        logProperties[Property.SUPPLIER_ID] = deal.supplier!!.supplierId
        logProperties[Property.SUPPLIER_NAME] = deal.supplier!!.supplierName
        logProperties[Property.DEALS_ID] = deal.dealNumber
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(
        context: Context,
        action: String?,
        images: List<GrnRequest.Image?>?,
        products: List<GrnRequest.Product?>,
        customLineItems: List<CustomLineItem?>,
        receivedDate: Long?,
        mOrder: Orders,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.ORDER_ID] = mOrder.orderId
        logProperties[Property.OUTLET_NAME] = mOrder.outlet!!.outletName
        logProperties[Property.PRODUCT_COUNT] = products.size
        logProperties[Property.CUSTOM_LINE_ITEMS] = customLineItems.size
        logProperties[Property.ORDER_DELIVERY_DATE] = receivedDate
        if (images != null && images.size > 0) {
            logProperties[Property.HAS_UPLOAD] = "true"
        } else {
            logProperties[Property.HAS_UPLOAD] = "false"
        }
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(context: Context, action: String?, mOrder: Orders) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.ORDER_ID] = mOrder.orderId
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(context: Context, action: String?, essential: Essential) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_POST_CODE] =
            SharedPref.read(SharedPref.USER_PIN_CODE, "")
        logProperties[Property.SUPPLIER_ID] = essential.supplier!!.supplierId
        logProperties[Property.SUPPLIER_NAME] =
            essential.supplier!!.supplierName
        logProperties[Property.ESSENTIALS_ID] = essential.essentialsId
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(context: Context, action: String?, essential: ProductDetailBySupplier) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_POST_CODE] = SharedPref.read(SharedPref.USER_PIN_CODE, "")
        if (essential.supplier != null) {
            if (!StringHelper.isStringNullOrEmpty(essential.supplier!!.supplierId)) {
                logProperties[Property.SUPPLIER_ID] = essential.supplier!!.supplierId
            } else if (!StringHelper.isStringNullOrEmpty(essential.supplier!!.supplierName)) {
                logProperties[Property.SUPPLIER_NAME] =
                    essential.supplier!!.supplierName
            } else {
                logProperties[Property.SUPPLIER_NAME] = "no"
                logProperties[Property.SUPPLIER_ID] = "no"
            }
        }
        if (!StringHelper.isStringNullOrEmpty(essential.dealNumber)) {
            logProperties[Property.DEALS] = "yes"
        } else {
            logProperties[Property.DEALS] = "no"
        }
        if (!StringHelper.isStringNullOrEmpty(essential.essentialsId)) {
            logProperties[Property.ESSENTIALS] = "yes"
        } else {
            logProperties[Property.ESSENTIALS] = "no"
        }
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(
        context: Context,
        action: String?,
        isMobileSelected: Boolean,
        value: String,
    ) {
        val logProperties = HashMap<String?, Any>()
        if (isMobileSelected) {
            logProperties[Property.PHONE] = value
        } else {
            logProperties[Property.EMAIL] = value
        }
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logGuestAction(
        context: Context,
        action: String?,
        createCompanyOutletRequest: CreateCompanyOutletRequest,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_NAME] = createCompanyOutletRequest.outletName
        logProperties[Property.COMPANY_NAME] = createCompanyOutletRequest.companyName
        logProperties[Property.TAGS] = createCompanyOutletRequest.otherCuisineFeatures
        val m_androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        logProperties[Property.DEVICE_ID] = m_androidId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(context: Context?, action: String?) {
        MixPanelHelper.pushEvent(context, action)
    }

    fun logAction(context: Context?, action: String?, order: Orders) {
        val logProperties = HashMap<String?, Any?>()
        val currentDate =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        logProperties[Property.OUTLET_ID] = order.outlet!!.outletId
        logProperties[Property.OUTLET_NAME] = order.outlet!!.outletName
        logProperties[Property.SUPPLIER_ID] = order.supplier!!.supplierId
        logProperties[Property.SUPPLIER_NAME] = order.supplier!!.supplierName
        logProperties[Property.ORDER_ID] = order.orderId
        logProperties[Property.ORDER_DELIVERY_DATE] = DateHelper.getStandardDateFormat(
            order.timeDelivered!!
        )
        logProperties[Property.PRODUCT_COUNT] = order.products!!.size
        logProperties[Property.PROMO_CODE] = order.isPromoCodeApplied
        logProperties[Property.SPECIAL_NOTES] = order.isSpecialNotesAvailable
        logProperties[Property.ORDER_PLACED_DATE] = currentDate
        if (order.amount != null) {
            val amount = order.amount!!.total!!.amount!!
            logProperties[Property.ORDER_AMOUNT] = amount
            logProperties[Property.CURRENCY_CODE] = order.amount!!.total!!.currencyCode
        }
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(context: Context?, action: String?, outlet: Outlet) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] = outlet.outletName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        supplierDataModel: DetailSupplierDataModel,
        product: ProductDetailBySupplier,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = supplierDataModel.outlet!!.outletId
        logProperties[Property.SUPPLIER_ID] =
            supplierDataModel.supplier.supplierId
        logProperties[Property.SUPPLIER_NAME] =
            supplierDataModel.supplier.supplierName
        logProperties[Property.PRODUCT_NAME] = product.productName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        isFavourite: Boolean,
        supplier: Supplier,
        product: ProductDetailBySupplier,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.FAVOURITE] = isFavourite
        logProperties[Property.SUPPLIER_ID] = supplier.supplierId
        logProperties[Property.PRODUCT_NAME] = product.productName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(context: Context?, action: String?, buyerDetails: BuyerDetails) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.USER_NAME] =
            buyerDetails.userDetails!!.email
        logProperties[Property.USER_TYPE] = buyerDetails.userDetails!!.roleGroup
        logProperties[Property.USER_MARKET] =
            buyerDetails.market
        logProperties[Property.USER_LANGUAGE] =
            buyerDetails.language
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(context: Context?, action: String?, outlet: Outlet, supplier: Supplier) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.SUPPLIER_ID] =
            supplier.supplierId
        logProperties[Property.SUPPLIER_NAME] = supplier.supplierName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logActionL(
        context: Context?,
        action: String?,
        imageUriRotationList: ArrayList<InQueueForUploadDataModel?>,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] =
            SharedPref.defaultOutlet?.outletId
        logProperties[Property.OUTLET_NAME] =
            SharedPref.defaultOutlet?.outletName
        logProperties[Property.IMAGE_COUNT] =
            imageUriRotationList.size
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        productDetailBySupplier: ProductDetailBySupplier?,
    ) {
        val logProperties = HashMap<String?, Any?>()
        if (productDetailBySupplier != null) {
            if (productDetailBySupplier.supplier != null) {
                if (!StringHelper.isStringNullOrEmpty(productDetailBySupplier.supplier!!.supplierId)) {
                    logProperties[Property.SUPPLIER_ID] =
                        productDetailBySupplier.supplier!!.supplierId
                }
                if (!StringHelper.isStringNullOrEmpty(productDetailBySupplier.supplier!!.supplierName)) {
                    logProperties[Property.SUPPLIER_NAME] =
                        productDetailBySupplier.supplier!!.supplierName
                }
            }
            logProperties[Property.PRODUCT_NAME] = productDetailBySupplier.productName
            logProperties[Property.FAVOURITE] = productDetailBySupplier.isFavourite
        }
        logProperties[Property.OUTLET_ID] =
            SharedPref.defaultOutlet?.outletId
        logProperties[Property.OUTLET_NAME] =
            SharedPref.defaultOutlet?.outletName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(context: Context?, action: String?, announcements: Announcements) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.NEWS_TITLE] = announcements.title
        logProperties[Property.OUTLET_ID] =
            SharedPref.defaultOutlet?.outletId
        logProperties[Property.OUTLET_NAME] = SharedPref.defaultOutlet?.outletName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    /**
     * pay invoice
     *
     * @param context
     * @param action
     * @param outlet
     * @param supplier
     * @param amount
     * @param invoiceCount
     */
    fun logAction(
        context: Context?,
        action: String?,
        outlet: Outlet,
        supplier: Supplier,
        amount: PriceDetails,
        invoiceCount: Int,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] = outlet.outletName
        logProperties[Property.SUPPLIER_ID] = supplier.supplierId
        logProperties[Property.SUPPLIER_NAME] = supplier.supplierName
        logProperties[Property.ORDER_AMOUNT] = amount.amount
        logProperties[Property.INVOICE_COUNT] = invoiceCount
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        order: Orders,
        dealId: String?,
        dealName: String?,
    ) {
        val currentDate =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = order.outlet!!.outletId
        logProperties[Property.OUTLET_NAME] = order.outlet!!.outletName
        logProperties[Property.ORDER_ID] = order.orderId
        logProperties[Property.ORDER_DELIVERY_DATE] = DateHelper.getStandardDateFormat(
            order.timeDelivered!!
        )
        logProperties[Property.PRODUCT_COUNT] = order.products!!.size
        logProperties[Property.PROMO_CODE] = order.isPromoCodeApplied
        logProperties[Property.SPECIAL_NOTES] = order.isSpecialNotesAvailable
        logProperties[Property.ORDER_PLACED_DATE] = currentDate
        if (order.amount != null) {
            val amount = order.amount!!.total!!.amount!!
            logProperties[Property.ORDER_AMOUNT] = amount
            logProperties[Property.CURRENCY_CODE] = order.amount!!.total!!.currencyCode
        }
        logProperties[Property.DEALS_ID] = dealId
        logProperties[Property.DEALS_Name] = dealName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        outlet: Outlet,
        dealId: String?,
        dealName: String?,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] = outlet.outletName
        logProperties[Property.DEALS_ID] = dealId
        logProperties[Property.DEALS_Name] = dealName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        outlet: Outlet,
        essentialsId: String?,
        supplier: Supplier,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] =
            outlet.outletName
        logProperties[Property.ESSENTIALS_ID] = essentialsId
        logProperties[Property.SUPPLIER_ID] = supplier.supplierId
        logProperties[Property.SUPPLIER_NAME] = supplier.supplierName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(context: Context?, action: String?, order: Orders, essentialId: String?) {
        val currentDate =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = order.outlet!!.outletId
        logProperties[Property.OUTLET_NAME] = order.outlet!!.outletName
        logProperties[Property.SUPPLIER_ID] = order.supplier!!.supplierId
        logProperties[Property.SUPPLIER_NAME] = order.supplier!!.supplierName
        logProperties[Property.ORDER_ID] = order.orderId
        logProperties[Property.ORDER_DELIVERY_DATE] = DateHelper.getStandardDateFormat(
            order.timeDelivered!!
        )
        logProperties[Property.PRODUCT_COUNT] = order.products!!.size
        logProperties[Property.PROMO_CODE] = order.isPromoCodeApplied
        logProperties[Property.SPECIAL_NOTES] = order.isSpecialNotesAvailable
        logProperties[Property.ORDER_PLACED_DATE] = currentDate
        if (order.amount != null) {
            val amount = order.amount!!.total!!.amount!!
            logProperties[Property.ORDER_AMOUNT] = amount
            logProperties[Property.CURRENCY_CODE] = order.amount!!.total!!.currencyCode
        }
        logProperties[Property.ESSENTIALS_ID] = essentialId
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        orderId: String?,
        outlet: Outlet,
        deals: Boolean,
        essential: Boolean,
        orderAmount: PriceDetails,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] = outlet.outletName
        logProperties[Property.ORDER_ID] = orderId
        logProperties[Property.DEALS] = deals
        logProperties[Property.ESSENTIALS] = essential
        logProperties[Property.ORDER_AMOUNT] = orderAmount.amount
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logAction(
        context: Context?,
        action: String?,
        orderId: String?,
        outlet: Outlet,
        deals: Boolean,
        essentials: Boolean,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] =
            outlet.outletName
        logProperties[Property.ORDER_ID] = orderId
        logProperties[Property.DEALS] = deals
        logProperties[Property.ESSENTIALS] = essentials
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logActionForInventory(context: Context?, action: String?, outlet: Outlet) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] =
            outlet.outletName
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    @JvmStatic
    fun logActionForInventory(
        context: Context?,
        action: String?,
        outlet: Outlet,
        adjustmentReason: String?,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] = outlet.outletName
        logProperties[Property.ADJUSTMENT_REASON] =
            adjustmentReason
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    fun logActionForInventoryActivity(
        context: Context?,
        action: String?,
        outlet: Outlet,
        activityType: String?,
    ) {
        val logProperties = HashMap<String?, Any?>()
        logProperties[Property.OUTLET_ID] = outlet.outletId
        logProperties[Property.OUTLET_NAME] = outlet.outletName
        logProperties[Property.ACTIVITY_TYPE] = activityType
        MixPanelHelper.pushEvent(context, action, logProperties)
    }

    object Property {
        const val USER_NAME = "Email"
        const val USER_TYPE = "UserType"
        const val USER_MARKET = "Market"
        const val USER_LANGUAGE = "Language"
        const val FAVOURITE = "Favorited"
        const val PRODUCT_COUNT = "Items"
        const val PROMO_CODE = "Promo"
        const val SPECIAL_NOTES = "SpecialNotes"
        const val PRODUCT_NAME = "ProductName"
        const val ORDER_ID = "OrderId"
        const val ORDER_PLACED_DATE = "OrderPlacedDate"
        const val ORDER_DELIVERY_DATE = "OrderDeliveredDate"
        const val SUPPLIER_ID = "SupplierId"
        const val OUTLET_ID = "OutletId"
        const val DEVICE_ID = "DeviceId"
        const val SUPPLIER_NAME = "SupplierName"
        const val OUTLET_NAME = "OutletName"
        const val IMAGE_COUNT = "ImageCount"
        const val ORDER_AMOUNT = "Amount"
        const val CURRENCY_CODE = "Currency"
        const val NEWS_TITLE = "NewsTitle"
        const val INVOICE_COUNT = "InvoiceCount"
        const val DEALS_ID = "DealsId"
        const val DEALS_Name = "DealsName"
        const val ESSENTIALS_ID = "EssentialsId"
        const val DEALS = "Deals"
        const val ESSENTIALS = "Essentials"
        const val OUTLET_POST_CODE = "OutletPostcode"
        const val EMAIL = "Email"
        const val PHONE = "PhoneNo"
        const val COMPANY_NAME = "CompanyName"
        const val TAGS = "tags"
        const val CUSTOM_LINE_ITEMS = "customLineItems"
        const val UPLOAD_IMAGE = "uploadImage"
        const val HAS_UPLOAD = "hasUpload"
        const val ADJUSTMENT_REASON = "AdjustmentReason"
        const val ACTIVITY_TYPE = "ActivityType"
    }
}