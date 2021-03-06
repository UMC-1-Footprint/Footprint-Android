package com.footprint.footprint.data.remote.badge

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface BadgeRetrofitInterface {

    /*뱃지 조회 API*/
    @GET("users/badges")
    fun getBadges(): Call<GetBadgeResponse>

    /*대표 뱃지 수정 API*/
    @PATCH("users/badges/title/{badgeIdx}")
    fun changeRepresentativeBadge(@Path("badgeIdx") badgeIdx: Int): Call<ChangeRepresentativeBadgeResponse>

    /*이달의 뱃지 조회 API*/
    @GET("users/badges/status")
    fun getMonthBadge(): Call<MonthBadgeResponse>
}