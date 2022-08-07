package com.codexdroid.sa_assingment.models.locals

import java.io.Serializable


/**
 * Placing Sample Data in constructor to save data in firebase via app
 *
 * */
data class ShopDetails(
    val image_qr_code: String = "https://images.samsung.com/is/image/samsung/assets/ph/support/mobile-devices/how-to-scan-a-qr-code-on-galaxy-device/QR-code.png?",
    val image_url: String = "https://play-lh.googleusercontent.com/7rRPPHvp6V-qCDVjHFkbkMRVljpmTFP-ZJFDMdcP-NQVB4UfWR2_9t71OGfYYdm81qE=w240-h480-rw",
    val last_visit: Long = 1659571278037,
    val latitude: Double = 19.141879,
    val longitude: Double = 72.856463,
    val shop_id: Int  = 1,
    val shop_name: String = "Samsung Agency",
    val shop_items: ArrayList<ShopItem>?,
): Serializable {
    constructor() : this("","",0L,0.0,0.0,0,"",null)
}

data class ShopItem(
    val item_id: Int = 11,
    val item_image: String = "https://images.samsung.com/is/image/samsung/p6pim/in/2108/gallery/in-galaxy-z-fold3-f926-5g-sm-f926bzkdinu-thumb-474118701?\$216_216_PNG\$",
    val item_name: String = "Galaxy Z Fold3 5G",
    val price: Double = 1769.00,
    val retailer_status: Int = 2
) : Serializable {
    constructor() : this(0,"","",0.0,0)
}