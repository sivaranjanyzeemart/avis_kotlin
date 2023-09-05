package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MultiPartImageUploadResponse {
    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: MultiPartImageUploadResponse.Data? = null

    inner class Data {
        @SerializedName("parentUrl")
        @Expose
        var parentUrl: String? = null

        @SerializedName("componentType")
        @Expose
        var componentType: String? = null

        @SerializedName("files")
        @Expose
        lateinit var files: List<MultiPartImageUploadResponse.File>
    }

    inner class File {
        @SerializedName("fileName")
        @Expose
        var fileName: String? = null

        @SerializedName("mimeType")
        @Expose
        var mimeType: String? = null

        @SerializedName("fileUrl")
        @Expose
        var fileUrl: String? = null

        @SerializedName("thumbnailUrl")
        @Expose
        var thumbnailUrl: String? = null
    }
}