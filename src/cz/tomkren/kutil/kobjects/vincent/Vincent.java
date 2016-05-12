package cz.tomkren.kutil.kobjects.vincent;

import com.google.common.base.Joiner;
import cz.tomkren.kutil.core.KAtts;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.core.Player;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.coin.Coin;
import cz.tomkren.kutil.kobjects.frame.Frame;
import cz.tomkren.kutil.shapes.AnimationShape;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.CollisionEvent;
import net.phys2d.raw.CollisionListener;
import net.phys2d.raw.World;

import java.util.List;

/** Created by tom on 28.7.2015. */


public class Vincent extends KObject implements Player, CollisionListener  {

    public static final float FRICTION = 110.0f;

    public enum WalkDir {LEFT,FRONT,RIGHT}

    //public enum HitDir {UP, DOWN, LEFT, RIGHT}

    private long step;

    private long coins;

    private World parentWorldWithListener;



    public final int jumpCoolDown = 80;
    private boolean canJump;
    private int canJumpCountdown;

    public final int walkCoolDown = 6;
    private boolean canWalk;
    private int canWalkCountdown;


    private AnimationShape animationShape;

    private WalkDir walkDir;

    public Vincent(KAtts kAtts, Kutil kutil) {
        super(kAtts, kutil);
        setType("vincent");
        setIsAffectedByGravity(true);


        kutil.getPlayerMaster().setActualPlayer(this);

        animationShape = (AnimationShape) shape();

        updateWalkDir(WalkDir.FRONT);

        step = 0;

        canJump = true;
        canJumpCountdown = 0;

        canWalk = true;
        canWalkCountdown = 0;


        coins = 0;


    }

    @Override
    public void init() {
        super.init();
        getBody().setFriction(FRICTION);
        resetCollisionListener();
    }

    @Override
    public void setParent(KObject newParent) {
        super.setParent(newParent);
        resetCollisionListener();
    }

    @Override
    public void changeCam(Frame frameShowingThePlayer) {
        Int2D camPos = pos();
        Int2D frameSize = frameShowingThePlayer.getSize();
        frameShowingThePlayer.setCam(new Int2D(camPos.getX() - frameSize.getX() / 2, camPos.getY() - frameSize.getY() / 2));
    }

    @Override
    public void collisionOccured(CollisionEvent event) {

        Body thisBody = getBody();
        Body otherBody = null;
        if (event.getBodyA() == thisBody) {
            otherBody = event.getBodyB();
        } else if(event.getBodyB() == thisBody) {
            otherBody = event.getBodyA();
        }

        if (otherBody == null) return;

        handleCollision( /*Int2D.fromROVector2f(event.getPoint()),*/ (KObject) otherBody.getUserData() );
    }

    private void handleCollision(/*Int2D hitPos,*/ KObject hitObject) {

        if (hitObject instanceof Coin) {
            hitObject.delete();
            coins ++;
        }

    }

    private void resetCollisionListener() {

        // remove from the previous parent world if it exists
        if (parentWorldWithListener != null) {
            parentWorldWithListener.removeListener(this);
        }

        parentWorldWithListener = getParentWorld();

        if (parentWorldWithListener != null) {
            parentWorldWithListener.addListener(this);
        }

    }


    private int walkLeftStep;
    private int walkRightStep;

    private static final int initFrame = 2;

    private void updateWalkDir(WalkDir dir) {

        if (walkDir == WalkDir.LEFT) {
            walkRightStep = initFrame;
            if (dir == walkDir) {
                walkLeftStep ++;
            } else {
                walkLeftStep = initFrame;
            }
        } else if (walkDir == WalkDir.RIGHT) {
            walkLeftStep = initFrame;
            if (dir == walkDir) {
                walkRightStep ++;
            } else {
                walkRightStep = initFrame;
            }
        } else if (walkDir == WalkDir.FRONT) {
            walkLeftStep  = initFrame;
            walkRightStep = initFrame;
        }

        walkDir = dir;
        switch (dir) {
            case FRONT: animationShape.setAnimationFrame("standFront",0); break;
            case LEFT:  animationShape.setAnimationFrame("walkLeft",walkLeftStep); break;
            case RIGHT: animationShape.setAnimationFrame("walkRight",walkRightStep); break;
        }
    }



