package com.footprint.footprint.ui.error

import android.graphics.Paint
import android.view.View
import com.footprint.footprint.databinding.ActivityErrorBinding
import com.footprint.footprint.ui.BaseActivity
import com.footprint.footprint.ui.main.MainActivity
import com.footprint.footprint.utils.getJwt

class ErrorActivity : BaseActivity<ActivityErrorBinding>(ActivityErrorBinding::inflate) {

    override fun initAfterBinding() {
        // Splash, SignIn, Register -> 홈으로 가기 X
        if(getJwt() == null)
            binding.errorGohomeTv.visibility = View.GONE

        binding.errorGohomeTv.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        // 뒤로가기
        binding.errorBackBtnIv.setOnClickListener{
            finish()
        }

        // 재시도
        binding.errorRetryBtn.setOnClickListener{
            // setResult(this, 9000)
            // finish()
        }

        // 홈으로 가기
        binding.errorGohomeTv.setOnClickListener{
            finishAffinity()
            startNextActivity(MainActivity::class.java)

            // way 2
            /*val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP*/
        }
    }

}