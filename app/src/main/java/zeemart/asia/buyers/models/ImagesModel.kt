package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by ParulBhandari on 2/1/2018.
 */
class ImagesModel {
    @SerializedName("imageFileNames")
    @Expose
    lateinit var imageFileNames: MutableList<String>

    @SerializedName("imageURL")
    @Expose
    var imageURL: String? = null
    override fun toString(): String {
        return "ImagesModel{" +
                "imageFileNames=" + imageFileNames +
                ", imageURL='" + imageURL + '\'' +
                '}'
    }
}