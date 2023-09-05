package zeemart.asia.buyers.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJsonList
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.CuisineTypeListAdapter
import zeemart.asia.buyers.adapter.CuisineTypeListAdapter.SelectedCuisineTypeFiltersListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.OutletFuturesModel
import zeemart.asia.buyers.network.OutletsApi
import zeemart.asia.buyers.network.OutletsApi.OutletFuturesResponseListener

/**
 * Created by RajPrudhviMarella on 04/Nov/2020.
 */
class SelectCuisineTypeActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private var txtHeader: TextView? = null
    private var txtSelectOneMoreOption: TextView? = null
    private lateinit var lstCousineType: RecyclerView
    private lateinit var btnDone: TextView
    private var lstSelectedCuisineTypes: MutableList<OutletFuturesModel.CuisineType?>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_cuisine_type)
        val bundle = intent.extras
        if (bundle != null)
            if (bundle.containsKey(ZeemartAppConstants.CUISINE_TYPE_LIST)) {
            lstSelectedCuisineTypes = fromJsonList(
                bundle.getString(ZeemartAppConstants.CUISINE_TYPE_LIST),
                OutletFuturesModel.CuisineType::class.java,
                object : TypeToken<MutableList<OutletFuturesModel.CuisineType?>?>() {}.type
            ) as MutableList<OutletFuturesModel.CuisineType?>?
        }
        backButton = findViewById(R.id.bt_back_btn)
        backButton.setOnClickListener(View.OnClickListener { finish() })
        txtHeader = findViewById(R.id.txt_select_cuisine)
        txtSelectOneMoreOption = findViewById(R.id.txt_select_one_more_cuisine)
        lstCousineType = findViewById(R.id.lst_cuisine_type)
        lstCousineType.setLayoutManager(LinearLayoutManager(this))
        btnDone = findViewById(R.id.txt_cuisine_done)
        btnDone.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@SelectCuisineTypeActivity, CreateCompanyOutlet::class.java)
            intent.putExtra(
                ZeemartAppConstants.CUISINE_TYPE_LIST,
                ZeemartBuyerApp.gsonExposeExclusive.toJson(lstSelectedCuisineTypes)
            )
            setResult(CUISINE_TYPE_RESULT_CODE, intent)
            finish()
        })
        OutletsApi.getOutletFuturesMap(this, object : OutletFuturesResponseListener {
            override fun onSuccessResponse(outletFuturesModel: OutletFuturesModel?) {
                if (outletFuturesModel != null && outletFuturesModel.cuisineType != null)
                    for (i in outletFuturesModel.cuisineType!!.indices) {
                    if (lstSelectedCuisineTypes != null && lstSelectedCuisineTypes!!.size > 0) {
                        for (j in lstSelectedCuisineTypes!!.indices) {
                            if (lstSelectedCuisineTypes!![j]!!.isSelected) {
                                if (lstSelectedCuisineTypes!![j]!!.name == outletFuturesModel.cuisineType!![i].name) {
                                    outletFuturesModel.cuisineType!![i].isSelected = true
                                }
                            }
                        }
                    }
                }
                lstCousineType.setAdapter(
                    CuisineTypeListAdapter(
                        this@SelectCuisineTypeActivity,
                        outletFuturesModel?.cuisineType!!,
                        object : SelectedCuisineTypeFiltersListener {
                            override fun onFilterSelected(cuisineType: OutletFuturesModel.CuisineType?) {
                                if (!lstSelectedCuisineTypes!!.contains(cuisineType)) {
                                    lstSelectedCuisineTypes!!.add(
                                        cuisineType!!
                                    )
                                }
                            }

                            override fun onFilterDeselected(cuisineType: OutletFuturesModel.CuisineType?) {
                                for (k in lstSelectedCuisineTypes!!.indices) {
                                    if (lstSelectedCuisineTypes!![k]!!.id == cuisineType!!.id) {
                                        lstSelectedCuisineTypes!!.remove(lstSelectedCuisineTypes!![k])
                                    }
                                }
                            }
                        })
                )
            }

            override fun onErrorResponse(error: VolleyError?) {}
        })
        setFont()
    }

    private fun setFont() {
        setTypefaceView(txtHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
        setTypefaceView(
            txtSelectOneMoreOption,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(btnDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
    }

    companion object {
        var CUISINE_TYPE_RESULT_CODE = 108
    }
}