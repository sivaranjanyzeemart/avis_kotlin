package zeemart.asia.buyers.helper
/**
 * Created by ParulBhandari on 8/16/2018.
 */
object StringHelper {
    fun isStringNullOrEmpty(str: String?): Boolean {
        if (str == null || str.isEmpty()) return true
        return false
    }

    fun join(data: Array<String>): String {
        var str: String = ""
        for (i in data.indices) {
            str = str + data.get(i)
        }
        return str
    }
}