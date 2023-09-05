package zeemart.asia.buyers.constants

import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getProperty

/**
 * Created by saiful on 25/9/17.
 */
object ServiceConstant {
    const val AuthServer = "AuthServer"
    const val NotificationServer = "NotificationServer"
    const val InventoryServer = "InventoryServer"
    const val OrdersServer = "OrdersServer"
    const val OrderManagementServer = "OrderManagementServer"
    const val ServiceServer = "ServiceServer"
    const val AccountManagementServer = "AccountManagementServer"
    const val AccountManagement = "AccountManagement"
    const val InvoiceServer = "InvoiceServer"
    const val ImageServer = "ImageServer"
    const val ReportsServer = "ReportsServer"
    const val ReportsManagementServer = "ReportsManagementServer"
    const val StockManagementServer = "StockManagementServer"
    const val LoyaltyProgramManagementServer = "LoyaltyProgramManagementServer"
    const val InventoryManagementServer = "InventoryManagementServer"
    const val PaymentManagementServer = "PaymentManagementServer"
    const val FileManagementServer = "FileManagementServer"
    const val DirectoryServer = "DirectoryServer"
    const val GoogleStorageServer = "GoogleStorageServer"
    const val STATUS_CODE_200_OK = 200
    const val STATUS_CODE_201_CREATED = 201
    const val STATUS_CODE_202_ACCEPTED = 202
    const val STATUS_CODE_400_BAD_REQUEST = 400
    const val STATUS_CODE_401_UNAUTHORIZED = 401
    const val STATUS_CODE_402_PAYMENT_REQUIRED = 402
    const val STATUS_CODE_403_FORBIDDEN = 403
    const val STATUS_CODE_404_NOT_FOUND = 404
    const val STATUS_CODE_405_METHOD_NOT_ALLOWED = 405
    const val STATUS_CODE_500_INTERNAL_SERVER_ERROR = 500
    const val STATUS_CODE_412_INSUFFICIENT_STOCK = 412
    const val TRY_LOGIN_AGAIN_401_MESSAGE = "try login again"
    const val RETRY_API_CALL__500_MESSAGE = "try same api call again"
    const val DISPLAY_APP_SPECIFIC_400_MESSAGE = "display app specific messgae"
    const val USER_TYPE = "buyer"
    val ENDPOINT_RETRIEVE_DEALS = getProperty(DirectoryServer) + "/suppliers/deals"
    val ENDPOINT_RETRIEVE_DEALS_ESSENTIALS = getProperty(DirectoryServer) + "/suppliers/ecommerce"
    val ENDPOINT_SPCL_DEALS =
        getProperty(InventoryManagementServer) + "/inventory/specials/suppliers"
    val ENDPOINT_RETRIEVE_ESSENTIALS = getProperty(DirectoryServer) + "/essentials"
    val ENDPOINT_RETRIEVE_ESSENTIALS_PRODUCTS =
        getProperty(DirectoryServer) + "/essentials/products"
    val ENDPOINT_RETRIEVE_DEAL_PRODUCTS = getProperty(DirectoryServer) + "/suppliers/deals/products"
    val ENDPOINT_SEARCH_DEALS_ESSENTIALS_ON_BOARDING =
        getProperty(DirectoryServer) + "/suppliers/ecommerce/v1"
    val ENDPOINT_VALID_ADD_ON_ORDER_BY_SUPPLIER_ID =
        getProperty(OrderManagementServer) + "/orders/po/validate/addOn"
    val ENDPOINT_VALID_ADD_ON_ORDER_BY_ORDER_ID =
        getProperty(OrderManagementServer) + "/orders/po/validate/draft/addOns"
    val ENDPOINT_SIGN_UP = getProperty(DirectoryServer) + "/accounts/user/onboard"
    val ENDPOINT_CREATE_PASSWORD = getProperty(DirectoryServer) + "/accounts/user/onboard"
    val ENDPOINT_RE_SEND_VC = getProperty(DirectoryServer) + "/accounts/user/onboard/regenerateCode"
    val ENDPOINT_CREATE_COMPANY_DETAILS =
        getProperty(DirectoryServer) + "/accounts/user/onboard/complete"
    val ENDPOINT_AUTHTOKEN = getProperty(AuthServer) + "/" + USER_TYPE + "/login/v1"
    val ENDPOINT_CHANGEPASSWORD = getProperty(AuthServer) + "/changePassword/v1"
    val ENDPOINT_SENDVC = getProperty(AuthServer) + "/sendVerificationCode/v1"
    val ENDPOINT_VALIDATEVC = getProperty(AuthServer) + "/validateVerificationCode/v1"
    val ENDPOINT_RESETPASSWORD = getProperty(AuthServer) + "/resetPassword/v1"
    val ENDPOINT_ORDERS = getProperty(OrderManagementServer) + "/orders/po"
    val ENDPOINT_ORDERS_DRAFT = getProperty(OrderManagementServer) + "/orders/po/draft"
    val ENDPOINT_ORDERS_DRAFT_PLACE = getProperty(OrderManagementServer) + "/orders/po/draft/place"
    val ENDPOINT_IMAGE_SERVER = getProperty(ImageServer) + "/server/php/"
    val ENDPOINT_INVOICES = getProperty(InvoiceServer) + "/invoices"
    val ENDPOINT_INVOICES_PENDING = getProperty(InvoiceServer) + "/invoices/pending"
    val ENDPOINT_INVOICES_PENDING_V1 = getProperty(InvoiceServer) + "/invoices/v1/pending"
    val ENDPOINT_INVOICES_LINKED_TO_ORDER = getProperty(InvoiceServer) + "/invoices/v1"
    val ENDPOINT_INVOICE_PENDING = getProperty(InvoiceServer) + "/invoice/pending"
    val ENDPOINT_INVOICE = getProperty(InvoiceServer) + "/invoice"
    val ENDPOINT_INVOICE_PROCESSED = getProperty(InvoiceServer) + "/invoices/processed"
    val ENDPOINT_INVOICE_COUNT_BY_STATUS = getProperty(InvoiceServer) + "/invoices/countByStatus"
    val ENDPOINT_E_INVOICE_GROUP_SUPPLIER =
        getProperty(InvoiceServer) + "/buyer/eInvoices/groupBySupplier"
    val ENDPOINT_E_CREDIT_SUPPLIER = getProperty(InvoiceServer) + "/supplier/eCreditNote/summary"

