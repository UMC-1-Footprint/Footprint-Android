package com.footprint.footprint.data.remote.footprint

import android.util.Log
import com.footprint.footprint.data.remote.walk.BaseResponse
import com.footprint.footprint.ui.walk.WalkDetailView
import com.footprint.footprint.utils.FormDataUtils
import com.footprint.footprint.utils.GlobalApplication
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FootprintService {
    private val footprintService = GlobalApplication.retrofit.create(FootprintRetrofitInterface::class.java)

    //산책별 발자국 리스트 조회
    fun getFootprints(walkDetailView: WalkDetailView, walkIdx: Int) {
        footprintService.getFootprints(walkIdx).enqueue(object: Callback<GetFootprintsResponse> {
            override fun onResponse(
                call: Call<GetFootprintsResponse>,
                response: Response<GetFootprintsResponse>
            ) {
                val res = response.body()
                Log.d("FootprintService","\ngetFootprints-RES\ncode: ${res?.code}\nbody: $res")

                when (val code = res?.code) {
                    1000, 2221 -> walkDetailView.onGetFootprintsSuccess(res?.result)    //2221: 해당 산책에 발자국 데이터가 존재하지 않음.
                    else -> walkDetailView.onWalkDetailFail(code!!, res?.message)
                }
            }

            override fun onFailure(call: Call<GetFootprintsResponse>, t: Throwable) {
                Log.e("FootprintService", "getFootprints-ERROR: ${t.message.toString()}")
                walkDetailView.onWalkDetailFail(5000, t.message.toString())
            }

        })
    }

    //발자국 데이터 수정
    fun updateFootprint(walkDetailView: WalkDetailView, footprintIdx: Int, footprintMap: HashMap<String, Any>, footprintPhoto: List<String>?) {
        walkDetailView.onWalkDetailLoading()

        var partMap: HashMap<String, RequestBody> = HashMap()   //글이나 태그에 대한 수정이 없을 땐 Empty
        var photos: ArrayList<MultipartBody.Part>? = null   //사진에 대한 수정이 없을 땐 null

        if (footprintMap.isNotEmpty())  //글이나 태그에 대한 수정 정보가 있을 때
            partMap = FormDataUtils.getPartMap(footprintMap)

        if (footprintPhoto!=null && footprintPhoto.isNotEmpty()) {  //사진을 추가할 경우 -> 각 자신을 MultipartBody.Part 객체로 변경
            photos = arrayListOf()
            for (photo in footprintPhoto)
                photos!!.add(FormDataUtils.prepareFilePart("photos", photo))
        } else if (footprintPhoto!=null && footprintPhoto.isEmpty()) {  //사진을 모두 삭제할 경우 -> 빈 파일로 MultipartBody 객체를 생성(key 만 보내고 value 는 빈 리스트)
            photos = arrayListOf()

            val attachmentEmpty = RequestBody.create(MediaType.parse("image/jpg"), "")
            photos.add(MultipartBody.Part.createFormData("photos", "", attachmentEmpty))
        }

        //발자국 정보 수정 API 호출
        footprintService.updateFootprint(footprintIdx, partMap, photos).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                val res = response.body()
                Log.d("FootprintService","\nupdateFootprint3-RES\ncode: ${res?.code}\nbody: $res")

                if (res?.code==1000)
                    walkDetailView.onUpdateFootprintSuccess()
                else
                    walkDetailView.onWalkDetailFail(res?.code!!, res?.message)
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                walkDetailView.onWalkDetailFail(5000, t.message.toString())
                Log.e("FootprintService", "updateFootprint3-ERROR: ${t.message.toString()}")
            }

        })
    }
}