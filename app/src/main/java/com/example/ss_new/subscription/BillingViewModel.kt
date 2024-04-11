package com.example.ss_new.subscription

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ss_new.app_utils.AllFilesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingViewModel(
    private val ctx: Application
) :
    AndroidViewModel(ctx) {
    private val googleBillingManager: GoogleBillingManager = GoogleBillingManager(ctx)
    private val dataStoreManager: DataStoreManager = DataStoreManager(ctx)
    val dataStore get() = dataStoreManager
    val billing get() = googleBillingManager


    init {
        startConnectionWithGooglePay()
        googleBillingManager.onPurchase = {
            viewModelScope.launch(Dispatchers.IO) {
                dataStore.saveSubscription(true)
                billing.hasSub = true
                AllFilesUtils.setSubEnabled(ctx, true)
                Log.d("lolo", ":PurchasedFoundInit ")
            }
        }


    }

    private fun startConnectionWithGooglePay() {
        Log.i("Billing", "startConnectionWithGooglePay: ")

        if (!googleBillingManager.billingReady)
            googleBillingManager.startConnection(onPurchaseFound = {
                it.forEach {
                    Log.i(
                        "Billing",
                        "startConnectionWithGooglePay: ${it.quantity} ${it.purchaseState} ${it.purchaseTime}"
                    )

                    it.products.forEach {
                        Log.i("Billing", "startConnectionWithGooglePay: $it ")
                    }
                }
                viewModelScope.launch(Dispatchers.IO) {
                    dataStore.saveSubscription(true)
                    billing.hasSub = true
                    AllFilesUtils.setSubEnabled(ctx, true)

                    Log.d("lolo", ":PurchasedFound ")
                }
            }, onPurchaseNotFound = {
                viewModelScope.launch(Dispatchers.IO) {
                    dataStore.saveSubscription(false)
                    billing.hasSub = false
                    AllFilesUtils.setSubEnabled(ctx, false)
                    Log.d("lolo", ":PurchasedNotFound ")
                }
                Log.i(
                    "Billing",
                    "purchase Not found: "
                )

                if (googleBillingManager.billingReady)
                    googleBillingManager.querySkuDetails(
                        onSuccess = {
                            Log.i(
                                "Billing",
                                "purchase list: "
                            )
                        },
                        onError = {
                            Log.i(
                                "Billing",
                                "purchase error: "
                            )

                        })


            })
    }

    /*   fun purchase(activity: Activity) {
           googleBillingManager.launchBillingFlow(
               activity, products.first(),
               products.first().subscriptionOfferDetails?.get(0)?.offerToken!!
           )
       }*/
}