    //Report endpoints
    val ENDPOINT_REPORT_SPENDING_DEPRECATED = getProperty(InvoiceServer) + "/invoices/spendings"
    val ENDPOINT_REPORT_REPORT_PRICE_CHANGES =
        getProperty(ReportsManagementServer) + "/reports/priceHistory"

    //Report server new APIs
    val ENDPOINT_REPORT_TOTAL_SPENDING = getProperty(ReportsManagementServer) + "/reports/spendings"
    val ENDPOINT_REPORT_CATEGORY_SPENDING =
        getProperty(ReportsManagementServer) + "/reports/spendings/category"
    val ENDPOINT_REPORT_SKU_SPENDING =
        getProperty(ReportsManagementServer) + "/reports/spendings/sku"
    val ENDPOINT_REPORT_TAGS_SPENDING =
        getProperty(ReportsManagementServer) + "/reports/spendings/outlettags"
    val ENDPOINT_REPORT_TAGS_SPENDING_DETAILS =
        getProperty(ReportsManagementServer) + "/reports/spendings/specifictag"
    val ENDPOINT_REPORT_TOTAL_SPENDING_RANGE =
        getProperty(ReportsManagementServer) + "/reports/spendings/range"
    val ENDPOINT_REPORT_EXPORT_CSV_LIST_OF_INVOICES_AND_RAW_DATA = getProperty(
        ReportsManagementServer
    ) + "/reports/invoices/excel/download/listInvoices"
    val ENDPOINT_REPORT_EXPORT_CSV_SUMMARY =
        getProperty(ReportsManagementServer) + "/reports/invoices/excel/download/spendings"

