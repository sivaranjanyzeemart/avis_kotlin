package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by saiful on 8/1/18.
 */
class ImageUpload {
    @SerializedName("file")
    @Expose
    var file: List<FileObject>? = null

    inner class FileObject {
        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("url")
        @Expose
        var url: String? = null
    }
}