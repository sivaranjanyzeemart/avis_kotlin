package zeemart.asia.buyers.helper.camera

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yongchun.library.view.ImageSelectorActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.camera.CameraEdgeDetectionHelper.Corners
import zeemart.asia.buyers.helper.camera.ShakeDetector.OnShakeListener
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.invoices.InvoiceDataManager
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.network.InvoiceHelper
import zeemart.asia.buyers.network.InvoiceHelper.PICK_FROM_FILES
import zeemart.asia.buyers.network.InvoiceHelper.imagePickerDialog

/**
 * Created by ParulBhandari on 7/10/2018.
 * Base camera preview activity for Zeemart app, extend to create Zeemart custom camera screen
 */
abstract class CameraPreviewActivity : AppCompatActivity(), View.OnClickListener {
    private var mCamera: Camera? = null
    private lateinit var mPreview: CameraPreview
    private var txtScanInvoice: TextView? = null
    private var txtInvoiceFrame: TextView? = null
    private var txtHold: TextView? = null
    private lateinit var txtCaptureImageCount: TextView
    private lateinit var btnCancelUpload: ImageButton
    private lateinit var btnSelectFromGallery: ImageButton
    private lateinit var btnCaptureInvoice: ImageButton
    private lateinit var txtReviewImages: TextView
    private lateinit var btnEnableDisableFlash: ImageButton
    private lateinit var preview: FrameLayout
    private var imageDisplayRotation = 0

    //private RectangularBoundView rectangularBoundView;
    private lateinit var imgDisplayCroppedImage: ImageView
    private var noOfMoreFilesToBeAdded = InvoiceDataManager.TOTAL_NO_OF_FILES_TO_BE_UPLOADED_AT_ONCE
    private val busy = false
    private var mSensorManager: SensorManager? = null
    private lateinit var mAccelerometer: Sensor
    private val rotationSensor: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null
    private var captureImageCount = 0
    var previewChangeDetectedCorners: List<Corners> = ArrayList()

