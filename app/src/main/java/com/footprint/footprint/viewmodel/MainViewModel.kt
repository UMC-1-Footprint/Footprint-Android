package com.footprint.footprint.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.footprint.footprint.data.dto.Result
import com.footprint.footprint.data.dto.MonthBadgeResponse
import com.footprint.footprint.domain.usecase.GetMonthBadgeUseCase
import com.footprint.footprint.utils.ErrorType
import kotlinx.coroutines.launch

class MainViewModel(private val getMonthBadgeUseCase: GetMonthBadgeUseCase): BaseViewModel() {
    private val _thisMonthBadge: MutableLiveData<MonthBadgeResponse> = MutableLiveData()
    val thisMonthBadge: LiveData<MonthBadgeResponse> get() = _thisMonthBadge

    fun getMonthBange(){
        viewModelScope.launch {
            when(val response = getMonthBadgeUseCase.invoke()){
                is Result.Success -> _thisMonthBadge.value = response.value
                is Result.NetworkError -> mutableErrorType.postValue(ErrorType.NETWORK)
                is Result.GenericError -> mutableErrorType.postValue(ErrorType.UNKNOWN)
            }
        }
    }

}