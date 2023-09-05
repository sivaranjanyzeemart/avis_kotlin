package zeemart.asia.buyers.network

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.ImageUpload
import zeemart.asia.buyers.models.MultipleFileUploadRequest
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Created by saiful on 8/1/18.
 */
object ImageUploadHelper {
    private fun GetFileDataFromDrawable(bitmap: Bitmap?): ByteArray {
        var byteArrayOutputStream = ByteArrayOutputStream()
        var compressionRate = 90
        do {
            byteArrayOutputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, compressionRate, byteArrayOutputStream)
            Log.e(
                "getFileDataFromDrawable",
                "byteArrayOutputStream.size()" + byteArrayOutputStream.size()
            )
            compressionRate -= 5
        } while (byteArrayOutputStream.size() > 1000000)
        return byteArrayOutputStream.toByteArray()
    }

    private fun GetPdfBytesData(context: Context, uri: Uri?): ByteArray {
        val baos = ByteArrayOutputStream()
        val fis: InputStream?
        try {
            fis = context.contentResolver.openInputStream(uri!!)
            val buf = ByteArray(1024)
            var n: Int
            while (-1 != fis!!.read(buf).also { n = it }) baos.write(buf, 0, n)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return baos.toByteArray()
    }

    // api id - 40.1 Image upload to image server
    fun UploadBitmap(
        context: Context,
        bitmap: Bitmap?,
        uri: Uri?,
        classType: String,
        categoryType: String,
        filename: String,
        callback: ImageUploaded
    ) {
        //our custom volley request
        val headers: MutableMap<String, String?> = HashMap()
        headers[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = SharedPref.defaultOutlet?.outletId
        val zeemartMultipartAPIRequest: ZeemartMultipartAPIRequest =
            object : ZeemartMultipartAPIRequest(context,
                Method.POST,
                ServiceConstant.ENDPOINT_IMAGE_SERVER,
                Response.Listener { response ->
                    try {
                        val obj = JSONObject(String(response.data))
                        val imageUpload = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            obj.toString(),
                            ImageUpload::class.java
                        )
                        callback.result(imageUpload)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        callback.result(null)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d("Image Upload Error", error.toString())
                    callback.result(null)
                },
                headers
            ) {
                /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["uniqueUploadID"] = "Zeemart-" + System.currentTimeMillis() / 1000L
                    params["class"] = classType
                    if (!StringHelper.isStringNullOrEmpty(categoryType)) {
                        params[categoryType] = "" + System.currentTimeMillis() / 1000L
                    }
                    return params
                }

                /*
             * Here we are passing image by renaming it with a unique name
             * */
                override fun getByteData(): Map<String, DataPart> {
                    val params: MutableMap<String, DataPart> = HashMap()
                    if (bitmap != null) {
                        params[ServiceConstant.FILE_LIST] = DataPart(
                            filename + ServiceConstant.FILE_EXT_JPEG,
                            GetFileDataFromDrawable(bitmap)
                        )
                    } else {
                        params[ServiceConstant.FILE_LIST] = DataPart(
                            filename + ServiceConstant.FILE_EXT_PDF,
                            GetPdfBytesData(context, uri)
                        )
                    }
                    return params
                }
            }

        //adding the request to volley
        VolleyRequest.getInstance(context)?.addToRequestQueue(zeemartMultipartAPIRequest)
    }

    // api id - 40.1 Upload File Multipart
    @JvmStatic
    fun UploadFileMultipart(
        context: Context,
        bitmap: Bitmap?,
        uri: Uri?,
        componentType: String,
        filename: String,
        callback: MultiPartImageUploaded
    ) {
        //our custom volley request
        val headers: MutableMap<String, String?> = HashMap()
        headers[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = SharedPref.defaultOutlet?.outletId
        val zeemartMultipartAPIRequest: ZeemartMultipartAPIRequest =
            object : ZeemartMultipartAPIRequest(context,
                Method.POST,
                ServiceConstant.ENDPOINT_UPLOAD_FILES_MULTIPART,
                Response.Listener { response ->
                    try {
                        val obj = JSONObject(String(response.data))
                        val imageUploadResponse = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            obj.toString(),
                            MultiPartImageUploadResponse::class.java
                        )
                        callback.result(imageUploadResponse)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        callback.result(null)
                    }
                },
                Response.ErrorListener { callback.result(null) },
                headers
            ) {
                /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["componentType"] = componentType
                    return params
                }

                /*
             * Here we are passing image by renaming it with a unique name
             * */
                override fun getByteData(): Map<String, DataPart> {
                    val params: MutableMap<String, DataPart> = HashMap()
                    if (bitmap != null) {
                        params[ServiceConstant.MULTIPART_FILE] = DataPart(
                            filename + ServiceConstant.FILE_EXT_JPEG,
                            GetFileDataFromDrawable(bitmap),
                            ServiceConstant.FILE_TYPE_IMAGE_JPEG
                        )
                    } else {
                        params[ServiceConstant.MULTIPART_FILE] = DataPart(
                            filename + ServiceConstant.FILE_EXT_PDF,
                            GetPdfBytesData(context, uri),
                            ServiceConstant.FILE_TYPE_APPLICATION_PDF
                        )
                    }
                    return params
                }
            }

        //adding the request to volley
        VolleyRequest.getInstance(context)?.addToRequestQueue(zeemartMultipartAPIRequest)
    }

    // api id - 40.1 Upload File Multipart
    @JvmStatic
    fun uploadMultipleFileMultipart(
        context: Context,
        request: List<MultipleFileUploadRequest>,
        callback: MultiPartImageUploaded
    ) {
        //our custom volley request
        val headers: MutableMap<String, String?> = HashMap()
        headers[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = SharedPref.defaultOutlet?.outletId
        val zeemartMultipartAPIRequest: ZeemartMultipartAPIRequestForMultipleFiles =
            object : ZeemartMultipartAPIRequestForMultipleFiles(context,
                Method.POST,
                ServiceConstant.ENDPOINT_UPLOAD_FILES_MULTIPART,
                Response.Listener { response ->
                    try {
                        val obj = JSONObject(String(response.data))
                        val imageUploadResponse = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            obj.toString(),
                            MultiPartImageUploadResponse::class.java
                        )
                        callback.result(imageUploadResponse)
                    } catch (e: JSONException) {
                        Log.e("error", "onErrorResponse: " + e.message)
                        e.printStackTrace()
                        callback.result(null)
                    }
                },
                Response.ErrorListener { error ->
                    Log.e("error", "onErrorResponse: " + error.message)
                    callback.result(null)
                },
                headers
            ) {
                /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["componentType"] = "INVOICE"
                    return params
                }

                override fun getByteData(): Map<String, ArrayList<DataPart>> {
                    val imageList: MutableMap<String, ArrayList<DataPart>> = HashMap()
                    val dataPart = ArrayList<DataPart>()
                    for (i in request.indices) {
                        var dp: DataPart
                        if (request[i].bitmap != null) {
                            dp = DataPart(
                                request[i].fileName + ServiceConstant.FILE_EXT_JPEG,
                                GetFileDataFromDrawable(
                                    request[i].bitmap
                                ),
                                ServiceConstant.FILE_TYPE_IMAGE_JPEG
                            )
                        } else {
                            dp = DataPart(
                                request[i].fileName + ServiceConstant.FILE_EXT_PDF,
                                GetPdfBytesData(context, request[i].uri),
                                ServiceConstant.FILE_TYPE_APPLICATION_PDF
                            )
                        }
                        dataPart.add(dp)
                    }
                    imageList[ServiceConstant.MULTIPART_FILE] = dataPart
                    return imageList
                }
            }
        //adding the request to volley
        VolleyRequest.getInstance(context)?.addToRequestQueue(zeemartMultipartAPIRequest)
    }

    interface ImageUploaded {
        fun result(imageUploads: ImageUpload?)
    }

    interface MultiPartImageUploaded {
        fun result(imageUploads: MultiPartImageUploadResponse?)
    }
}