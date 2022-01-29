package com.footprint.footprint.ui.main.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.footprint.footprint.R
import com.footprint.footprint.databinding.FragmentSettingBinding
import com.footprint.footprint.ui.BaseFragment
import com.footprint.footprint.ui.dialog.ActionDialogFragment

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private lateinit var actionDialogFragment: ActionDialogFragment

    override fun initAfterBinding() {
        if (!::actionDialogFragment.isInitialized)
            initActionDialog()

        setMyEventListener()
    }

    private fun initActionDialog() {
        actionDialogFragment = ActionDialogFragment()

    }

    private fun setMyEventListener() {
        //뒤로가기 이미지뷰 클릭 리스너 -> 프래그먼트 종료
        binding.settingBackIv.setOnClickListener {
            requireActivity().onBackPressed()
        }

        //내 정보 수정 다음 이미지뷰 클릭 리스너 -> 내 정보 조회 프래그먼트(MyInfoFragment)로 이동
        binding.settingUpdateMyInfoNextIv.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_myInfoFragment)
        }

        //로그아웃 텍스트뷰 클릭 리스너 -> 로그아웃 관련 ActionDialogFragment 띄우기
        binding.settingLogoutTv.setOnClickListener {
            setActionDialogBundle(getString(R.string.msg_logout))
            actionDialogFragment.show(requireActivity().supportFragmentManager, null)
        }

        //회워탈퇴 텍스트뷰 클릭 리스너 -> 회원탈퇴 관련 ActionDialogFragment 띄우기
        binding.settingWithdrawalTv.setOnClickListener {
            setActionDialogBundle(getString(R.string.msg_withdrawal))
            actionDialogFragment.show(requireActivity().supportFragmentManager, null)
        }
    }

    private fun setActionDialogBundle(msg: String) {
        val bundle: Bundle = Bundle()
        bundle.putString("msg", msg)
        actionDialogFragment.arguments = bundle
    }
}