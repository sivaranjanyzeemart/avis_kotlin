package zeemart.asia.buyers.helper

class CleverTapHelper { //    public static void pushProfile(Context context, ViewSpecificUser.Data userData) {
    //        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(context);
    //        HashMap<String, Object> profileUpdate = new HashMap<>();
    //        profileUpdate.put("First Name", userData.getFirstName());
    //        profileUpdate.put("Last name", userData.getLastName());
    //        profileUpdate.put("Email", userData.getEmail());
    //        profileUpdate.put("UserType", userData.getRoleGroup());
    //        profileUpdate.put("Market", userData.getMarket());
    //        profileUpdate.put("Phone", userData.getPhone());
    //        if (StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.ANDROID_ID, ""))) {
    //            String m_androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    //            SharedPref.write(SharedPref.ANDROID_ID, m_androidId);
    //            Log.e("android device Id", "pushProfile: " + m_androidId);
    //            profileUpdate.put("DeviceID", m_androidId);
    //        }
    //        profileUpdate.put("selfSignUp", SharedPref.readBool(SharedPref.DISPLAY_USE_FILTER_FOR_INVOICED_POPUP, false));
    //        if (!StringHelper.isStringNullOrEmpty(SharedPref.read(SharedPref.USER_PIN_CODE, "")))
    //            profileUpdate.put("UserPostcode", SharedPref.read(SharedPref.USER_PIN_CODE, ""));
    //        StringBuilder BusinessType = new StringBuilder();
    //        if (userData.getOutletFeatures() != null && userData.getOutletFeatures().getBusinessType() != null) {
    //            for (int i = 0; i < userData.getOutletFeatures().getBusinessType().size(); i++) {
    //                if (i != userData.getOutletFeatures().getBusinessType().size() - 1) {
    //                    BusinessType.append(userData.getOutletFeatures().getBusinessType().get(i).getName()).append(", ");
    //                } else {
    //                    BusinessType.append(userData.getOutletFeatures().getBusinessType().get(i).getName());
    //                }
    //            }
    //        }
    //        StringBuilder cousineType = new StringBuilder();
    //        if (userData.getOutletFeatures() != null && userData.getOutletFeatures().getCuisineType() != null) {
    //            for (int i = 0; i < userData.getOutletFeatures().getCuisineType().size(); i++) {
    //                if (i != userData.getOutletFeatures().getCuisineType().size() - 1) {
    //                    cousineType.append(userData.getOutletFeatures().getCuisineType().get(i).getName()).append(", ");
    //                } else {
    //                    cousineType.append(userData.getOutletFeatures().getCuisineType().get(i).getName());
    //                }
    //            }
    //        }
    //        StringBuilder cousineFeatures = new StringBuilder();
    //        if (userData.getOutletFeatures() != null && userData.getOutletFeatures().getCuisineFeatures() != null) {
    //            for (int i = 0; i < userData.getOutletFeatures().getCuisineFeatures().size(); i++) {
    //                if (i != userData.getOutletFeatures().getCuisineFeatures().size() - 1) {
    //                    cousineFeatures.append(userData.getOutletFeatures().getCuisineFeatures().get(i).getName()).append(", ");
    //                } else {
    //                    cousineFeatures.append(userData.getOutletFeatures().getCuisineFeatures().get(i).getName());
    //                }
    //            }
    //        }
    //        profileUpdate.put("BusinessType", BusinessType);
    //        profileUpdate.put("Cuisine", cousineType);
    //        profileUpdate.put("Feature", cousineFeatures);
    //        cleverTapAPI.pushProfile(profileUpdate);
    //        cleverTapAPI.onUserLogin(profileUpdate);
    //    }
    //
    //    public static void registerFCM(Context context, String token) {
    //        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(context);
    //        cleverTapAPI.pushFcmRegistrationId(token, true);
    //    }
    //
    //    public static void pushEvent(Context context, String action) {
    //        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(context);
    //        cleverTapAPI.pushEvent(action);
    //    }
    //
    //    public static void pushEvent(Context context, String action, HashMap<String, Object> properties) {
    //        CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(context);
    //        cleverTapAPI.pushEvent(action, properties);
    //    }
}