    //Agreement Terms and Policy
    val ENDPOINT_RETRIEVE_AGREEMENTS = getProperty(AccountManagement) + "/agreements/effective"
    val ENDPOINT_VALIDATE_AGREEMENT = getProperty(AccountManagement) + "/user/agreements/validate"
    val ENDPOINT_ACCEPT_AGREEMENT = getProperty(AccountManagement) + "/user/agreements/accept"
    val ENDPOINT_ORDER_SINGLE_ORDER = getProperty(OrderManagementServer) + "/orders/po"
    val ENDPOINT_CANCEL_ORDER = getProperty(OrderManagementServer) + "/orders/po/cancel"
    val ENDPOINT_EDIT_APPROVE_ORDER = getProperty(OrderManagementServer) + "/orders/po/edit/approve"
    val ENDPOINT_ORDERS_DRAFT_EDIT_PLACE =
        getProperty(OrderManagementServer) + "/orders/po/draft/edit/place"
    val ENDPOINT_RETRIEVE_ORDER_DETAILS =
        getProperty(OrderManagementServer) + "/orders/po/orderById"
    val ENDPOINT_ORDER_RECEIVE_ORDER = getProperty(OrderManagementServer) + "/orders/po/receive"
    val ENDPOINT_ACTIVE_DEALS = getProperty(LoyaltyProgramManagementServer) + "/deals/buyer"
    val ENDPOINT_DEAL_PRODUCTS =
        getProperty(LoyaltyProgramManagementServer) + "/deal/products/buyer"
    val ENDPOINT_PLACE_DEAL_ORDER = getProperty(OrderManagementServer) + "/orders/po/deal"
    val ENDPOINT_NOTIFICATIONS_ANNOUNCEMENTS =
        getProperty(NotificationServer) + "/notification/announcements"
    val ENDPOINT_NOTIFICATIONS_ANNOUNCEMENTS_DETAILS =
        getProperty(NotificationServer) + "/notification/announcement"
    val ENDPOINT_NOTIFICATIONS_ANNOUNCEMENTS_READ =
        getProperty(NotificationServer) + "/notification/announcement/status"
    val ENDPOINT_NOTIFICATIONS = getProperty(NotificationServer) + "/notification"
    val ENDPOINT_USER_PROFILE = getProperty(AccountManagement) + "/user"
    val ENDPOINT_SPECIFIC_OUTLET = getProperty(AccountManagement) + "/outlet"
    val ENDPOINT_SUPPLIER = getProperty(InventoryServer) + "/marketlist/v1/supplier"
    val ENDPOINT_SUPPLIER_NEW =
        getProperty(InventoryManagementServer) + "/inventory/deliveryPreference/delivery/settings"
    val ENDPOINT_PRODUCT_LIST_BY_SUPPLIER = getProperty(InventoryServer) + "/marketlist"
    val ENDPOINT_MARKET_LIST_BY_SUPPLIER =
        getProperty(InventoryManagementServer) + "/inventory/marketlist"
    val ENDPOINT_UIT_SIZE = getProperty(ServiceServer) + "/inventory/Unit_Sizes.json"
    val ENDPOINT_SEARCH_BY_PRODUCT_NAME =
        getProperty(InventoryManagementServer) + "/inventory/marketlist/outlet/search/v1"
    val ENDPOINT_COMPANY_PREFERENCE_LINKED_SUPPLIER =
        getProperty(InventoryManagementServer) + "/inventory/companypreference/linkedsupplier"
    val ENDPOINT_OUTLET_FUTURES = getProperty(GoogleStorageServer) + "/OutletFeatures.json"

