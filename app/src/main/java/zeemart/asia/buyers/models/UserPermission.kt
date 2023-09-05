package zeemart.asia.buyers.models

import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref

/**
 * Created by saiful on 7/8/18.
 */
object UserPermission {
    fun UserRole(perm: String): Boolean {
        if (SharedPref.buyerDetail != null) {
            val permList = SharedPref.buyerDetail!!.restrictedAccess
            if (permList?.contains(perm) == true) return false
        }
        return true
    }

    @JvmStatic
    fun HasViewPrice(): Boolean {
        return UserRole(Permission.VIEW_PRICE)
    }

    @JvmStatic
    fun HasViewProcessedInvoice(): Boolean {
        return UserRole(Permission.VIEW_PROCESSED_INVOICE)
    }

    @JvmStatic
    fun HasManageSuppliers(): Boolean {
        return UserRole(Permission.MANAGE_SUPPLIER)
    }

    fun HasManagePayments(): Boolean {
        return UserRole(Permission.MANAGE_PAYMENTS)
    }

    @JvmStatic
    fun HasEditCompanies(): Boolean {
        return UserRole(Permission.COMPANY_EDIT)
    }

    fun HasInvoice(): Boolean {
        return UserRole(Permission.UPLOAD_INVOICE)
    }

    fun HasViewReport(): Boolean {
        return UserRole(Permission.VIEW_REPORT)
    }

    fun HasViewInventory(): Boolean {
        return UserRole(Permission.MANAGE_INVENTORY)
    }

    /**
     * sets the tab ids as per the invoice permission.
     */
    fun setTabIdsAsPerPermissions() {
        var count = 0
        ZeemartAppConstants.HOME_TAB_ID = count
        count = count + 1
        if (HasInvoice()) {
            ZeemartAppConstants.INVOICES_TAB_ID = count
            count = count + 1
        }
        if (HasViewReport()) {
            ZeemartAppConstants.REPORT_TAB_ID = count
            count = count + 1
        }
        if (HasViewInventory()) {
            ZeemartAppConstants.INVENTORY_TAB_ID = count
            count = count + 1
        }
        ZeemartAppConstants.MORE_TAB_ID = count
    }

    object Permission {
        const val VIEW_PRICE = "GENERAL_SHOWPRICE"
        const val UPLOAD_INVOICE = "INVOICE_VIEW"
        const val VIEW_PROCESSED_INVOICE = "INVOICES_VIEW_PROCESSED"
        const val VIEW_REPORT = "MANAGE_REPORTS"
        const val MANAGE_PAYMENTS = "MANAGE_PAYMENTS"
        const val MANAGE_INVENTORY = "MANAGE_INVENTORY"
        const val COMPANY_EDIT = "COMPANY_EDIT"
        const val MANAGE_SUPPLIER = "MANAGE_SUPPLIER"
    }
}