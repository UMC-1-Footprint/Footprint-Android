package com.footprint.footprint.ui.walk

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.footprint.footprint.R
import com.footprint.footprint.databinding.ActivityWalkAfterBinding
import com.footprint.footprint.service.S3UploadService
import com.footprint.footprint.ui.BaseActivity
import com.footprint.footprint.ui.adapter.FootprintRVAdapter
import com.footprint.footprint.ui.dialog.ActionDialogFragment
import com.footprint.footprint.ui.dialog.FootprintDialogFragment
import com.footprint.footprint.ui.dialog.MsgDialogFragment
import com.footprint.footprint.ui.dialog.NewBadgeDialogFragment
import com.footprint.footprint.domain.model.*
import com.footprint.footprint.ui.error.ErrorActivity
import com.footprint.footprint.utils.*
import com.footprint.footprint.viewmodel.WalkViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.collections.ArrayList

class WalkAfterActivity :
    BaseActivity<ActivityWalkAfterBinding>(ActivityWalkAfterBinding::inflate) {
    private lateinit var actionDialogFragment: ActionDialogFragment
    private lateinit var footprintDialogFragment: FootprintDialogFragment
    private lateinit var newBadgeDialogFragment: NewBadgeDialogFragment
    private lateinit var footprintRVAdapter: FootprintRVAdapter
    private lateinit var saveWalkEntity: SaveWalkEntity
    private lateinit var networkErrSb: Snackbar
    private lateinit var s3ErrorSb: Snackbar

    private var tempAddFootprintPosition: Int? = null   //발자국을 추가할 때 추가하려고 하는 위치를 임시 저장하는 변수
    private var tempUpdateFootprintPosition: Int? = null    //발자국을 수정할 때 수정하려고 하는 위치를 임시 저장하는 변수
    private var s3ReqCnt: Int = 0
    private var s3ResCnt: Int = 0

    private val acquireBadges: ArrayList<BadgeEntity> = arrayListOf() //산책 저장 후 얻은 뱃지 리스트를 저장하는 전역 변수
    private val walkVm: WalkViewModel by viewModel<WalkViewModel>()

    override fun initAfterBinding() {
        observe()
        setMyClickListener()
        setActionDialog()
        initFootprintDialog()
        initNewBadgeDialog()

        //WalkMpFragment 로부터 전달 받은 walk 데이터
        val walkStr = intent.getStringExtra("walk")
        saveWalkEntity = Gson().fromJson<SaveWalkEntity>(walkStr, SaveWalkEntity::class.java)

        bindWalkData()  //데이터 바인딩

        S3UploadService.setCallback(object : S3UploadService.Callback {
            override fun successWalkImg(img: String) {
                saveWalkEntity.pathImg = img

                if (saveWalkEntity.saveWalkFootprints.isNotEmpty())
                    uploadFootprintPhotos(0, 0)
                else
                    saveWalk()
            }

            override fun failWalkImg() {
                binding.walkAfterLoadingPb.visibility = View.VISIBLE

                s3ErrorSb = Snackbar.make(binding.root, getString(R.string.error_api_fail), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.action_retry)) {
                    uploadWalkImg(File(saveWalkEntity.pathImg))    //산책 이미지를 S3에 저장
                }
                s3ErrorSb.show()
            }

            override fun successFootprintImg(img: String, footprintIdx: Int, imgIdx: Int) {
                saveWalkEntity.saveWalkFootprints[footprintIdx].photos[imgIdx] = img
                s3ResCnt++

                if (s3ReqCnt==s3ResCnt)
                    saveWalk()
            }

            override fun failFootprintImg(footprintIdx: Int, imgIdx: Int) {
                binding.walkAfterLoadingPb.visibility = View.VISIBLE

                s3ErrorSb = Snackbar.make(binding.root, getString(R.string.error_api_fail), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.action_retry)) {
                    uploadWalkImg(File(saveWalkEntity.pathImg))    //산책 이미지를 S3에 저장
                }
                s3ErrorSb.show()
            }
        })
    }

    override fun onStop() {
        super.onStop()

        if (::networkErrSb.isInitialized && networkErrSb.isShown)
            networkErrSb.dismiss()
        else if (::s3ErrorSb.isInitialized && s3ErrorSb.isShown)
            s3ErrorSb.dismiss()
    }

    private fun saveWalk() {
        if (!isNetworkAvailable(applicationContext)) {
            binding.walkAfterLoadingPb.visibility = View.VISIBLE

            networkErrSb = Snackbar.make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE)
            networkErrSb.show()
        } else
            walkVm.saveWalk(saveWalkEntity)
    }

    private fun uploadFootprintPhotos(startFootprintIndex: Int, startPhotoIndex: Int) {
        var havePhotos: Boolean = false //발자국에 저장된 이미지가 있는지 없는지 판단해주는 Boolean 변수

        loop@ for (footprintIndex in startFootprintIndex until saveWalkEntity.saveWalkFootprints.size) {
            for (photoIndex in startPhotoIndex until saveWalkEntity.saveWalkFootprints[footprintIndex].photos.size) {
                havePhotos = true

                if (!isNetworkAvailable(applicationContext)) {
                    networkErrSb = Snackbar.make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.action_retry)) {
                        uploadFootprintPhotos(footprintIndex, photoIndex)
                    }
                    networkErrSb.show()

                    break@loop
                } else {
                    S3UploadService.uploadFootprintImg(applicationContext, File(saveWalkEntity.saveWalkFootprints[footprintIndex].photos[photoIndex]), footprintIndex, photoIndex)
                    s3ReqCnt++
                }
            }
        }

        if (!havePhotos)    //발자국에 어떠한 이미지도 없으면 바로 saveWalk 메서드 호출
            saveWalk()
    }

    override fun onBackPressed() {
        binding.walkAfterSlidingUpPanelLayout.apply {
            if (panelState == SlidingUpPanelLayout.PanelState.EXPANDED || panelState == SlidingUpPanelLayout.PanelState.ANCHORED)   //SlidingUpPanelLayout 이 위로 올라가 있으면 아래로 내리기
                panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            else {    //그렇지 않으면 -> ‘OO번째 산책’ 작성을 취소할까요? 다이얼로그 화면 띄우기
                setWalkDialogBundle("'${saveWalkEntity.walkTitle}' 작성을 취소할까요?", getString(R.string.action_delete))
                actionDialogFragment.show(supportFragmentManager, null)
            }
        }
    }

    private fun setMyClickListener() {
        //취소 텍스트뷰 클릭 리스너 -> ‘OO번째 산책’ 작성을 취소할까요? 다이얼로그 화면 띄우기
        binding.walkAfterCancelTv.setOnClickListener {
            setWalkDialogBundle("'${saveWalkEntity.walkTitle}' 작성을 취소할까요?", getString(R.string.action_delete))
            actionDialogFragment.show(supportFragmentManager, null)
        }

        //저장 텍스트뷰 클릭 리스너 -> ‘OO번째 산책’을 저장할까요? 다이얼로그 화면 띄우기
        binding.walkAfterSaveTv.setOnClickListener {
            setWalkDialogBundle("'${saveWalkEntity.walkTitle}'을 저장할까요?", getString(R.string.action_save))
            actionDialogFragment.show(supportFragmentManager, null)
        }

        //발자국 추가 텍스트뷰 클릭 리스너 -> FootprintDialogFragment 띄우기
        binding.walkAfterPlusTv.setOnClickListener {
            footprintDialogFragment.show(supportFragmentManager, null)
        }
    }

    //WalkDialogFragment 초기화
    private fun setActionDialog() {
        actionDialogFragment = ActionDialogFragment()

        actionDialogFragment.setMyDialogCallback(object : ActionDialogFragment.MyDialogCallback {

            //‘OO번째 산책’ 작성을 취소할까요?
            override fun action1(isAction: Boolean) {
                if (isAction)   //삭제 버튼 누르면 액티비티 종료
                    finish()
            }

            //‘OO번째 산책’을 저장할까요?
            override fun action2(isAction: Boolean) {
                if (isAction && isNetworkAvailable(applicationContext)) {   //저장 버튼 누르면 산책 정보 저장 API 호출
                    binding.walkAfterLoadingPb.visibility = View.VISIBLE    //로딩바 띄우기
                    uploadWalkImg(File(saveWalkEntity.pathImg))    //산책 이미지를 S3에 저장
                } else if (isAction && !isNetworkAvailable(applicationContext)) {    //저장을 눌렀는데 네트워크 연결이 안 돼 있는 경우
                    networkErrSb = Snackbar.make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE)
                    networkErrSb.show()
                }
            }
        })
    }

    private fun uploadWalkImg(file: File) {
        if (!isNetworkAvailable(applicationContext)) {
            binding.walkAfterLoadingPb.visibility = View.VISIBLE

            networkErrSb = Snackbar.make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE)
            networkErrSb.show()
        } else
            S3UploadService.uploadWalkImg(applicationContext, file)  //산책 이미지 저장
    }

    //FootprintDialogFragment 초기화 함수
    private fun initFootprintDialog() {
        footprintDialogFragment = FootprintDialogFragment()

        footprintDialogFragment.setMyDialogCallback(object : FootprintDialogFragment.MyDialogCallback {
            //발자국 추가
            override fun sendFootprint(saveWalkFootprint: SaveWalkFootprintEntity) {
                //발자국을 남겼어요 메세지 다이얼로그 띄우기
                val bundle: Bundle = Bundle()
                bundle.putString("msg", getString(R.string.msg_leave_footprint))
                val msgDialogFragment: MsgDialogFragment = MsgDialogFragment()
                msgDialogFragment.arguments = bundle
                msgDialogFragment.show(supportFragmentManager, null)

                saveWalkFootprint.onWalk = 0  //얘는 산책 중에 남긴 게 아니니까 0으로 변경

                if (tempAddFootprintPosition==null) {   //첫번째 발자국을 남긴 경우: 산책 기록이 없어요! -> 발자국 RV
                   footprintRVAdapter.addData(saveWalkFootprint, 0)   //어댑터에 데이터 추가하여 UI 업데이트
                    binding.walkAfterPostRv.visibility = View.VISIBLE
                    binding.walkAfterPlusLineView.visibility = View.INVISIBLE
                    binding.walkAfterPlusTv.visibility = View.INVISIBLE
                } else
                    footprintRVAdapter.addData(saveWalkFootprint, tempAddFootprintPosition!!)   //어댑터에 데이터 추가하여 UI 업데이트
                binding.walkAfterRecordTv.text = saveWalkEntity.saveWalkFootprints.size.toString()    //기록 수를 보여주는 텍스트뷰도 증가
            }

            //발자국 수정
            override fun sendUpdatedFootprint(saveWalkFootprint: SaveWalkFootprintEntity) {
                //발자국을 수정했어요 메세지 다이얼로그 띄우기
                val bundle: Bundle = Bundle()
                bundle.putString("msg", getString(R.string.msg_update_footprint))
                val msgDialogFragment: MsgDialogFragment = MsgDialogFragment()
                msgDialogFragment.arguments = bundle
                msgDialogFragment.show(supportFragmentManager, null)

                footprintRVAdapter.updateDataVerAfter(saveWalkFootprint, tempUpdateFootprintPosition!!) //수정된 발자국으로 어댑터 UI 업데이트

                footprintDialogFragment.dismiss()
                initFootprintDialog()   //발자국 남기기 다이얼로그 프래그먼트 초기화
            }

            override fun cancel() {
                initFootprintDialog()   //발자국 남기기 다이얼로그 프래그먼트 초기화
            }
        })
    }

    //산책 정보를 바인딩하는 함수
    private fun bindWalkData() {
        binding.walkAfterTitleTv.text = saveWalkEntity.walkTitle   //산책 이름
        Glide.with(this).load(saveWalkEntity.pathImg).into(binding.walkAfterMapIv)    //산책 동선 이미지
        binding.walkAfterWalkTimeTv.text = saveWalkEntity.walkTime    //산책 시간
        binding.walkAfterCalorieTv.text = saveWalkEntity.calorie.toString()   //칼로리
        binding.walkAfterDistanceTv.text = saveWalkEntity.distance.toString() //산책 거리
        binding.walkAfterRecordTv.text = saveWalkEntity.saveWalkFootprints.size.toString()    //발자국 수
        binding.walkAfterTimeDescTv.text = "${saveWalkEntity.startAt.split(" ")[0].replace("-", ".")} ${saveWalkEntity.startAt.split(" ")[1].substring(0, 5)}~${saveWalkEntity.endAt.split(" ")[1].substring(0, 5)}"  //산책 날짜

        binding.walkAfterSlidingUpPanelLayout.panelHeight = (getDeviceHeight() - convertDpToPx(this, 90) - (getDeviceHeight() * 0.42)).toInt()  //SlidingPanelLayout 높이 설정

        if (saveWalkEntity.saveWalkFootprints.isEmpty()) {  //산책 도중 기록을 남기지 않았을 때 -> 산책 기록이 없어요!
            binding.walkAfterPostRv.visibility = View.INVISIBLE
            binding.walkAfterPlusLineView.visibility = View.VISIBLE
            binding.walkAfterPlusTv.visibility = View.VISIBLE
        } else {    //산책 도중 기록을 남겼을 때 -> 발자국 정보들을 보여주는 SlidingPanelLayout
            binding.walkAfterPostRv.visibility = View.VISIBLE
            binding.walkAfterPlusLineView.visibility = View.INVISIBLE
            binding.walkAfterPlusTv.visibility = View.INVISIBLE
        }

        initAdapter()   //어댑터 초기화
    }

    //기록 관련 리사이클러뷰 초기화
    private fun initAdapter() {
        footprintRVAdapter = FootprintRVAdapter()
        footprintRVAdapter.setMyItemClickListener(object : FootprintRVAdapter.MyItemClickListener {
            //발자국 추가 텍스트뷰 클릭 리스너
            override fun addFootprint(position: Int) {
                if (saveWalkEntity.saveWalkFootprints.size>=9) {  //현재 발자국 개수가 9개인 경우 -> "발자국은 최대 9개까지 남길 수 있어요" 메세지 다이얼로그 프래그먼트 띄우기
                    val msgDialogFragment: MsgDialogFragment = MsgDialogFragment()
                    val bundle: Bundle = Bundle()
                    bundle.putString("msg", getString(R.string.error_post_cnt_exceed))
                    msgDialogFragment.arguments = bundle
                    msgDialogFragment.show(supportFragmentManager, null)
                } else {    //FootprintDialogFragment 띄우기
                    tempAddFootprintPosition = position + 1 //발자국 위치를 추가할 position 데이터 저장
                    footprintDialogFragment.show(supportFragmentManager, null)
                }
            }

            //발자국 편집 텍스트뷰 클릭 리스너
            override fun updateFootprintVerAfter(position: Int, saveWalkFootprint: SaveWalkFootprintEntity) {
                tempUpdateFootprintPosition = position  //수정하고자 하는 발자국 데이터의 위치 저장

                //수정할 발자국 데이터와 함께 FootprintDialogFragment 띄우기
                val bundle: Bundle = Bundle()
                bundle.putString("footprint", Gson().toJson(saveWalkFootprint))
                footprintDialogFragment.arguments = bundle
                footprintDialogFragment.show(supportFragmentManager, null)
            }

            override fun updateFootprintVerDetail(position: Int, saveWalkFootprint: GetFootprintEntity) {
            }
        })

        //어댑터에 데이터 저장
        footprintRVAdapter.setDataAfterVer(saveWalkEntity.saveWalkFootprints)
        binding.walkAfterPostRv.adapter = footprintRVAdapter
    }

    //WalkDialogFragment 에 넘겨준 메세지(ex.‘OO번째 산책’을 저장할까요?)를 저장하는 함수
    private fun setWalkDialogBundle(msg: String, action: String) {
        val bundle: Bundle = Bundle()
        bundle.putString("msg", msg)
        bundle.putString("action", action)

        actionDialogFragment.arguments = bundle
    }

    private fun initNewBadgeDialog() {
        newBadgeDialogFragment = NewBadgeDialogFragment()

        newBadgeDialogFragment.setMyDialogCallback(object : NewBadgeDialogFragment.MyDialogCallback {
            override fun confirm() {
                if (acquireBadges.isEmpty())    //모든 뱃지를 보여주면 액티비티 종료
                    finish()
                else    //다음 뱃지 보여주기
                    showNewBadgeDialog(acquireBadges.removeAt(0))
            }

        })
    }

    //NewBadgeDialog 에 얻은 뱃지를 저장해서 다이얼로그에 저장
    private fun showNewBadgeDialog(badge: BadgeEntity) {
        val bundle = Bundle()
        bundle.putString("badge", Gson().toJson(badge))
        newBadgeDialogFragment.arguments = bundle
        newBadgeDialogFragment.show(supportFragmentManager, null)
    }

    private fun observe() {
        walkVm.mutableErrorType.observe(this, Observer {
            binding.walkAfterLoadingPb.visibility = View.INVISIBLE

            when (it) {
                ErrorType.NETWORK -> {
                    networkErrSb = Snackbar.make(binding.root, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE)
                    networkErrSb.show()
                }
                ErrorType.UNKNOWN, ErrorType.DB_SERVER -> {
                    startErrorActivity("WalkAfterActivity")
                }
            }
        })

        walkVm.badges.observe(this, Observer {
            binding.walkAfterLoadingPb.visibility = View.INVISIBLE

            if (it.isEmpty()) {  //얻은 뱃지가 없을 때
                showToast(getString(R.string.msg_save_walk_success))
                finish()
            } else {  //얻은 뱃지가 있을 때
                acquireBadges.addAll(it) //전역 변수에 얻은 뱃지 리스트를 저장
                showNewBadgeDialog(acquireBadges.removeAt(0))   //NewBadgeDialog 띄우기
            }
        })
    }
}