package zeemart.asia.buyers.network

import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.helper.DialogHelper.displayErrorMessageDialog
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.network.GRNApi.saveGRNResponseListener
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.network.ZeemartAPIRequest
import zeemart.asia.buyers.helper.CommonMethods
import com.android.volley.VolleyError
import zeemart.asia.buyers.network.VolleyRequest
import zeemart.asia.buyers.models.BuyerCredentials
import zeemart.asia.buyers.network.LoginApi.LoginBuyerListener
import zeemart.asia.buyers.ZeemartBuyerApp
import org.json.JSONException
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.network.UsersApi
import zeemart.asia.buyers.network.UsersApi.ViewSpecificUserListener
import zeemart.asia.buyers.models.ViewSpecificUser
import zeemart.asia.buyers.constants.ZeemartAppConstants
import com.onesignal.OneSignal
import zeemart.asia.buyers.network.PushNotificationApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import com.google.gson.JsonSyntaxException
import zeemart.asia.buyers.network.LoginApi.SignUpBuyerListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.BuyerCredentials.UpdatePassword
import zeemart.asia.buyers.models.BuyerCredentials.ResetPassword
import zeemart.asia.buyers.models.BuyerCredentials.ForgotPassword
import zeemart.asia.buyers.models.BuyerCredentials.ValidateCode
import kotlin.Throws
import com.android.volley.AuthFailureError
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.network.ZeemartAPIRequest.DraftErrorListener
import zeemart.asia.buyers.network.InputStreamVolleyRequest
import zeemart.asia.buyers.network.GetPdfFile
import android.content.Intent
import androidx.core.content.FileProvider
import zeemart.asia.buyers.network.OutletsApi.GetSpecificOutletResponseListener
import zeemart.asia.buyers.models.SpecificOutlet
import zeemart.asia.buyers.network.OutletsApi.OutletFuturesResponseListener
import com.android.volley.toolbox.StringRequest
import zeemart.asia.buyers.models.OutletFuturesModel
import zeemart.asia.buyers.models.OrderPayments.PaymentBankAccountDetails
import zeemart.asia.buyers.network.PaymentApi.CompanyCardDetailsListener
import zeemart.asia.buyers.network.PaymentApi.CompanyPreferenceListener
import zeemart.asia.buyers.network.PaymentApi.PayInvoiceListener
import zeemart.asia.buyers.network.PaymentApi.RetrieveAccountDetailsListener
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.network.CancelOrderRequest
import zeemart.asia.buyers.network.OrderHelper
import zeemart.asia.buyers.network.OrderHelper.RepeatOrderResponseListener
import zeemart.asia.buyers.network.OrderHelper.DraftOrderResponseListener
import zeemart.asia.buyers.models.OrderStatus
import zeemart.asia.buyers.network.RetrieveOrders
import zeemart.asia.buyers.network.RetrieveOrders.GetOrderDataListener
import zeemart.asia.buyers.network.OrderHelper.OutletDraftOrdersListener
import zeemart.asia.buyers.network.OrderHelper.ChartsDataListener
import zeemart.asia.buyers.models.ChartsDataModel
import zeemart.asia.buyers.network.OrderHelper.OrderSummaryListener
import zeemart.asia.buyers.network.DeleteOrder
import zeemart.asia.buyers.network.OrderHelper.DealsResponseListener
import zeemart.asia.buyers.network.OrderHelper.SpclDealsResponseListener
import zeemart.asia.buyers.network.OrderHelper.DealsOnBoardingResponseListener
import zeemart.asia.buyers.network.OrderHelper.DealsEssentialsOnBoardingResponseListener
import zeemart.asia.buyers.network.OrderHelper.DealsProductsListener
import zeemart.asia.buyers.network.OrderHelper.ValidAddOnOrderListener
import zeemart.asia.buyers.models.AddOnOrderValidResponse
import zeemart.asia.buyers.network.OrderHelper.ValidAddOnOrderByOrderIdListener
import zeemart.asia.buyers.network.OrderHelper.ValidAddOnOrderByOrderIdsListener
import zeemart.asia.buyers.interfaces.ProductDetailsListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import zeemart.asia.buyers.models.ProductAttributeModel
import zeemart.asia.buyers.models.ProductDetailModel
import zeemart.asia.buyers.network.CompaniesApi.CompaniesResponseListener
import zeemart.asia.buyers.network.CompaniesApi.SpecificCompanyResponseListener
import zeemart.asia.buyers.models.RetrieveCompaniesResponse.SpecificCompany
import zeemart.asia.buyers.interfaces.UnitSizeResponseListener
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseListener
import zeemart.asia.buyers.models.EssentialsBaseResponse
import zeemart.asia.buyers.network.EssentialsApi.EssentialsResponseOnBoardingListener
import zeemart.asia.buyers.network.EssentialsApi.EssentialsProductsResponseListener
import zeemart.asia.buyers.network.EssentialsApi.EssentialsProductsOnBoardingResponseListener
import zeemart.asia.buyers.network.EssentialsApi.EssentialsProductsSearchResponseListener
import zeemart.asia.buyers.network.EssentialsApi.SelfOnBoardingEssentialsProductsSearchResponseListener
import zeemart.asia.buyers.interfaces.SupplierListResponseListener
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel.SupplierPagination
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel.SupplierResponseForDeliveryPreferences
import zeemart.asia.buyers.interfaces.ProductListBySupplierListner
import zeemart.asia.buyers.models.marketlist.ProductListBySupplier
import zeemart.asia.buyers.network.MarketListApi.ProductListListener
import zeemart.asia.buyers.network.MarketListApi.SearchProductListListener
import zeemart.asia.buyers.models.marketlist.Favourite
import zeemart.asia.buyers.network.MarketListApi
import zeemart.asia.buyers.network.UserAgreement.ValidateAgreementResponseListener
import zeemart.asia.buyers.models.userAgreement.Agreements
import zeemart.asia.buyers.models.userAgreement.ValidateAgreements
import zeemart.asia.buyers.network.UserAgreement.AcceptAgreementListener
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import zeemart.asia.buyers.helper.CustomHurlStack
import zeemart.asia.buyers.interfaces.NewOrderResponseListener
import zeemart.asia.buyers.network.GetOrderDetails.GetOrderDetailedDataListener
import zeemart.asia.buyers.helper.ApiParamsHelper.IncludeFields
import zeemart.asia.buyers.network.OrderManagement.CreateDraftOrderBasedOnSKUsListener
import zeemart.asia.buyers.network.OrderManagement.SingleDraftOrderUpdateListener
import zeemart.asia.buyers.network.OrderManagement.PlaceDraftedOrdersAfterReviewListener
import zeemart.asia.buyers.network.OrderManagement.PlaceDraftedOrdersAfterReviewRequestModel
import zeemart.asia.buyers.network.OrderManagement.NewDealOrderResponseListener
import zeemart.asia.buyers.network.OrderManagement.NewEssentialOrderResponseListener
import zeemart.asia.buyers.network.OrderManagement.uploadTransactionrResponseListener
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.network.NotificationsApi.RetrieveNotificationResponseListener
import zeemart.asia.buyers.models.NotificationDetail
import zeemart.asia.buyers.models.NotificationRead
import zeemart.asia.buyers.network.NotificationsApi.RetrieveSpecificAnnouncementResponseListener
import zeemart.asia.buyers.network.NotificationsApi.RetrieveAllAnnouncementsResponseListener
import zeemart.asia.buyers.network.OrderSettingsApi.OrderSettingDeliveryDatesListener
import zeemart.asia.buyers.network.OrderSettingsApi.OrderSettingOutletsListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import zeemart.asia.buyers.constants.DebugConstants
import com.android.volley.toolbox.HttpHeaderParser
import zeemart.asia.buyers.models.BadRequest
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.network.LoginApi
import com.android.volley.RetryPolicy
import com.android.volley.DefaultRetryPolicy
import zeemart.asia.buyers.network.GetOrderHistoryLog.GetOrderHistoryDataListener
import zeemart.asia.buyers.network.ApproveRejectOrders
import zeemart.asia.buyers.models.PushNotification
import com.android.volley.ParseError
import zeemart.asia.buyers.interfaces.OutletSpendingHistoryDataListener
import zeemart.asia.buyers.network.GetOutletSpendingHistoryData

