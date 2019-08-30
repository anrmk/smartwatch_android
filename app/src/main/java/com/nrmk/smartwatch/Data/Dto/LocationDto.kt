package com.nrmk.smartwatch.Data.Dto

class LocationDto {
    var provider: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var altitude : Double? = null
    var accuracy : Float? = null
    var speed : Float? = null
    var direction: Double? = null
    var isFromMockProvider : Boolean = false
    var timestamp: Long = System.currentTimeMillis()
}