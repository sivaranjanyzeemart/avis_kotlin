package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddCardRequestBody {
    @SerializedName("companyId")
    @Expose
    var companyId: String? = null

    @SerializedName("cardToken")
    @Expose
    var cardToken: String? = null

    //    public Address getAddress() {
    //        return address;
    //    }
    //
    //    public void setAddress(Address address) {
    //        this.address = address;
    //    }
    //    @SerializedName("address")
    //    @Expose
    //    private Address address;
    @SerializedName("cardType")
    @Expose
    var cardType: String? = null

    @SerializedName("cardLast4Digits")
    @Expose
    var cardLast4Digits: String? = null

    @SerializedName("expMonth")
    @Expose
    var expMonth: Int? = null

    @SerializedName("expYear")
    @Expose
    var expYear: Int? = null

    @SerializedName("clientIP")
    @Expose
    var clientIP: String? = null

    @SerializedName("timeTokenGenerated")
    @Expose
    var timeTokenGenerated: Long = 0

    @SerializedName("createdBy")
    @Expose
    var createdBy: UserDetails? = null
}