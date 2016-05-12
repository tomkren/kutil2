package cz.tomkren.kutil.core.masters;

import cz.tomkren.kutil.core.KObject;

/** Created by tom on 23.7.2015. */

public class ClipboardMaster {

    private KObject clipboard; // KObject ve schránce

    public ClipboardMaster() {
        clipboard = null;
    }


    public KObject getClipboard() {
        return clipboard;
    }

    public void setClipboard(KObject toClipboard) {
        clipboard = toClipboard;
    }


}
