package zeemart.asia.buyers.network



/**
 * Created by ParulBhandari on 12/27/2017.
 */
//public class EditUserProfile
//{
//    private GetRequestStatusResponseListener getUserProfileDataListener;
//    private Context context;
//    private String requestBody;
//
//    public EditUserProfile(Context context, GetRequestStatusResponseListener listener,String requestBody)
//    {
//        this.context = context;
//        this.getUserProfileDataListener = listener;
//        this.requestBody = requestBody;
//    }
//
//    public void editUserProfile()
//    {
//        String userProfileURL = ServiceConstant.ENDPOINT_USER_PROFILE_USERS;
//        ZeemartAPIRequest getUserProfileRequest = new ZeemartAPIRequest(context, Request.Method.PUT, userProfileURL, requestBody, CommonMethods.getHeaderMapBasedOnOutlet(true,""), new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//                getUserProfileDataListener.onSuccessResponse(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                getUserProfileDataListener.onErrorResponse((VolleyErrorHelper)error);
//            }
//        });
//        VolleyRequest.getInstance(context).addToRequestQueue(getUserProfileRequest);
//    }
//}
