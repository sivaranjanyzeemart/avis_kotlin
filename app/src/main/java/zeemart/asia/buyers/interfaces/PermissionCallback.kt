package zeemart.asia.buyers.interfaces

/**
 * Created by saiful on 17/5/18.
 */
interface PermissionCallback {
    fun denied(requestCode: Int)
    fun allowed(requestCode: Int)

    companion object {
        const val WRITE_IMAGE = 10001
        const val TAKE_IMAGE = 10002
    }
}