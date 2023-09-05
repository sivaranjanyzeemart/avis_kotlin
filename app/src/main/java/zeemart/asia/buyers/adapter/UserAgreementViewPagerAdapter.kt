package zeemart.asia.buyers.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import zeemart.asia.buyers.R
import zeemart.asia.buyers.UserAgreementAndTermsPolicy.UserAgreementPrivacyPolicyWebFragment
import zeemart.asia.buyers.UserAgreementAndTermsPolicy.UserAgreementTermsOfUseWebFragment

/**
 * Created by RajPrudhvi on 23/06/2020.
 */
class UserAgreementViewPagerAdapter(
    private val context: Context,
    private val json: String,
    fm: FragmentManager?
) : FragmentPagerAdapter(
    fm!!
) {
    override fun getItem(i: Int): Fragment {
        if (i == 0) {
            return UserAgreementTermsOfUseWebFragment.newInstance(json)
        }
        return if (i == 1) {
            UserAgreementPrivacyPolicyWebFragment.newInstance(json)
        }
        else UserAgreementTermsOfUseWebFragment.newInstance(json)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) {
            context.getString(R.string.txt_terms_of_use)
        } else if (position == 1) {
            context.getString(R.string.txt_privacy_policy)
        } else {
            ""
        }
    }
}