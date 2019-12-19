package com.example.youngster_bmi_app.centile

data class Standard (
    val gender: Gender,
    val type: Type,
    val age: Int,
    val centiles: List<Centile>
)