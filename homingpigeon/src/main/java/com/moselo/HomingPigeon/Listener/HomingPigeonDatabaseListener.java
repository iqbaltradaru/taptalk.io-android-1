package com.moselo.HomingPigeon.Listener;

import com.moselo.HomingPigeon.Data.Message.HpMessageEntity;

import java.util.List;

public interface HomingPigeonDatabaseListener {
    void onSelectFinished(List<HpMessageEntity> entities);
}
