package cz.tomkren.kutil.kobjects.character.effectors;

import cz.tomkren.kutil.shapes.AnimationShape;
import net.phys2d.raw.Body;

/** Created by user on 7. 8. 2015.*/

public class SimpleLegs implements Legs {

    private Dir walkDir;
    private Body body;
    private AnimationShape shape;

    private LegMovement walk;
    private LegMovement jump;
    private LegMovement stop;

    private int walkLeftStep;
    private int walkRightStep;
    private static final int INIT_FRAME = 2;

    public SimpleLegs(Body body, AnimationShape shape) {
        this.body = body;
        this.shape = shape;
    }

    @Override
    public void addMovement(Action action, LegMovement movement) {
        switch (action) {
            case WALK: walk = movement; break;
            case JUMP: jump = movement; break;
            case STOP: stop = movement; break;
        }
    }

    @Override
    public String move(Action legsAction, Dir dir) {
        switch (legsAction) {
            case WALK: return walk.move(dir);
            case JUMP: return jump.move(dir);
            case STOP: return stop.move(dir);
        }
        return null;
    }

    @Override
    public void stepInform() {
        walk.stepInform();
        jump.stepInform();
    }

    @Override
    public void updateDir(Dir dir) {

        if (walkDir == Dir.LEFT) {
            walkRightStep = INIT_FRAME;
            if (dir == walkDir) {
                walkLeftStep ++;
            } else {
                walkLeftStep = INIT_FRAME;
            }
        } else if (walkDir == Dir.RIGHT) {
            walkLeftStep = INIT_FRAME;
            if (dir == walkDir) {
                walkRightStep ++;
            } else {
                walkRightStep = INIT_FRAME;
            }
        } else if (walkDir == Dir.FRONT) {
            walkLeftStep  = INIT_FRAME;
            walkRightStep = INIT_FRAME;
        }

        walkDir = dir;
        switch (dir) {
            case FRONT: shape.setAnimationFrame("standFront",0); break;
            case LEFT:  shape.setAnimationFrame("walkLeft",walkLeftStep); break;
            case RIGHT: shape.setAnimationFrame("walkRight",walkRightStep); break;
        }
    }

    @Override
    public void setAnimationFrame(String animationName, int frame) {
        shape.setAnimationFrame(animationName, frame);
    }

    @Override
    public Dir getCurrentDir() {return walkDir;}

    @Override
    public Body getBody() {return body;}
}
