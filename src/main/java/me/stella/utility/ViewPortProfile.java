package me.stella.utility;

import java.util.UUID;

public class ViewPortProfile {

    private final UUID viewingUID;
    private final Object viewingNBT;

    public UUID getViewingUID() {
        return viewingUID;
    }

    public Object getViewingNBT() {
        return viewingNBT;
    }

    public ViewPortProfile(UUID uid, Object object) {
        this.viewingUID = uid;
        this.viewingNBT = object;
    }

}
