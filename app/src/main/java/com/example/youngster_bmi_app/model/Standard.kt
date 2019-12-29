package com.example.youngster_bmi_app.model

data class Standard (
    val gender: Gender,
    val type: Type,
    val age: Int,
    val centiles: List<Centile>
)