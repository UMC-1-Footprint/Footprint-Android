package com.footprint.footprint.ui.register

import android.content.Context
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.footprint.footprint.data.model.User
import com.footprint.footprint.databinding.ActivityRegisterBinding
import com.footprint.footprint.ui.BaseActivity
import com.footprint.footprint.utils.KeyboardVisibilityUtils
import com.google.android.material.tabs.TabLayoutMediator

class RegisterActivity : BaseActivity<ActivityRegisterBinding>(ActivityRegisterBinding::inflate) {
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private lateinit var newUser: User
    override fun initAfterBinding() {
        initVP()
        initMarginTop()
        softKeyboard()

        //SignIn Activity -> User 받아오기
        if (intent.hasExtra("user")) {
            newUser = intent.getSerializableExtra("user") as User
            Log.d("REGISTER", newUser.toString())
        }

        //Register Info Fragment -> 입력 다 받았으면
        //1. 버튼 색 변경
        //2. 버튼 누르면 -> 갤럭시 액티비티로
    }


    private fun initMarginTop() {
        //상단바 높이
        val statusbarHeight = getStatusBarHeightDP(this)
        binding.registerTopLayout.setPadding(
            0,
            statusbarHeight - (statusbarHeight / 4),
            0,
            statusbarHeight / 4
        )
        Log.d(
            "STATUSBAR",
            "상단바 높이: ${statusbarHeight} margintop: ${statusbarHeight - (statusbarHeight / 4)} marginBottom: ${statusbarHeight / 4}"
        )
    }

    private fun initVP() {
        //VP & TB 세팅
        val registerVPAdapter = RegisterViewpagerAdapter(this)
        binding.registerVp.adapter = registerVPAdapter
        //binding.registerVp.isUserInputEnabled = false

        TabLayoutMediator(binding.registerTb, binding.registerVp) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()
    }

    //상단바 높이 구하기
    fun getStatusBarHeightDP(context: Context): Int {
        var result = 0
        val resourceId: Int =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimension(resourceId).toInt()
        }
        return result
    }

    //키보드 up
    private fun softKeyboard() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        /*keyboardVisibilityUtils = KeyboardVisibilityUtils(window,
    onShowKeyboard = { keyboardHeight ->
        binding.registerScrollLayout.run {
            smoothScrollTo(scrollX, scrollY + keyboardHeight)
        }
        Log.d("KEYBOARD", "Height = ${keyboardHeight}")
    })*/
    }
}