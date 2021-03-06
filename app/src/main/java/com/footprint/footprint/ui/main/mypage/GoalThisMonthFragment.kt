package com.footprint.footprint.ui.main.mypage

import android.graphics.Paint
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.footprint.footprint.R
import com.footprint.footprint.data.model.GoalModel
import com.footprint.footprint.data.remote.goal.GoalService
import com.footprint.footprint.databinding.FragmentGoalThisMonthBinding
import com.footprint.footprint.ui.BaseFragment
import com.footprint.footprint.ui.adapter.DayRVAdapter
import com.footprint.footprint.utils.convertDpToPx
import com.footprint.footprint.utils.getDeviceWidth
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class GoalThisMonthFragment :
    BaseFragment<FragmentGoalThisMonthBinding>(FragmentGoalThisMonthBinding::inflate), GoalView {
    private lateinit var dayRVAdapter: DayRVAdapter
    private lateinit var goal: GoalModel

    private val jobs: ArrayList<Job> = arrayListOf()

    override fun initAfterBinding() {
        GoalService.getThisMonthGoal(this)

        setMyClickListener()
        binding.goalThisMonthChangeGoalTv.paintFlags = Paint.UNDERLINE_TEXT_FLAG    //"다음달부터 목표를 변경할래요 >" 텍스트뷰 밑줄 긋기
    }

    override fun onDestroyView() {
        for (job in jobs) {
            job.cancel()
        }

        super.onDestroyView()
    }

    private fun initAdapter() {
        val deviceWidth = getDeviceWidth()
        dayRVAdapter = DayRVAdapter((deviceWidth - convertDpToPx(requireContext(), 88)) / 7)
        dayRVAdapter.setUserGoalDay(goal.dayIdx)   //어댑터에 사용자 목표 요일 데이터 전달
        dayRVAdapter.setEnabled(false)  //아이템뷰 비활성화

        binding.goalThisMonthGoalDayRv.adapter = dayRVAdapter
    }

    private fun bind() {
        //현재 년도, 월 구해서 화면에 보여주기
        val monthList = goal.month!!.split(" ")
        binding.goalThisMonthYearTv.text = monthList[0]
        binding.goalThisMonthMonthTv.text = " ${monthList[1]}"

        //목표 산책 시간
        val hour = goal.userGoalTime.walkGoalTime!! / 60
        val minute = goal.userGoalTime.walkGoalTime!! % 60
        binding.goalThisMonthGoalWalkTimeBtn.text = when {
            hour==0 -> "${minute}분"
            minute==0 -> "${hour}시간"
            else -> "${hour}시간 ${minute}분"
        }

        //산책 시간대
        binding.goalThisMonthGoalWalkSlotBtn.text = when(goal.userGoalTime.walkTimeSlot) {
            1 -> getString(R.string.title_early_morning)
            2 -> getString(R.string.title_late_morning)
            3 -> getString(R.string.title_early_afternoon)
            4 -> getString(R.string.title_late_afternoon)
            5 -> getString(R.string.title_night)
            6 -> getString(R.string.title_dawn)
            else -> getString(R.string.title_different_every_time)
        }

        if (goal.goalNextModified!!)
            binding.goalThisMonthChangeGoalTv.text = getString(R.string.msg_see_goal_next_month)
        else
            binding.goalThisMonthChangeGoalTv.text = getString(R.string.msg_change_goal_next_month)
    }

    private fun setMyClickListener() {
        //뒤로가기 아이콘 이미지뷰 클릭 리스너
        binding.goalThisMonthBackIv.setOnClickListener {
            (requireActivity()).onBackPressed()
        }

        //"다음달부터 목표를 변경할래요 >" 텍스트뷰 클릭 리스너 -> GoalNextMonthUpdateFragment 로 이동
        //"다음달 목표 보기 >" 텍스트뷰 클릭 리스너 -> GoalNextMonthFragment 로 이동
        binding.goalThisMonthChangeGoalTv.setOnClickListener {
            if ((it as TextView).text==getString(R.string.msg_change_goal_next_month)) {
                val nextMonthGoal = goal
                nextMonthGoal.month = getNextMonth()

                val action = GoalThisMonthFragmentDirections.actionGoalThisMonthFragmentToGoalNextMonthUpdateFragment(Gson().toJson(nextMonthGoal))
                findNavController().navigate(action)
            }
            else
                findNavController().navigate(R.id.action_goalThisMonthFragment_to_goalNextMonthFragment)
        }
    }

    private fun getNextMonth(): String {    ////다음달 년도, 월 구하기
        val currentTime = System.currentTimeMillis()
        val yearFormat = SimpleDateFormat("yyyy")
        val monthFormat = SimpleDateFormat("MM")

        return if (monthFormat.format(currentTime) == "12")    //ex) 2022.12월일 때 -> 2023.1월
            "${yearFormat.format(currentTime).toInt() + 1}년 1월"
        else   //ex) 2022.2월일 때 -> 2022.3월
            "${yearFormat.format(currentTime)}년 ${monthFormat.format(currentTime).toInt() + 1}월"
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_retry) {
            GoalService.getThisMonthGoal(this@GoalThisMonthFragment)
        }.show()
    }

    override fun onGetGoalSuccess(goal: GoalModel) {
        if (view!=null) {
            jobs.add(viewLifecycleOwner.lifecycleScope.launch {
                this@GoalThisMonthFragment.goal = goal
            })

            initAdapter()  //어댑터 초기화
            bind()  //유저 데이터 바인딩
        }
    }

    override fun onGoalFail(code: Int?) {
        if (view!=null) {
            jobs.add(viewLifecycleOwner.lifecycleScope.launch {
                when (code) {
                    6000 -> showSnackBar(getString(R.string.error_network))   //네트워크 연결 문제
                    else -> showSnackBar(getString(R.string.error_api_fail))   //그 이외 문제
                }
            })
        }
    }
}