/**
 * Created by ParulBhandari on 12/27/2017.
 */
//public class RetrieveUserProfile {
//
//    public interface Listener {
//        void onSuccessResponse(UserDetails.ViewUser user);
//
//        void onErrorResponse(VolleyErrorHelper error);
//    }
//
//    public static final String USER_ID = "id";
//    private Listener getUserProfileDataListener;
//    private Context context;
//
//    public RetrieveUserProfile(Context context, Listener listener) {
//        this.context = context;
//        this.getUserProfileDataListener = listener;
//    }
//
//    public void getUserProfile(String userId) {
//        String userProfileURL = ServiceConstant.ENDPOINT_USER_PROFILE + "?" + USER_ID + "=" + userId;
//        ZeemartAPIRequest getUserProfileRequest = new ZeemartAPIRequest(context, Request.Method.GET, userProfileURL, null, CommonMethods.getHeaderMapBasedOnOutlet(true, ""), new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//                UserDetails.ViewUser userProfileData = ZeemartBuyerApp.gsonExposeExclusive.fromJson(response.toString(), UserDetails.ViewUser.class);
//                getUserProfileDataListener.onSuccessResponse(userProfileData);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                getUserProfileDataListener.onErrorResponse((VolleyErrorHelper) error);
//            }
//        });
//        VolleyRequest.getInstance(context).addToRequestQueue(getUserProfileRequest);
//    }
//}