    @Override
    public void step() {
        super.step();
        step++;

        if (!canJump) {
            canJumpCountdown--;
            if (canJumpCountdown <= 0) {
                canJump = true;
            }
        }

        if (!canWalk) {
            canWalkCountdown--;
            if (canWalkCountdown <= 0) {
                canWalk = true;
            }
        }
    }

    @Override
    public String playerCmd(String cmdName, List<String> args) {

        if (!simulationMaster().isSimulationRunning()) {return null;}

        switch (cmdName) {
            case "go"    : go(args.get(0)); return null;
            case "jump"  : return jump(args.get(0));
            case "front" : updateWalkDir(WalkDir.FRONT); return null;
        }

        //getBody().adjustVelocity(new Vector2f(10f,0f));


        return "Zatim tohle tvoje "+ cmdName+"("+ Joiner.on(",").join(args) +") neumim, sorry chlape.";
    }

    @Override
    public String getInfoString() {
        return super.getInfoString()+" step: "+step+" coins: "+coins;
    }

    private String go(String dir) {
        switch (dir) {
            case "right": return go(WalkDir.RIGHT);
            case "left" : return go(WalkDir.LEFT);
        }

        return "Unsupported walk dir '"+dir+"' !";
    }

    private String jump(String dir) {
        switch (dir) {
            case "right"   : updateWalkDir(WalkDir.RIGHT); return jumpRight();
            case "left"    : updateWalkDir(WalkDir.LEFT);  return jumpLeft();
            case "up"      : return jumpUp();
            case "default" : return jump();
        }

        return "Unsupported jump dir '"+dir+"' !";
    }

    private String jumpRight() {animationShape.setAnimationFrame("walkRight",2); return jump(30,-65) ;} // jump( 15f, -40f);}
    private String jumpLeft()  {animationShape.setAnimationFrame("walkLeft",2); return jump(-30,-65) ;} // jump(-15f, -40f);}
    private String jumpUp() {animationShape.setAnimationFrame("standFront",0); return jump(0,-50f);}


    private String jump() {
        switch (walkDir) {
            case RIGHT: return jumpRight();
            case FRONT: return jumpUp();
            case LEFT:  return jumpLeft();
        }
        return null;
    }


    private String jump(float x, float y) {
        if (canJump) {
            getBody().adjustVelocity(new Vector2f(x,y));
            canJump = false;
            canJumpCountdown = jumpCoolDown;
        }
        return null;
    }

    public static final float stepSize = 34.5f;

    private boolean trhanaChuze = false;

    public static final float MAX_SPEED = 1.5f*34.5f;

    private String go(WalkDir dir) {

        if (canWalk) {
            canWalk = false;
            canWalkCountdown = walkCoolDown;
        } else {
            return null;
        }

        if (walkDir != dir && walkDir != WalkDir.FRONT) {
            updateWalkDir(WalkDir.FRONT);
            return "Jen jsem se otočil.";
        } else {

            if (trhanaChuze) {
                getBody().adjustPosition(new Vector2f( (dir==WalkDir.RIGHT?1:-1)*stepSize, 0f));
            } else {
                if (getBody().getVelocity().length() < MAX_SPEED) {
                    getBody().adjustVelocity(new Vector2f( (dir==WalkDir.RIGHT?1:-1)*stepSize, -5f));
                    getBody().adjustPosition(new Vector2f((dir == WalkDir.RIGHT ? 1 : -1) * stepSize*0.15f, 0f));
                }
            }

            updateWalkDir(dir);
            return "Popošel jsem..";
        }
    }





}
