package com.example.ss_new.subscription

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.ss_new.app_utils.AllFilesUtils

class GoogleBillingManager(
    val activity: Context
) :
    PurchasesUpdatedListener {
    var pHistory: MutableList<PurchaseHistoryRecord>? = null
    private var oldPurchases: List<Purchase>? = null
    var onPurchase: ((String) -> Unit)? = null
    var billingReady = false
    var productsList: MutableList<ProductDetails> = arrayListOf()

    var hasSub = false
    var oldSub = false

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .enablePendingPurchases()
        .setListener(this)
        .build()


    fun startConnection(
        onPurchaseFound: (List<Purchase>) -> Unit,
        onPurchaseNotFound: () -> Unit
    ) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                billingReady = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                getOldPurchases(onPurchaseFound, onPurchaseNotFound)

                history()
            }

            override fun onBillingServiceDisconnected() {
                startConnection(onPurchaseFound, onPurchaseNotFound)
            }
        })
    }

    fun getOldPurchases(
        onPurchaseFound: (List<Purchase>) -> Unit,
        onPurchaseNotFound: () -> Unit
    ) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)//or SUBS
                .build()
        ) { billingResult1, list ->
            if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK && list.isNotEmpty()
            ) {
                oldPurchases = list
                onPurchaseFound.invoke(list)

                //here you can pass the user to use the app because he has an active subscription

            } else if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK && list.isEmpty()
            ) {
                //  Log.i("OnPurhase", "onPurchaseCallback: need to load purchases")
                getOldInApp(onPurchaseFound, onPurchaseNotFound)

            }

        }
    }


    fun getOldInApp(
        onPurchaseFound: (List<Purchase>) -> Unit,
        onPurchaseNotFound: () -> Unit
    ) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)//or SUBS
                .build()
        ) { billingResult1, list ->
            if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK && list.isNotEmpty()
            ) {

                Log.i("GoogleBillingManager", "getOldInApp: found ")
                onPurchaseFound.invoke(list)
                //here you can pass the user to use the app because he has an active subscription
            } else if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK && list.isEmpty()
            ) {
                Log.i("GoogleBillingManager", "getOldInApp: not found ")

                onPurchaseNotFound()
            }

        }
    }

    fun history() {
        Log.i(
            "Billing",
            "History : "
        )
        val params =
            QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                .build()


        billingClient.queryPurchaseHistoryAsync(
            params
        ) { p0, p1 ->
            pHistory = p1

            p1?.forEach {
                Log.i(
                    "Billing",
                    "purchase resposnse history ${it.products[0]}: "
                )
            }
        }
    }

    fun querySkuDetails(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val skuList = listOf(
            Subscriptions.Monthly , Subscriptions.SixMonth , Subscriptions.Annual
        )

        val productList = ArrayList<QueryProductDetailsParams.Product>()
        skuList.forEach { sku ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku.key)
                    .setProductType(sku.type)
                    .build()
            )
        }

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->


            Log.i(
                "Billing",
                "purchase resposnse ${billingResult.responseCode}: "
            )


            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                onSuccess()
                productsList = productDetailsList
//                querySkuDetailsInApp()
                productDetailsList.forEach {
                    Log.i("Billing", "querySkuDetails: ${it.productId}")
                    when (it.productId) {
                        "monthly_id" -> {

                            AllFilesUtils.monthly_price = it.subscriptionOfferDetails?.get(0)?.pricingPhases!!.pricingPhaseList[0]?.formattedPrice.toString()
                            Log.d("lolo", "querySkuDetailsMonthly: ${AllFilesUtils.monthly_price}")
                        }
                        "6month_id" -> {
                            AllFilesUtils.six_month_price = it.subscriptionOfferDetails?.get(0)?.pricingPhases!!.pricingPhaseList[0]?.formattedPrice.toString()
                            Log.d("lolo", "querySkuDetails6Month: ${AllFilesUtils.six_month_price}")
                        }
                        "yearly_id" -> {
                            AllFilesUtils.yearly_price = it.subscriptionOfferDetails?.get(0)?.pricingPhases!!.pricingPhaseList[0]?.formattedPrice.toString()
                            Log.d("lolo", "querySkuDetailsAnnual: ${AllFilesUtils.yearly_price}")
                        }
                    }

                }
            } else {
                onError()
            }
        }
    }


//    fun querySkuDetailsInApp() {
//        val skuList = listOf(
//            Subscriptions.LifeTime
//        )
//
//        val productList = ArrayList<QueryProductDetailsParams.Product>()
//        skuList.forEach { sku ->
//            productList.add(
//                QueryProductDetailsParams.Product.newBuilder()
//                    .setProductId(sku.key)
//                    .setProductType(sku.type)
//                    .build()
//            )
//        }
//
//        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
//
//        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
//
//
//            Log.i(
//                "Billing",
//                "purchase InApp  ${billingResult.responseCode}: "
//            )
//
//
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                productDetailsList.forEach {
//                    productsList.add(it)
//                }
//                productDetailsList.forEach {
//                    it.subscriptionOfferDetails?.forEach {
//                        Log.i("Billing", "querySkuDetails: ${it.offerId} ${it.offerToken}")
//
//                    }
//
//                }
//            }
//        }
//    }


    fun launchSubscribeFlow(activity: Activity, sub: Subscriptions) {
        // An activity reference from which the billing flow will be launched.
        val productDetails = productsList.find { it.productId == sub.key }
        val offerToken =
            productDetails?.subscriptionOfferDetails?.first()?.offerToken

        productDetails?.let {
            val productDetailsParamsList =
                listOf(
                    offerToken?.let {
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    } ?: BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )


            val billingFlowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

// Launch the billing flow
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }


    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.i("OnPurhase", "onPurchasesUpdated: ${purchases?.size}")

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            oldPurchases = purchases

            for (purchase in purchases) {


                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    Log.i(
                        "OnPurhase",
                        "onPurchasesUpdated: ${purchase.isAutoRenewing} ${purchase.products.size}  ${purchase.products[0]}"
                    )

                    if (!purchase.isAcknowledged && purchase.isAutoRenewing) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams
                            .newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()


                        billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                            // result here

                            onPurchase?.invoke(purchase.products[0])

                            Log.i(
                                "OnPurhase",
                                "aknowlegde: ${it.responseCode == BillingClient.BillingResponseCode.OK}"
                            )

                        }
                    } else {
                        onPurchase?.invoke(purchase.products[0])
                    }


                } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                    // The user has cancelled their subscription
                    // Update their subscription status in your app
                    Log.i("OnPurhase", "onPurchasesUpdated error: ${billingResult.debugMessage}")
                }
            }
        }

        Log.i("OnPurhase", "onPurchasesUpdated error: ${billingResult.debugMessage}")

    }


}