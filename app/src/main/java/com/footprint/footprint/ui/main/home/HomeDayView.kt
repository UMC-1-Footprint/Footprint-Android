package com.footprint.footprint.ui.main.home

import com.footprint.footprint.data.remote.achieve.Today

interface HomeDayView {
    /*일별 목표 받아오기*/
    fun onTodaySuccess(today: Today)
}