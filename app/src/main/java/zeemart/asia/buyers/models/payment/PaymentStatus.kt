package zeemart.asia.buyers.models.paymentimport

enum class PaymentStatus(private var mStatusName: String) {
    DELETED("Deleted"), ACTIVE("Active");

    fun getmStatusName(): String {
        return mStatusName
    }

    fun setmStatusName(mStatusName: String) {
        this.mStatusName = mStatusName
    }
}