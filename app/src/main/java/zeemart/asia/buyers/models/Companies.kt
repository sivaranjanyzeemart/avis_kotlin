package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Companies {
    @SerializedName("companyId")
    @Expose
    var companyId: String? = null

    @SerializedName("companyName")
    @Expose
    var companyName: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("logoURL")
    @Expose
    var logoURL: String? = null

    @SerializedName("verificationStatus")
    @Expose
    var verificationStatus: String? = null

    @SerializedName("settings")
    @Expose
    var settings: CompanySettings? = null

    @SerializedName("outlet")
    @Expose
    lateinit var outlet: List<Outlet>
    override fun toString(): String {
        return "Companies{" +
                "companyId='" + companyId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", name='" + name + '\'' +
                ", logoURL='" + logoURL + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", outlet=" + outlet +
                '}'
    }
}