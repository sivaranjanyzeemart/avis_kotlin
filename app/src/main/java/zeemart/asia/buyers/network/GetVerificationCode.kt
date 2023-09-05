package zeemart.asia.buyers.network

/**
 * Created by ParulBhandari on 11/10/2017.
 */
//public class GetVerificationCode {
//
//    private String zeemartId;
//    private Context context;
//    private GetRequestStatusResponseListener resetPasswordResponseListener;
//
//    public GetVerificationCode(String zeemartId, Context context, GetRequestStatusResponseListener resetPasswordResponseListener) {
//        this.zeemartId = zeemartId;
//        this.context = context;
//        this.resetPasswordResponseListener = resetPasswordResponseListener;
//    }
//
//    /**
//     * @return JSONRequestBody
//     */
//    private String generateRequestBodyJSON() {
//
//        BuyerCredentials.ForgotPassword forgotPassword = new BuyerCredentials.ForgotPassword(zeemartId);
//        return ZeemartBuyerApp.gsonExposeExclusive.toJson(forgotPassword);
//
////        JSONObject requestBody = null;
////        HashMap<String,String> resetPasswordRequestMap = new HashMap<String,String>();
////        resetPasswordRequestMap.put(ServiceConstant.REQUEST_BODY_ZEEMARTID,zeemartId);
////
////        try
////        {
////            requestBody =  new JSONObject(ZeemartBuyerApp.gson.toJson(resetPasswordRequestMap));
////        }
////        catch(JSONException e)
////        {
////           e.printStackTrace();
////        }
////        return requestBody;
//    }
//
//    public void callResetPasswordAPI() {
//        String requestbody = generateRequestBodyJSON();
//        ZeemartAPIRequest resetPasswordRequest = new ZeemartAPIRequest(context, Request.Method.POST, ServiceConstant.ENDPOINT_SENDVC, requestbody, null, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//                resetPasswordResponseListener.onSuccessResponse(response);
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                resetPasswordResponseListener.onErrorResponse((VolleyErrorHelper) error);
//            }
//        });
//
//        VolleyRequest.getInstance(context).addToRequestQueue(resetPasswordRequest);
//    }
//}
