@file:OptIn(ExperimentalNaverMapApi::class)

package com.example.onboardingtestapplication.View.MapView

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.graphics.PointF
import android.location.Location
import androidx.compose.ui.graphics.Color

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.onboardingtestapplication.Model.CoVidCenter
import com.example.onboardingtestapplication.View.MapView.screen.centerInformationCard
import com.example.onboardingtestapplication.View.MapView.screen.currentPositionButton
import com.example.onboardingtestapplication.View.MapView.screen.customMarker
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.compose.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MapActivity : ComponentActivity() {

    private val mapViewModel : MapViewModel by viewModels()
//    private lateinit var locationSource : FusedLocationSource // by lazy 로 접근시 초기화 human error 방지
    private val locationSource : FusedLocationSource by lazy {
    FusedLocationSource(this@MapActivity, LOCATION_PERMISSION_REQUEST_CODE)
}

    //view 에서는 지양하기 어떻게 viewModel 에 넣어야 할까 대표적인 안티패턴..

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private val requestLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        result ->
        if (!result) { // 권한 거부 시 로직
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocation.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        requestLocation.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        //권한 요청
//        locationSource = FusedLocationSource(this@MapActivity, LOCATION_PERMISSION_REQUEST_CODE)



        setContent {
            val seoul = LatLng(37.532600, 127.024612)
            val cameraPositionState : CameraPositionState = rememberCameraPositionState {
                position = CameraPosition(seoul, 11.0)
            }

            NaverMap(
                cameraPositionState = cameraPositionState,
                locationSource = locationSource,
                modifier = Modifier.fillMaxSize(),
                onMapClick = { point, latLang ->
                    mapViewModel.updateSelectedMarker(null)
                    Log.d("mapView", "screen : $point / mapPos : $latLang")
                }
            ) {

                val context = LocalContext.current

                MapEffect(key1 = context) {
                        naverMap ->
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
                }

                if(mapViewModel.centerList.observeAsState().value != null) {
                    Log.d("mapView", "${mapViewModel.centerList.value?.size}")
                    for(item in mapViewModel.centerList.value!!) {
                        Log.d("mapView", "$item")

                        customMarker(coVidCenter = item) {
                            mapViewModel.updateSelectedMarker(item)
                            Log.d("mapView", "marker clicked id: ${item.id}")
                            val centerLocation = LatLng(item.lat, item.lng)
                            cameraPositionState.move(CameraUpdate.scrollTo(centerLocation))
                            true
                        }
                    }
                }
            }

            currentPositionButton {
                locationSource.activate {
                        location ->
                    Log.d("mapView", "${location}")
                }

                Log.d("mapView","active ${locationSource.isActivated}")
                Log.d("mapView","last ${locationSource.lastLocation}")

                val lat = locationSource.lastLocation?.latitude
                val lng = locationSource.lastLocation?.longitude

                val currentLocation = LatLng(lat!!, lng!!)
                cameraPositionState.move(CameraUpdate.scrollTo(currentLocation))
            } // 지도 관련한건.. view model 에 어떻게 넣어야할지 모르겠다..

            if(mapViewModel.markerSelect.observeAsState().value == true) {
                mapViewModel.selectedCenterData.observeAsState().value?.let {
                    centerInformationCard(it)
                }
            }
        }
    }
}



