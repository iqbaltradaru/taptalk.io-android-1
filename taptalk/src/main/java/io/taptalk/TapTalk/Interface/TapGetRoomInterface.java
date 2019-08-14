package io.taptalk.TapTalk.Interface;

import io.taptalk.TapTalk.Model.TAPRoomModel;

public interface TapGetRoomInterface {
    void onSuccess(TAPRoomModel roomModel);

    void onError(String errorCode, String errorMessage);
}

