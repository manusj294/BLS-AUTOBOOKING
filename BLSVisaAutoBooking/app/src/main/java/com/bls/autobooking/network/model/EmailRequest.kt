package com.bls.autobooking.network.model

data class EmailRequest(
    val service_id: String,
    val template_id: String,
    val user_id: String,
    val template_params: Map<String, String>
)