    //  public static final String ENDPOINT_OUTLET_FUTURES = ZeemartBuyerApp.getProperty(FileManagementServer) + "/common/outlets/features";
    val ENDPOINT_SAVE_GRN = getProperty(OrderManagementServer) + "/orders/po/grn"
    val ENDPOINT_DELETE_GRN = getProperty(OrderManagementServer) + "/orders/po/grn"
    val ENDPOINT_CREATE_DRAFT_ORDER_FOR_SKU =
        getProperty(OrderManagementServer) + "/orders/po/draft/bysku"
    val ENDPOINT_UPLOAD_REQUIRED = getProperty(OrderManagementServer) + "/orders/po/uploadRequired"
    val ENDPOINT_APPROVE_ORDER = getProperty(OrderManagementServer) + "/orders/po/approval"
    val ENDPOINT_REJECT_ORDER = getProperty(OrderManagementServer) + "/orders/po/reject"
    val ENDPOINT_PRODUCT_DETAIL = getProperty(InventoryServer) + "/product"
    val REPORT_SKU_SEARCH_LIST = getProperty(InventoryServer) + "/marketlist/products"
    val ENDPOINT_NOTIFICATION_APP_LAUNCH_NO_LOGIN =
        getProperty(NotificationServer) + "/notification/default/devices"
    val ENDPOINT_NOTIFICATION_REGISTER_DEVICE =
        getProperty(NotificationServer) + "/notification/devices"
    val ENDPOINT_ACTIVITY_LOG = getProperty(OrderManagementServer) + "/orders/activitylogs"
    val ENDPOINT_ADD_FAVORITES = getProperty(InventoryServer) + "/marketlist/outlet/favourites"
    val ENDPOINT_REPEAT_ORDERS =
        getProperty(OrderManagementServer) + "/orders/po/supplier/recentOrders"
    val ENDPOINT_VIEW_ORDER_SUMMARY =
        getProperty(ReportsManagementServer) + "/reports/orders/chartData/orderDate"
    val ENDPOINT_VIEW_ORDER_SUMMARY_NEW =
        getProperty(ReportsManagementServer) + "/reports/order/summary"
    val ENDPOINT_SEARCH_DEAL_ESSENTIALS =
        getProperty(InventoryManagementServer) + "/inventory/specials/v1"
    val ENDPOINT_VIEW_ESSENTIALS =
        getProperty(InventoryManagementServer) + "/inventory/essentials/buyer"
    val ENDPOINT_VIEW_ORDER_AGAIN_ESSENTIALS =
        getProperty(InventoryManagementServer) + "/inventory/essentials/buyer/recent"
    val ENDPOINT_VIEW_ESSENTIALS_PRODUCTS =
        getProperty(InventoryManagementServer) + "/inventory/essentials/products/buyer"
    val ENDPOINT_MARK_ESSENTIALS_PRODUCT_AS_FAVOURITE =
        getProperty(InventoryManagementServer) + "/inventory/essentials/products/favourite"
    val ENDPOINT_REMOVE_ESSENTIALS_PRODUCT_AS_FAVOURITE =
        getProperty(InventoryManagementServer) + "/inventory/essentials/products/unfavourite"
    val ENDPOINT_PLACE_ESSENTIAL_ORDER =
        getProperty(OrderManagementServer) + "/orders/po/essentials"
    val ENDPOINT_UPLOAD_TRANSACTION_IMAGE_ORDER =
        getProperty(OrderManagementServer) + "/orders/po/payment/receipts"

