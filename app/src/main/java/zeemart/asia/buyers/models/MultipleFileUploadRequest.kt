package zeemart.asia.buyers.models

import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.annotations.Expose

/**
 * Created by RajPrudhviMarella on 16/June/2021.
 */
class MultipleFileUploadRequest {
    @Expose
    var fileName: String? = null

    @Expose
    var bitmap: Bitmap? = null

    @Expose
    var uri: Uri? = null
}