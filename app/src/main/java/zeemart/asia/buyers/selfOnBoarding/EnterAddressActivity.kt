package zeemart.asia.buyers.selfOnBoarding

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import java.io.IOException
import java.util.*

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class EnterAddressActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private var txtEnterAddress: TextView? = null
    private var txtWillShowDeals: TextView? = null
    private var txtCountry: TextView? = null
    private var txtSingapore: TextView? = null
    private var txtAddress: TextView? = null
    private lateinit var txtTypePostalAddress: TextView
    private lateinit var lytSearchPlace: LinearLayout
    private lateinit var lytDone: RelativeLayout
    private var txtDone: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_address)
        btnBack = findViewById(R.id.bt_back_btn)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        txtEnterAddress = findViewById(R.id.txt_enter_address)
        txtWillShowDeals = findViewById(R.id.txt_will_show_deals)
        txtCountry = findViewById(R.id.txt_country)
        txtSingapore = findViewById(R.id.txt_singapore)
        txtAddress = findViewById(R.id.txt_address)
        txtTypePostalAddress = findViewById(R.id.txt_postal_address)
        lytSearchPlace = findViewById(R.id.lyt_search_places)
        lytSearchPlace.setOnClickListener(View.OnClickListener {
            if (!Places.isInitialized()) {
                Places.initialize(
                    this@EnterAddressActivity,
                    "AIzaSyA0ZnGsJJk1YpfL2g3NOpl7zHkQmr7CjW0"
                )
            }
            // return after the user has made a selection.
            val fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("SG").setTypeFilter(
                TypeFilter.ADDRESS
            ).build(this@EnterAddressActivity)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        })
        lytDone = findViewById(R.id.lyt_done)
        lytDone.setBackground(resources.getDrawable(R.drawable.dark_grey_rounded_corner))
        lytDone.setClickable(false)
        txtDone = findViewById(R.id.txt_done)
        if (!StringHelper.isStringNullOrEmpty(
                SharedPref.read(
                    SharedPref.USER_DELIVERY_ADDRESS,
                    ""
                )
            )
        ) {
            txtTypePostalAddress.setText(SharedPref.read(SharedPref.USER_DELIVERY_ADDRESS, ""))
            txtTypePostalAddress.setTextColor(resources.getColor(R.color.black))
            lytDone.setBackground(resources.getDrawable(R.drawable.green_round_corner))
            lytDone.setClickable(true)
            lytDone.setOnClickListener(View.OnClickListener {
                AnalyticsHelper.logGuestAction(
                    this@EnterAddressActivity,
                    AnalyticsHelper.TAP_GUEST_ENTER_ADDRESS_DONE
                )
                val intent =
                    Intent(this@EnterAddressActivity, BrowseDealAndEssentialsSuppliers::class.java)
                startActivity(intent)
            })
        }
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtEnterAddress, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(txtWillShowDeals, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtCountry, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSingapore, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAddress, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtTypePostalAddress, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            val place = Autocomplete.getPlaceFromIntent(data)
            SharedPref.write(SharedPref.USER_DELIVERY_NAME, place.name)
            SharedPref.write(SharedPref.USER_DELIVERY_ADDRESS, place.address)
            txtTypePostalAddress!!.text = place.address
            txtTypePostalAddress!!.setTextColor(resources.getColor(R.color.black))
            lytDone!!.background = resources.getDrawable(R.drawable.green_round_corner)
            lytDone!!.isClickable = true
            lytDone!!.setOnClickListener {
                val intent =
                    Intent(this@EnterAddressActivity, BrowseDealAndEssentialsSuppliers::class.java)
                startActivity(intent)
            }
            try {
                val addresses: List<Address>
                val geocoder = Geocoder(this@EnterAddressActivity, Locale.getDefault())
                try {
                    addresses = geocoder.getFromLocation(
                        Objects.requireNonNull(place.latLng).latitude,
                        place.latLng.longitude,
                        1
                    )!! // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    val postalCode = addresses[0].postalCode
                    SharedPref.write(SharedPref.USER_PIN_CODE, postalCode)
                    Log.e("AddressPostal: ", "" + postalCode)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //setMarker(latLng);
            }
        }
    }

    companion object {
        var AUTOCOMPLETE_REQUEST_CODE = 201
    }
}