    /***************** STOCK MANAGEMENT  */
    val ENDPOINT_INVENTORY_OUTLET_SHELVES =
        getProperty(StockManagementServer) + "/inventory/outlet/shelves"
    val ENDPOINT_INVENTORY_OUTLET_LIST_TRANSFER = getProperty(AccountManagement) + "/group/outlets"
    val ENDPOINT_INVENTORY_OUTLET_STOCKAGES =
        getProperty(StockManagementServer) + "/inventory/outlet/stockages"
    val ENDPOINT_INVENTORY_OUTLET_PRODUCTS =
        getProperty(StockManagementServer) + "/inventory/products"
    val ENDPOINT_INVENTORY_OUTLET_SHELVE_STOCKCOUNT =
        getProperty(StockManagementServer) + "/inventory/outlet/shelve/stockCount"
    val ENDPOINT_INVENTORY_OUTLET_STOCKAGE =
        getProperty(StockManagementServer) + "/inventory/outlet/stockage"
    val ENDPOINT_INVENTORY_OUTLET_SHELVE_ADJUSTMENT =
        getProperty(StockManagementServer) + "/inventory/outlet/shelve/adjustment"
    val ENDPOINT_INVENTORY_OUTLET_SHELVE_AMENDMENT =
        getProperty(StockManagementServer) + "/inventory/outlet/shelve/amendment"
    val ENDPOINT_INVENTORY_ORDER_SETTINGS_DELIVERY_DATES =
        getProperty(InventoryManagementServer) + "/inventory/deliveryPreference/settings"
    val ENDPOINT_INVENTORY_SAVE_ORDER_SETTINGS_DELIVERY_DATES = getProperty(
        InventoryManagementServer
    ) + "/inventory/deliveryPreference"
    val ENDPOINT_INVENTORY_RETRIEVE_OUTLETS_SUPPLIERS_FOR_REVIEW = getProperty(
        InventoryManagementServer
    ) + "/inventory/deliveryPreference/linkedSuppliersByOutlet"
    val ENDPOINT_INVENTORY_DRAFT =
        getProperty(StockManagementServer) + "/inventory/outlet/draft/stockage"
    val ENDPOINT_INVENTORY_ON_HAND_HISTORY =
        getProperty(StockManagementServer) + "/inventory/outlet/onhandstock/history"

    /*******************Loyalty Program */
    val ENDPOINT_VALIDATE_PROMOTION_CODE =
        getProperty(LoyaltyProgramManagementServer) + "/promocode/validate"
    val ENDPOINT_PROMOTION_CODES = getProperty(LoyaltyProgramManagementServer) + "/promocodes"
    val ENDPOINT_PROMOTION_CODE = getProperty(LoyaltyProgramManagementServer) + "/promocode"

    /***************** PAYMENT MANAGEMENT  */
    val ENDPOINT_COMPANY_CARD_PAYMENT_DETAILS =
        getProperty(PaymentManagementServer) + "/payments/paymentCardData"
    val ENDPOINT_PAY_INVOICES = getProperty(PaymentManagementServer) + "/payments/invoices/pay"
    val ENDPOINT_PAY_ORDERS = getProperty(PaymentManagementServer) + "/payments/order/pay"
    val ENDPOINT_PAYMENT_CARD_DEFAULT =
        getProperty(PaymentManagementServer) + "/payments/paymentCardData/default"
    val ENDPOINT_PAYMENT_ADD_CARD =
        getProperty(PaymentManagementServer) + "/payments/paymentCardData"
    val ENDPOINT_RETRIEVE_BANK_TRANSFER_ACCOUNT_DETAILS =
        getProperty(PaymentManagementServer) + "/payments/accounts"

    /***************** FILE MANAGEMENT  */
    val ENDPOINT_UPLOAD_FILES_MULTIPART =
        getProperty(FileManagementServer) + "/storage/files/multipart"
    val ENDPOINT_RETRIEVE_COMPANIES = getProperty(AccountManagement) + "/companies"
    val ENDPOINT_RETRIEVE_SPECIFIC_COMPANY = getProperty(AccountManagement) + "/company"
    val ENDPOINT_UPDATE_COMPANIES = getProperty(AccountManagement) + "/company/verification/request"

