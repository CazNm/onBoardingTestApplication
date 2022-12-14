package com.example.onboardingtestapplication.Model

import android.util.Log
import androidx.room.*
import com.example.onboardingtestapplication.Model.dao.CoVidCenterDao
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import retrofit2.await
import javax.inject.Inject

class CoVidCenterRepository @Inject constructor(val dataBase: CoVidCenterDataBase) {
    //repository 에서는 데이터의 출처를 저장하는 말 그대로 데이터의 접근과 API의 결과물을 주는 것.
    // 여기서 데이터를 가공하는 식의 Handle 은 ViewModel 에서 처리 하는게 이상적임.. 지양해야 할 것.

    fun getCenterData() : Flow<List<CoVidCenter>> = flow {
        emit(dataBase.coVidCenterDao().getAll())
    }

    fun  saveCenterData(dataList: List<CoVidCenter>) = CoroutineScope(Dispatchers.IO).launch {
        for(data in dataList)
            dataBase.coVidCenterDao().insertCoVidCenter(data)
    }

    fun removeCenterData() = CoroutineScope(Dispatchers.Default).launch {
        dataBase.coVidCenterDao().deleteAll()
    }

    fun requestCoVidCenterList (pageIndex: Int)  : Flow<List<CoVidCenter>> = flow  {
        val pageSize = 10
        val callGetCoVidCenter = RetrofitObject.coVidCenterApi.getCenterList(pageIndex, pageSize)

        try {
            val response = callGetCoVidCenter.await()
            emit(response.data)
        }
        catch(e : Exception) {
            Log.d("splashViewModel", "response error occur $e")
            emit(emptyList())
        }
        finally {
            Log.d("splashViewModel", "error can not define")
            emit(emptyList())
        }
    } //서버 response 처리하기
}

data class CovidCenterResponse (
    val currentCount : Int,
    val data : List<CoVidCenter>,
    val matchCount : Int,
    val page : Int,
    val perPage : Int,
    val totalCount : Int
    )

@Entity(tableName = "covidcenter")
data class CoVidCenter @JvmOverloads constructor(
    val address : String?,
    @ColumnInfo(name = "center_name") val centerName : String?,
    @ColumnInfo(name = "center_type") val centerType : String?,
    @ColumnInfo(name = "created_at") val createdAt : String?,
    @ColumnInfo(name = "facility_name") val facilityName : String?,
    @PrimaryKey val id : Int,
    val lat : Double,
    val lng : Double,
    val org : String?,
    @ColumnInfo(name =  "phone_number") val phoneNumber : String?,
    val sido : String?,
    val sigungu : String?,
    @ColumnInfo(name = "updated_at") val updatedAt : String?,
    @ColumnInfo(name = "zip_code") val zipCode : Int
    )// this is model


@Database(entities = [CoVidCenter::class], version = 2, exportSchema = false)
abstract class CoVidCenterDataBase : RoomDatabase() {
    abstract fun coVidCenterDao() : CoVidCenterDao
}

//@Database(
//    version = 2,
//    entities = [CoVidCenter::class],
//    exportSchema = false,
//    autoMigrations = [
//        AutoMigration (from = 1, to = 2)
//    ]
//)
//
//abstract class CoVidCenterDataBase : RoomDatabase() {
//    abstract fun coVidCenterDao() : CoVidCenterDao
//}


/*
Sample data
{
  "currentCount": 4,
  "data": [
    {
      "address": "경기도 파주시 와석순환로 415",
      "centerName": "코로나19 경기도 파주시 예방접종센터",
      "centerType": "지역",
      "createdAt": "2021-07-15 00:25:54",
      "facilityName": "운정행복센터",
      "id": 281,
      "lat": "37.7242066",
      "lng": "126.7513156",
      "org": "",
      "phoneNumber": "031-940-5597",
      "sido": "경기도",
      "sigungu": "파주시",
      "updatedAt": "2021-07-16 04:55:59",
      "zipCode": "10894"
    },
    {
      "address": "경기도 남양주시 화도읍 수레로 1259",
      "centerName": "코로나19 경기도 남양주시 예방접종센터",
      "centerType": "지역",
      "createdAt": "2021-07-16 04:55:59",
      "facilityName": "화도체육문화센터",
      "id": 282,
      "lat": "37.6498487",
      "lng": "127.301275",
      "org": "",
      "phoneNumber": "031-590-2598",
      "sido": "경기도",
      "sigungu": "남양주시",
      "updatedAt": "2021-07-16 04:55:59",
      "zipCode": "12178"
    },
    {
      "address": "경기도 시흥시 하중로 226",
      "centerName": "코로나19 경기도 시흥시 예방접종센터",
      "centerType": "지역",
      "createdAt": "2021-07-16 04:55:59",
      "facilityName": "시흥국민체육센터",
      "id": 283,
      "lat": "37.3916531",
      "lng": "126.8053548",
      "org": "",
      "phoneNumber": "031-310-6822",
      "sido": "경기도",
      "sigungu": "시흥시",
      "updatedAt": "2021-07-16 04:55:59",
      "zipCode": "14976"
    },
    {
      "address": "충청남도 당진시 시청1로 30",
      "centerName": "코로나19 충청남도 당진시 예방접종센터",
      "centerType": "지역",
      "createdAt": "2021-07-16 04:56:00",
      "facilityName": "국민체육센터",
      "id": 284,
      "lat": "36.8936392",
      "lng": "126.6443718",
      "org": "",
      "phoneNumber": "041-360-6138",
      "sido": "충청남도",
      "sigungu": "당진시",
      "updatedAt": "2021-07-16 04:56:00",
      "zipCode": "31772"
    }
  ],
  "matchCount": 284,
  "page": 29,
  "perPage": 10,
  "totalCount": 284
}*/