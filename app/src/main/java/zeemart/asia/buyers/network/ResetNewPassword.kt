package zeemart.asia.buyers.network

/**
 * Created by ParulBhandari on 11/15/2017.
 */
//public class ResetNewPassword {
//    private String zeemartId;
//    private Context context;
//    private String verificationCode;
//    private String newPassword;
//    private GetRequestStatusResponseListener resetPasswordResponseListener;
//
//    public ResetNewPassword(String zeemartId, String verificationCode, String newPassword, Context context, GetRequestStatusResponseListener resetPasswordResponseListener) {
//        this.zeemartId = zeemartId;
//        this.context = context;
//        this.newPassword = newPassword;
//        this.verificationCode = verificationCode;
//        this.resetPasswordResponseListener = resetPasswordResponseListener;
//    }
//
//    /**
//     * @return JSONRequestBody
//     */
//    private String generateRequestBodyJSON() {
//
//        BuyerCredentials.ResetPassword resetPassword = new BuyerCredentials.ResetPassword(verificationCode, newPassword,zeemartId);
//        return ZeemartBuyerApp.gsonExposeExclusive.toJson(resetPassword);
//    }
//
//    public void callResetPasswordAPI() {
//        String jsonRequestBody = generateRequestBodyJSON();
//
//        final ZeemartAPIRequest resetPasswordRequest = new ZeemartAPIRequest(context, Request.Method.POST, ServiceConstant.ENDPOINT_RESETPASSWORD, jsonRequestBody, null, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                resetPasswordResponseListener.onSuccessResponse(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                resetPasswordResponseListener.onErrorResponse((VolleyErrorHelper) error);
//
//            }
//        });
//
//        VolleyRequest.getInstance(context).addToRequestQueue(resetPasswordRequest);
//    }
//}