    /* MISC ENDPOINTS */
    const val ENDPOINT_ZENDESK =
        "https://zeemart.zendesk.com/hc/en-us/categories/115000425151-Existing-users"
    const val ENDPOINT_ZEEMART_TERMS = "https://zeemart.asia/terms/"
    const val ENDPOINT_ZEEMART_SUBSCRIPTION_PLANS = "https://www.zeemart.asia/#comp-knu30qa5"
    const val ENDPOINT_ZEEMART_SUBSCRIPTION_PLANS_ASUTRALIA =
        "https://www.zeemart.asia/buyer-au#comp-l8bfdzbs"
    const val ENDPOINT_ZEEMART_PRIVACY = "https://www.zeemart.asia/privacy-policy"
    const val ENDPOINT_GOOGLE_DOC_EMBED = "https://docs.google.com/gview?embedded=true&url="
    const val ENDPOINT_ZEEMART_REFER_URL =
        "https://www.zeemart.asia/refer-a-friend?utm_source=supplier-order-notification&utm_medium=email&utm_campaign=referral-reengage-v1"
    const val EMAIL_SUPPORT = "help@zeemart.co"
    const val ENDPOINT_ZEEMART_SIGNUP = "https://www.zeemart.asia/buyer-signup"

    /* FILE TYPE */
    const val FILE_TYPE_PLAIN_TEXT = "plain/text"
    const val FILE_TYPE_PDF = "application/pdf"
    const val FILE_TYPE_EXCEL = "application/vnd.ms-excel"
    const val FILE_TYPE_CSV = "text/csv"
    const val FILE_TYPE_IMAGE = "image/*"
    const val FILE_TYPE_IMAGE_JPEG = "image/jpeg"
    const val FILE_TYPE_APPLICATION_PDF = "application/pdf"
    const val FILE_EXT_JPEG = ".jpg"
    const val FILE_EXT_PDF = ".pdf"
    const val FILE_LIST = "file[0]"
    const val MULTIPART_FILE = "multipartFiles"
    const val REQUEST_BODY_HEADER_KEY_CONTENT_TYPE = "Content-Type"
    const val REQUEST_BODY_HEADER_KEY_CONTENT_TYPE_VALUE = "application/json"
    const val DEFAULT_CHARSET = "utf-8"
    const val charset = "DEFAULT_CHARSET"
    const val JSON_STATUS = "status"
    const val JSON_MESSAGE = "message"
    const val REQUEST_BODY_HEADER_KEY_AUTH_TYPE = "authType"
    const val REQUEST_BODY_HEADER_KEY_AUTH_TYPE_VALUE = "Zeemart"
    const val REQUEST_BODY_ZEEMARTID = "ZeemartId"
    const val REQUEST_NEW_PASSWORD_PARAM = "newPassword"
    const val REQUEST_BODY_PASSWORD = "password"
    const val REQUEST_BODY_VERIFICATION_CODE = "verificationCode"
    const val RESPONSE_STATUS_PARAM = "getStatusName"
    const val REQUEST_BODY_AUTH_TOKEN_HEADER = "mudra"
    const val REQUEST_BODY_BUYER_HEADER = "outletId"
    const val NO_ACTION = "response error json or unsupported encoding"
    const val SUCCESS_RESPONSE = "success"
    const val APPLY_FEE = "APPLY_FEE"
    const val BELOW_MINIMUM_ORDER = "BELOW_MINIMUM_ORDER"
    const val ALL_ORDER = "ALL_ORDERS"
    const val REJECT_BELOW_MIN_ORDER = "REJECT_BELOW_MIN_ORDER"
    const val NO_DELIVERY_FEE = "NO_DELIVERY_FEE"
    const val SKU = "sku"
    const val DEVICE_TOKEN = "deviceToken"
    const val PLATFORM = "platform"
    const val GCM = "GCM"
    const val USER_ID = "userId"
    const val ACTIVE = "ACTIVE"
    @JvmField
    var STATUS_CODE_403_404_405_MESSAGE = "show response body message as it is"
    const val INTENT_FOR_WHATS_APP_URL = "https://api.whatsapp.com/send?phone="

    object CrashlyticsCustomErrorCode {
        const val IMAGE_UPLOAD_FAIL = 1001
        const val FROM_GSON_FAIL = 1002
    }
}