package com.footprint.footprint.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.footprint.footprint.utils.GlobalApplication.Companion.X_ACCESS_TOKEN
import com.footprint.footprint.utils.GlobalApplication.Companion.eSharedPreferences
import com.footprint.footprint.utils.GlobalApplication.Companion.mSharedPreferences
import com.google.gson.reflect.TypeToken

/*Onboarding- true(온보딩 실행 이력 O), false(온보딩 실행 이력 X/첫 접속)*/
fun saveOnboarding(onboardingStatus: Boolean){
    val editor = mSharedPreferences.edit()

    editor.putBoolean("onboarding", onboardingStatus)
    editor.apply()
}

fun getOnboarding() = mSharedPreferences.getBoolean("onboarding", false)


/*Login Status - kakao, google, null*/
fun saveLoginStatus(loginStatus: String){
    val editor = mSharedPreferences.edit()

    editor.putString("loginStatus", loginStatus)
    editor.apply()
}

fun getLoginStatus(): String = mSharedPreferences.getString("loginStatus", "null")!!


fun removeLoginStatus(){
    val editor = mSharedPreferences.edit()

    editor.remove("loginStatus")
    editor.apply()
}

/*JwtId - 로그인 API 호출 후, 서버에서 받아오는 JwtId & API 요청 시 사용하는 X-ACCESS-TOKEN*/
fun saveJwt(jwtToken: String) {
    val editor = eSharedPreferences.edit()
    editor.putString(X_ACCESS_TOKEN, jwtToken)

    editor.apply()
}

fun getJwt(): String? = eSharedPreferences.getString(X_ACCESS_TOKEN, null)

fun removeJwt(){
    val editor = eSharedPreferences.edit()

    editor.remove(X_ACCESS_TOKEN)
    editor.apply()
}

/*Tag: 태그 검색 기록 확인에서 활용*/
fun saveTags(context: Context,tags: ArrayList<String>) {
    val spf = context.getSharedPreferences("tag", AppCompatActivity.MODE_PRIVATE)
    val editor = spf.edit()
    val json = gson.toJson(tags)

    editor.putString("tags", json)
    editor.apply()
}

fun getTags(context: Context): ArrayList<String>? {
    val spf = context.getSharedPreferences("tag", AppCompatActivity.MODE_PRIVATE)
    val json = spf.getString("tags", null)
    val type = object : TypeToken<ArrayList<String>>() {}.type

    return gson.fromJson(json, type)
}

/*Password: 산책 일기 암호*/
fun savePWD(password: String){
    val editor = mSharedPreferences.edit()

    editor.putString("password", password)
    editor.apply()
}

fun getPWD(): String? =  mSharedPreferences.getString("password", null)

fun removePWD(){
    val editor = mSharedPreferences.edit()

    editor.remove("password")
    editor.apply()

    savePWDstatus("DEFAULT")
}

/*PWDstatus: 산책 일기 암호 상태 - DEFAULT(암호 X), ON(암호 ON), OFF(암호 OFF)*/
fun savePWDstatus(status: String){
    val editor = mSharedPreferences.edit()

    editor.putString("pwdStatus", status)
    editor.apply()
}

fun getPWDstatus(): String? =  mSharedPreferences.getString("pwdStatus", "DEFAULT")


//산책 일기 암호가 풀렸는지 - DEFAULT(NOTHING), SUCCESS(암호가 풀림), CANCEL(암호 풀려다 취소 ex.뒤로가기)
fun saveCrackStatus(isCrack: String) {
    val editor = mSharedPreferences.edit()

    editor.putString("crackStatus", isCrack)
    editor.apply()
}

fun getCrackStatus(): String? = mSharedPreferences.getString("crackStatus", "NOTHING")


/*Notification: 앱 푸시 알림 - true(알림 on) false(알림 off)*/
fun saveNotification(status: Boolean){
    val editor = mSharedPreferences.edit()

    editor.putBoolean("notification", status)
    editor.apply()
}

fun getNotification(): Boolean =  mSharedPreferences.getBoolean("notification", false)

fun removeNotification(){
    val editor = mSharedPreferences.edit()

    editor.remove("notification")
    editor.apply()
}

/*초기화: loginStatus, PWD, JWT, Notification 초기화 */
fun reset(){
    removeLoginStatus()
    removePWD()
    removeJwt()
    removeNotification()
}