    //to stop camera flash blinking when user selects an image from gallery and moves to the Image Preview Screen
    private var isFromGallery = false
    private var outlet: Outlet? = null
    var orientationEventListener: OrientationEventListener? = null
    private var isLandscape = false
    private var isLandscapeInverted = false
    private var isPotrait = false
    private var uploadInvoicesLayout: FrameLayout? = null
    fun setNoOfMoreFilesToBeAdded(noOfMoreFilesToBeAdded: Int) {
        this.noOfMoreFilesToBeAdded = noOfMoreFilesToBeAdded
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_camera_preview_landscape)
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_camera_preview)
        }
        orientationEventListener =
            object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
                override fun onOrientationChanged(orientation: Int) {
                    if (Settings.System.getInt(
                            contentResolver,
                            Settings.System.ACCELEROMETER_ROTATION,
                            0
                        ) == 0
                    ) {
                        if (orientation > 70 && orientation <= 80) {
                            if (!isLandscapeInverted) {
                                imageDisplayRotation =
                                    CameraHelper.setImageOrientationFromSensorDetector(
                                        mCamera,
                                        Surface.ROTATION_270
                                    )
                                isLandscapeInverted = true
                                val deg = -90f
                                rotateButtons(deg)
                            }
                            isLandscape = false
                            isPotrait = false
                        } else if (orientation > 260 && orientation < 280) {
                            if (!isLandscape) {
                                imageDisplayRotation =
                                    CameraHelper.setImageOrientationFromSensorDetector(
                                        mCamera,
                                        Surface.ROTATION_90
                                    )
                                val deg = 90f
                                rotateButtons(deg)
                                isLandscape = true
                            }
                            isLandscapeInverted = false
                            isPotrait = false
                        } else if (orientation == 0 || orientation == 180) {
                            if (!isPotrait) {
                                imageDisplayRotation = CameraHelper.setImageOrientation(
                                    mCamera,
                                    this@CameraPreviewActivity
                                )
                                val deg = 0f
                                rotateButtons(deg)
                                isPotrait = true
                            }
                            isLandscapeInverted = false
                            isLandscape = false
                        }
                    }
                }
            }
        // ShakeDetector initialization
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector!!.setOnShakeListener(object : OnShakeListener {
            override fun onShake() {
                txtHold!!.visibility = View.VISIBLE
                btnCaptureInvoice.isClickable = false
                btnCaptureInvoice.isEnabled = false
                btnCaptureInvoice.setImageResource(R.drawable.take_pic_grey)
            }

            override fun onNoShake() {
                txtHold!!.visibility = View.GONE
                btnCaptureInvoice.isClickable = true
                btnCaptureInvoice.isEnabled = true
                btnCaptureInvoice.setImageResource(R.drawable.take_pic)
            }
        })
        initView()
    }

    fun rotateButtons(deg: Float) {
        btnCaptureInvoice!!.animate().rotation(deg).interpolator =
            AccelerateDecelerateInterpolator()
        btnSelectFromGallery!!.animate().rotation(deg).interpolator =
            AccelerateDecelerateInterpolator()
        btnEnableDisableFlash!!.animate().rotation(deg).interpolator =
            AccelerateDecelerateInterpolator()
        uploadInvoicesLayout!!.animate().rotation(deg).interpolator =
            AccelerateDecelerateInterpolator()
    }

    private fun initView() {
        txtScanInvoice = findViewById(R.id.txt_invoice_header)
        ZeemartBuyerApp.setTypefaceView(
            txtScanInvoice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtInvoiceFrame = findViewById(R.id.txt_invoice_frame)
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceFrame,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtHold = findViewById(R.id.txt_hold_still)
        ZeemartBuyerApp.setTypefaceView(
            txtHold,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtCaptureImageCount = findViewById(R.id.txt_no_of_image_upload)
        ZeemartBuyerApp.setTypefaceView(
            txtCaptureImageCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        btnCancelUpload = findViewById(R.id.btn_cancel_upload)
        btnCancelUpload.setOnClickListener(this)
        btnSelectFromGallery = findViewById(R.id.btn_select_from_gallery)
        btnSelectFromGallery.setOnClickListener(this)
        btnCaptureInvoice = findViewById(R.id.btn_capture_invoice)
        btnCaptureInvoice.setOnClickListener(this)
        uploadInvoicesLayout = findViewById(R.id.lyt_upload_invoices)
        txtReviewImages = findViewById(R.id.txt_review_Images)
        ZeemartBuyerApp.setTypefaceView(
            txtReviewImages,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtReviewImages.setOnClickListener(this)
        btnEnableDisableFlash = findViewById(R.id.btn_enable_disable_flash)
        btnEnableDisableFlash.setOnClickListener(this)
        imgDisplayCroppedImage = findViewById(R.id.displayCroppedImage)
        imgDisplayCroppedImage.setVisibility(View.GONE)
        /* switchAutoCrop = findViewById(R.id.switch_auto_crop);
        switchAutoCrop.setChecked(true);*/
        //rectangularBoundView = findViewById(R.id.bounded_rect);
        outlet = SharedPref.defaultOutlet
        if (captureImageCount > 0) {
            txtReviewImages.setVisibility(View.VISIBLE)
            txtCaptureImageCount.setVisibility(View.VISIBLE)
            //btnReviewImages.setImageBitmap(captureBitmap);
            txtCaptureImageCount.setText(Integer.toString(captureImageCount))
        }
        /*switchAutoCrop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true);
                    rectangularBoundView.setVisibility(View.VISIBLE);
                } else {
                    SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, false);
                    rectangularBoundView.setVisibility(View.GONE);
                }
            }
        });*/
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_camera_preview_landscape)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_camera_preview)
        }
        initView()
        OpenCVInitView()
    }

    private fun OpenCVInitView() {
        //OpenCVLoader.initDebug();
        if (checkPermission()) {
            setUpCam()
        } else {
            requestPermission()
        }
    }

    /**
     * sets up the camera
     */
    private fun setUpCam() {
        if (mCamera == null) {
            mCamera = cameraInstance
        }
        try {
            mCamera!!.setDisplayOrientation(CameraHelper.getCamRotationDegrees(this, mCamera))
        } catch (e: Exception) {
        }
        imageDisplayRotation = CameraHelper.setImageOrientation(mCamera, this)
        /*Camera.PreviewCallback callback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, final Camera camera) {
                edgeDetectionFrameChange(data, camera);
            }
        };*/
        /* if (SharedPref.readBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true))
            mPreview = new CameraPreview(this, mCamera, callback);
        else*/
        mPreview = CameraPreview(this, mCamera, object : Camera.PreviewCallback {
            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
                // Dummy implementation or just leave it empty
            }
        })
        preview = findViewById(R.id.camera_preview)
        preview.addView(mPreview)
        if (CameraHelper.hasFlash(mCamera)) {
            btnEnableDisableFlash!!.visibility = View.VISIBLE
            if (SharedPref.readBool(SharedPref.FLASH_INVOICE_UPLOAD, true) == true && !isFromGallery) {
                setFlashMode(Camera.Parameters.FLASH_MODE_TORCH, R.drawable.flash_active)
            } else if (SharedPref.readBool(
                    SharedPref.FLASH_INVOICE_UPLOAD,
                    true
                )!! && isFromGallery
            ) {
                setFlashMode(Camera.Parameters.FLASH_MODE_OFF, R.drawable.flash_inactive)
                isFromGallery = false
            } else {
                setFlashMode(Camera.Parameters.FLASH_MODE_OFF, R.drawable.flash_inactive)
            }
        } else {
            btnEnableDisableFlash!!.visibility = View.INVISIBLE
        }
        /*if (SharedPref.readBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true))
            switchAutoCrop.setChecked(true);
        else
            switchAutoCrop.setChecked(false);*/
    }

    /*  public void edgeDetectionFrameChange(byte[] data, Camera camera) {
        if (busy) {
            return;
        }
        try {
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
            if (width == 0) {
                width = CameraEdgeDetectionHelper.defaultWidth;
            }
            if (height == 0) {
                height = CameraEdgeDetectionHelper.defaultHeight;
            }
            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new android.graphics.Rect(0, 0, width, height), 100, out);
            byte[] bytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            Mat img = new Mat();
            Utils.bitmapToMat(bitmap, img);
            bitmap.recycle();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                Core.rotate(img, img, Core.ROTATE_90_CLOCKWISE);
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<MatOfPoint> contours = CameraEdgeDetectionHelper.findContours(img);

            CameraEdgeDetectionHelper.Corners corners = CameraEdgeDetectionHelper.getCorners(contours, img.size());
            CameraEdgeDetectionHelper.pic = img;
            busy = false;
            if (corners != null && corners.getPoints().size() == 4) {
                //countCornerDetectedTimes = countCornerDetectedTimes + 1;
                previewChangeDetectedCorners.add(corners);
                //draw the largest frame out of every 5 frames detected
                if (previewChangeDetectedCorners.size() == 5) {
                    CameraEdgeDetectionHelper.Corners largestPolyCorner = getLargestPolygonCorner(previewChangeDetectedCorners);
                    rectangularBoundView.onCornersDetected(largestPolyCorner);
                    CameraEdgeDetectionHelper.Corners cropCorners = new CameraEdgeDetectionHelper.Corners(rectangularBoundView.getCorners2Crop(), img.size());
                    CameraEdgeDetectionHelper.corners = cropCorners;
                    //countCornerDetectedTimes = 0;
                    previewChangeDetectedCorners.clear();
                }
            } else {
                rectangularBoundView.onCornersNotDetected();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }*/
    private fun getLargestPolygonCorner(previewChangeDetectedCorners: List<Corners>): Corners? {
        var largestPolyCorners: Corners? = null
        var maxWidthPoly = 0
        var maxHeightPoly = 0
        for (i in previewChangeDetectedCorners.indices) {
            val tl = previewChangeDetectedCorners[i].points[0]
            val tr = previewChangeDetectedCorners[i].points[1]
            val br = previewChangeDetectedCorners[i].points[2]
            val bl = previewChangeDetectedCorners[i].points[3]
            val widthA = Math.sqrt(Math.pow(br.x - bl.x, 2.0) + Math.pow(br.y - bl.y, 2.0))
            val widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2.0) + Math.pow(tr.y - tl.y, 2.0))
            val dw = Math.max(widthA, widthB)
            val maxWidth = dw.toInt()
            val heightA = Math.sqrt(Math.pow(tr.x - br.x, 2.0) + Math.pow(tr.y - br.y, 2.0))
            val heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2.0) + Math.pow(tl.y - bl.y, 2.0))
            val dh = Math.max(heightA, heightB)
            val maxHeight = dh.toInt()
            if (maxWidthPoly < maxWidth && maxHeightPoly < maxHeight) {
                maxWidthPoly = maxWidth
                maxHeightPoly = maxHeight
                largestPolyCorners = previewChangeDetectedCorners[i]
            }
        }
        return largestPolyCorners
    }// Camera is not available (in use or does not exist)
    // returns null if camera is unavailable*/
    /**
     * A safe way to get an instance of the Camera object.
     */
    private val cameraInstance: Camera?
        private get() {
            var c: Camera? = null
            try {
                c = Camera.open()
                val parameters = c.getParameters()
                parameters.pictureFormat = ImageFormat.JPEG
                parameters.jpegQuality = 100
                c.setParameters(parameters)
            } catch (e: Exception) {
                e.printStackTrace()
                // Camera is not available (in use or does not exist)
            }
            return c // returns null if camera is unavailable*/
        }

    private fun checkPermission(): Boolean {
        return if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            // Permission is not granted
            false
        } else true
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if ((grantResults.size > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && !isFinishing) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                setUpCam()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED && !isFinishing
                    ) {
                        showMessageOKCancel(
                            getString(R.string.txt_allow_access_permission),
                            DialogInterface.OnClickListener { dialog, which -> requestPermission() })
                    }
                }
            }
            PICK_FROM_FILES -> {
                val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.type = ServiceConstant.FILE_TYPE_PDF
                chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(chooseFile, PICK_FROM_FILES)
            }
            InvoiceHelper.PICK_FROM_GALLERY -> ImageSelectorActivity.start(
                this,
                noOfMoreFilesToBeAdded,
                1,
                false,
                false,
                false
            )
            else -> Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_cancel_upload -> if (captureImageCount > 0) {
                val builder = AlertDialog.Builder(this@CameraPreviewActivity)
                builder.setPositiveButton(
                    R.string.txt_yes_cancel,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                            finish()
                        }
                    })
                builder.setNegativeButton(
                    R.string.txt_no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                        }
                    })
                val dialog = builder.create()
                dialog.setCancelable(false)
                dialog.setCanceledOnTouchOutside(false)
                dialog.setMessage(getString(R.string.txt_cancel_upload, captureImageCount))
                dialog.show()
                val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                deleteBtn.setTextColor(resources.getColor(R.color.chart_red))
                deleteBtn.isAllCaps = false
                val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                cancelBtn.isAllCaps = false
            } else {
                finish()
            }
            R.id.btn_select_from_gallery -> {
                AnalyticsHelper.logAction(
                    this@CameraPreviewActivity,
                    AnalyticsHelper.TAP_ADD_INVOICE_FROM_GALLERY,
                    outlet!!
                )
                imagePickerDialog(this, noOfMoreFilesToBeAdded)
            }
            R.id.btn_capture_invoice -> {
                AnalyticsHelper.logAction(
                    this@CameraPreviewActivity,
                    AnalyticsHelper.TAP_ADD_INVOICE_TAKE_PHOTO,
                    outlet!!
                )
                try {
                    mCamera!!.takePicture(null, null, mPicture)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.txt_review_Images -> {
                if (CameraHelper.hasFlash(mCamera)) setFlashMode(
                    Camera.Parameters.FLASH_MODE_OFF,
                    R.drawable.flash_inactive
                )
                reviewImagesCallback()
            }
            R.id.btn_enable_disable_flash -> try {
                val flashMode = mCamera!!.parameters.flashMode
                if (!SharedPref.readBool(SharedPref.FLASH_INVOICE_UPLOAD, true)!!) {
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH, R.drawable.flash_active)
                    SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, true)
                } else if (SharedPref.readBool(SharedPref.FLASH_INVOICE_UPLOAD, true) == true) {
                    setFlashMode(Camera.Parameters.FLASH_MODE_OFF, R.drawable.flash_inactive)
                    SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, false)
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            else -> {}
        }
    }

    fun setFlashMode(flashMode: String, flashImageResourceId: Int) {
        val params = mCamera!!.parameters
        if (params.flashMode != flashMode) {
            params.flashMode = flashMode
            mCamera!!.parameters = params
        }
        btnEnableDisableFlash!!.setImageDrawable(resources.getDrawable(flashImageResourceId))
    }

    private val mPicture: PictureCallback = object : PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            orientationEventListener!!.disable()
            /*if (SharedPref.readBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true)) {*/
            /*  if (CameraEdgeDetectionHelper.corners != null) {
                    busy = true;
                    Camera.Size size = camera.getParameters().getPictureSize();
                    Mat mat = new Mat(new Size(new Double(size.width), new Double(size.height)), CvType.CV_8U);
                    mat.put(0, 0, data);
                    List<Point> points = CameraEdgeDetectionHelper.corners.getPoints();
                    Mat croppedMat = CameraEdgeDetectionHelper.cropPicture(CameraEdgeDetectionHelper.pic, points);

                    Bitmap croppedBitmap = Bitmap.createBitmap(croppedMat.width(), croppedMat.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(croppedMat, croppedBitmap);
                    //Bitmap enhancedBitmap = CameraEdgeDetectionHelper.enhancePicture(croppedBitmap);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    onPictureTakenCallback(byteArray);
                    croppedBitmap.recycle();
                    //enhancedBitmap.recycle();
                    busy = false;
                } else {*/onPictureTakenCallback(data)
            /* }
            } else {
                onPictureTakenCallback(data);
            }*/
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FROM_FILES && resultCode == RESULT_OK) {
            isFromGallery = true
            imagePickedFromGallery(data, ZeemartAppConstants.PICK_FOR_FILES)
        }
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            isFromGallery = true
            imagePickedFromGallery(data, ZeemartAppConstants.PICK_FOR_GALLERY)
        }
    }

    override fun onResume() {
        super.onResume()
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager!!.registerListener(
            mShakeDetector,
            mAccelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        orientationEventListener!!.enable()
        OpenCVInitView()
    }

    /**
     * callback method when the picture is clicked from the camera
     *
     * @param data
     */
    abstract fun onPictureTakenCallback(data: ByteArray?)

    /**
     * callback when the user pics image from gallery
     *
     * @param data
     */
    abstract fun imagePickedFromGallery(data: Intent?, pickForFiles: String?)

    /**
     * call back when review image button is clicked
     */
    abstract fun reviewImagesCallback()

    /**
     * get the current degree of rotation that the image is in
     *
     * @return
     */
    fun setReviewImageIcon(bitmap: Bitmap?, size: Int) {
        txtReviewImages!!.visibility = View.VISIBLE
        txtCaptureImageCount!!.visibility = View.VISIBLE
        txtCaptureImageCount!!.text = Integer.toString(size)
        captureImageCount = size
    }

    fun hideReviewImageIcon() {
        txtReviewImages!!.visibility = View.INVISIBLE
        txtCaptureImageCount!!.visibility = View.INVISIBLE
    }

    fun getImageDisplayRotation(): Int {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /*if (switchAutoCrop.isChecked()) {
                switch (imageDisplayRotation) {
                    case 0:
                        imageDisplayRotation = 0;
                        break;
                    case 90:
                        imageDisplayRotation = 90;
                        break;
                    case 180:
                        imageDisplayRotation = 180;
                        break;
                    case 270:
                        imageDisplayRotation = 270;
                        break;
                }
            } else {*/
            when (imageDisplayRotation) {
                0 -> imageDisplayRotation = 0
                90 -> imageDisplayRotation = 90
                180 -> imageDisplayRotation = 0
                270 -> imageDisplayRotation = 270
            }
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            /* if (switchAutoCrop.isChecked()) {
                switch (imageDisplayRotation) {
                    case 0:
                        imageDisplayRotation = 180;
                        break;
                    case 90:
                        imageDisplayRotation = 0;
                        break;
                    case 180:
                        imageDisplayRotation = 0;
                        break;
                    case 270:
                        imageDisplayRotation = 180;
                        break;

                }
            }*/
        }
        return imageDisplayRotation
    }

    override fun onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager!!.unregisterListener(mShakeDetector)
        super.onPause()
        stopCamera()
        orientationEventListener!!.disable()
    }

    private fun stopCamera() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mPreview!!.holder.removeCallback(mPreview)
            mCamera!!.lock()
            mCamera!!.release()
            mCamera = null
        }
    }

    companion object {
        private val PERMISSION_REQUEST_CODE = 200
    }
}