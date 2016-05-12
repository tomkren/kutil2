package cz.tomkren.kutil.core;

import cz.tomkren.kutil.kobjects.frame.Frame;

import java.util.List;

/** Created by tom on 29.7.2015 */

public interface Player extends Cmd {

    void changeCam(Frame frameShowingThePlayer);

    String playerCmd(String cmdName, List<String> args);

    default String apply(List<String> args) {
        return playerCmd(args.get(0),args.subList(1,args.size()));
    }

}
