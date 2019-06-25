package com.kehui.www.testapp.retrofit;


import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by jwj on 2017/12/19.
 */

public interface APIService {
    //增加体测记录
    @FormUrlEncoded
    @POST("/Api/Pad/{p}")
    public Call<String> api(@Path("p") String p, @Field("data") String json);


}
