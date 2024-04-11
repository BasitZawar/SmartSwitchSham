package com.example.ss_new.subscription

import com.android.billingclient.api.BillingClient

sealed class Subscriptions(val key: String, var isSubscribe: Boolean = false, val type: String) {
     object Monthly : Subscriptions("monthly_id", type = BillingClient.ProductType.SUBS)
    object SixMonth: Subscriptions("6month_id", type = BillingClient.ProductType.SUBS)
     object Annual : Subscriptions("yearly_id", type = BillingClient.ProductType.SUBS)
//     object LifeTime : Subscriptions("smart_p", type = BillingClient.ProductType.INAPP)
}