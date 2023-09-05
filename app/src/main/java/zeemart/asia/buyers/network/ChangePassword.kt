package zeemart.asia.buyers.network

/**
 * Created by ParulBhandari on 11/20/2017.
 */
//public class ChangePassword {
//
//    private Context context;
//    private String newPassword;
//    private String oldPassword;
//    private GetRequestStatusResponseListener changePasswordResponseListener;
//
//    public static boolean validPasswordStrength(String pwd) {
//        if (!pwd.matches("^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%&()*+,-./:;<=>?@\\^_`{|}~\"'\\[\\]]).*$")) {
//            return false;//!#$%&()*+,-./:;<=>?@\^_`{|}~"'\\[\\]
//        }
//        return true;
//    }
//
//    public ChangePassword(String oldPassword, String newPassword, Context context, GetRequestStatusResponseListener resetPasswordResponseListener) {
//        this.context = context;
//        this.newPassword = newPassword;
//        this.oldPassword = oldPassword;
//        this.changePasswordResponseListener = resetPasswordResponseListener;
//    }
//
//    /**
//     * @return JSONRequestBody
//     */
//    private String generateRequestBodyJSON() {
//        BuyerCredentials.UpdatePassword updatePassword = new BuyerCredentials.UpdatePassword(oldPassword, newPassword);
//        return ZeemartBuyerApp.gsonExposeExclusive.toJson(updatePassword);
//    }
//
//    public void callResetPasswordAPI() {
//        String jsonRequestBody = generateRequestBodyJSON();
//
//        final ZeemartAPIRequest resetPasswordRequest = new ZeemartAPIRequest(context, Request.Method.POST, ServiceConstant.ENDPOINT_CHANGEPASSWORD, jsonRequestBody, CommonMethods.getHeaderMapBasedOnOutlet(true, ""), new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                changePasswordResponseListener.onSuccessResponse(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                changePasswordResponseListener.onErrorResponse((VolleyErrorHelper) error);
//
//            }
//        });
//
//        VolleyRequest.getInstance(context).addToRequestQueue(resetPasswordRequest);
//    }
//
//}
