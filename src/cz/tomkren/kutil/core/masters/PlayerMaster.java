package cz.tomkren.kutil.core.masters;

import cz.tomkren.kutil.core.Cmd;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.core.Player;

import java.util.List;

/** Created by tom on 29.7.2015. */

public class PlayerMaster {

    private Player actualPlayer;

    public PlayerMaster(Kutil kutil) {

        actualPlayer = null;

        kutil.registerFullCmd(Cmd.player, this::playerCmd);
    }

    public void setActualPlayer(Player player) {
        actualPlayer = player;
    }

    public String playerCmd(List<String> args) {
        if (actualPlayer != null) {
            return actualPlayer.apply(args);
        } else {
            return null;
        }
    }



}
