package com.footprint.footprint.data.remote.achieve

import com.footprint.footprint.ui.main.home.HomeDayView
import com.footprint.footprint.ui.main.home.HomeMonthView
import retrofit2.*
import com.footprint.footprint.ui.main.home.HomeView
import com.footprint.footprint.ui.main.mypage.MyPageView
import com.footprint.footprint.utils.GlobalApplication.Companion.retrofit
import com.footprint.footprint.utils.LogUtils


object AchieveService {
    private val achieveService = retrofit.create(AchieveRetrofitInterface::class.java)
    private lateinit var homeView: HomeView

    fun setHomeView(homeView: HomeView){
        this.homeView = homeView
    }

    /*일별 정보 API*/
    fun getToday(homeDayView: HomeDayView){
        achieveService.getToday().enqueue(object : Callback<TodayResponse>{
            override fun onResponse(call: Call<TodayResponse>, response: Response<TodayResponse>) {
                val body = response.body()

                LogUtils.d("TODAY/API-SUCCESS", body.toString())
                when(body!!.code){
                    1000 ->{
                        val result = body.result
                        homeDayView.onTodaySuccess(result!!)
                    }
                    else -> homeView.onHomeFailure(body.code, body.message)
                }
            }

            override fun onFailure(call: Call<TodayResponse>, t: Throwable) {
                homeView.onHomeFailure(213, t.message.toString())
                LogUtils.d("TODAY/API-FAILURE", t.message.toString())
            }
        })
    }


    /*월별 정보 API*/
    fun getTMonth(homeMonthView: HomeMonthView){
        achieveService.getTMonth().enqueue(object : Callback<TMonthResponse>{
            override fun onResponse(
                call: Call<TMonthResponse>,
                response: Response<TMonthResponse>
            ) {
                val body = response.body()

                LogUtils.d("TMONTH/API-SUCCESS", body.toString())
                when(body!!.code){
                    1000 ->{
                        val result = body.result
                        homeMonthView.onTMonthSuccess(result!!)
                    }
                    else -> homeView.onHomeFailure(body.code, body.message)
                }
            }

            override fun onFailure(call: Call<TMonthResponse>, t: Throwable) {
                LogUtils.d("TMONTH/API-FAILURE", t.message.toString())
                homeView.onHomeFailure(213, t.message.toString())
            }

        })
    }

    /*마이페이지 정보(통계) API*/
    fun getInfoDetail(myPageView: MyPageView) {
        val infoService = retrofit.create(AchieveRetrofitInterface::class.java)

        myPageView.onMyPageLoading()

        infoService.getInfoDetail().enqueue(object : Callback<AchieveDetailResponse>{
            override fun onResponse(
                call: Call<AchieveDetailResponse>,
                response: Response<AchieveDetailResponse>
            ) {
                val resp = response.body()!!

                when(resp.code) {
                    1000 -> myPageView.onMyPageSuccess(resp.result)
                    else -> myPageView.onMyPageFailure(resp.code, resp.message)
                }
            }

            override fun onFailure(call: Call<AchieveDetailResponse>, t: Throwable) {
                myPageView.onMyPageFailure(400, t.message.toString())
            }

        })
    }

}