package io.taptalk.TapTalk.API.Service;

import io.taptalk.TapTalk.Model.ResponseModel.TAPBaseResponse;
import io.taptalk.TapTalk.Model.ResponseModel.TAPGetUserResponse;
import io.taptalk.TapTalk.Model.ResponseModel.TAPUpdateRoomResponse;
import io.taptalk.TapTalk.Model.ResponseModel.TAPUploadFileResponse;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface TAPTalkMultipartApiService {

    @POST("chat/file/upload")
    Observable<TAPBaseResponse<TAPUploadFileResponse>> uploadFile(@Body RequestBody uploadFile);

    @POST("client/user/photo/upload")
    Observable<TAPBaseResponse<TAPGetUserResponse>> uploadProfilePicture(@Body RequestBody uploadProfilePicture);

    @POST("client/room/photo/upload")
    Observable<TAPBaseResponse<TAPUpdateRoomResponse>> uploadRoomPicture(@Body RequestBody uploadGroupPicture);
}
