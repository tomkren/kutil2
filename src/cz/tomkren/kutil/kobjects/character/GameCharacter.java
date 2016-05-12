package cz.tomkren.kutil.kobjects.character;

import com.google.common.base.Joiner;
import cz.tomkren.helpers.ResourceLoader;
import cz.tomkren.kutil.core.KAtts;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.core.Player;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.items.KItem;
import cz.tomkren.kutil.kobjects.character.brains.SimpleBrain;
import cz.tomkren.kutil.kobjects.character.effectors.*;
import cz.tomkren.kutil.kobjects.character.maps.MapGraph;
import cz.tomkren.kutil.kobjects.character.sensors.SimpleVision;
import cz.tomkren.kutil.kobjects.coin.Coin;
import cz.tomkren.kutil.kobjects.frame.Frame;
import cz.tomkren.kutil.shapes.AnimationShape;
import net.phys2d.raw.Body;
import net.phys2d.raw.CollisionEvent;
import net.phys2d.raw.CollisionListener;
import net.phys2d.raw.World;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;

/** Created by tom on 6.8.2015. */

public class GameCharacter extends KObject implements Player, CollisionListener {

    private KItem<Boolean> isHumanPlayer;
    private KItem<Integer> coins;

    private JSONObject config;
    private AnimationShape shape;
    private String log;

    private SimpleVision vision;
    private SimpleBrain brain;
    private Legs legs;

    private MapGraph mapGraph; // TODO pak přesunout asi, ale než se vyvrbí co to vlastně je, tak nechat tady

    private int step;
    private int stepsPerAction;
    private World parentWorldWithListener;

    public static final String DEFAULT_CONFIG_PATH = "/cz/tomkren/kutil/kobjects/vincent/vincent.json";
    public static final Font LOG_FONT = new Font( Font.SANS_SERIF , Font.PLAIN , 10 );

    public GameCharacter(KAtts kAtts, Kutil kutil) {
        super(kAtts, kutil);

        setType("character");

        isHumanPlayer = items().addBoolean(kAtts, "human", false);
        KItem<String> configPath = items().addString(kAtts, "config", DEFAULT_CONFIG_PATH);
        coins = items().addInteger(kAtts, "coins", 0);

        setIsPhysical(true);

        if (isHumanPlayer.get()) {
            kutil.getPlayerMaster().setActualPlayer(this);
        }

        config = new ResourceLoader().loadJSON(configPath.get());
        shape = new AnimationShape(configPath.get());
        setShape(shape);

        step = 0;
        log  = "";
        stepsPerAction = config.getInt("stepsPerAction");
    }

    private void log(String msg) {
        log += msg + '\n';
    }

    @Override
    public void init() {
        super.init();

        Body body = getBody();
        body.setFriction((float) config.getDouble("friction"));

        legs = new SimpleLegs(body, shape);
        legs.updateDir(Legs.Dir.FRONT);
        legs.addMovement(Legs.Action.WALK, new SimpleComboWalk(config.getJSONObject("walk"), legs));
        legs.addMovement(Legs.Action.JUMP, new SimpleJump(config.getJSONObject("jump"), legs));
        legs.addMovement(Legs.Action.STOP, new SimpleStop(legs));

        resetCollisionListener();
        resetSensors();

        mapGraph = new MapGraph();

        brain = new SimpleBrain(this);

        log("Hello, my name is "+id()+ ".");
    }

    @Override
    public void setParent(KObject newParent) {
        super.setParent(newParent);
        resetCollisionListener();
        resetSensors();
    }

    @Override
    public void step() {
        super.step();
        legs.stepInform();
        step ++;

        if (!isHumanPlayer.get()) {
            if (step % stepsPerAction == 0) {
                brain.performAction();
            }
        }
    }

    @Override
    public void drawOutside(Graphics2D g, Int2D center, int frameDepth, double zoom, Int2D zoomCenter) {
        super.drawOutside(g, center, frameDepth, zoom, zoomCenter);


        if (isHighlighted()) {

            Int2D drawPos = pos().plus(center);
            int x = drawPos.getX();
            int y = drawPos.getY();

            int d = 10;
            g.setColor(Color.magenta);
            g.drawLine(x-d,y-d,x+d,y+d);
            g.drawLine(x-d,y+d,x+d,y-d);

            int dxLog = 60;
            int dyLog = 10;
            g.setFont(LOG_FONT);
            g.drawString(log, x+dxLog, y+dyLog);
        }
    }

    public Legs getLegs() {return legs;}
    public SimpleVision getVision() {return vision;}


    @Override
    public String getInfoString() {
        return super.getInfoString()+" step: "+step+" coins: "+coins.get();
    }

    @Override
    public String playerCmd(String cmdName, List<String> args) {
        if (!simulationMaster().isSimulationRunning()) {return null;}

        // TODO předělat pořádně ..
        switch (cmdName) {
            case "go"    : return legs.move(Legs.Action.WALK, Legs.fromString(args.get(0)));
            case "jump"  : return legs.move(Legs.Action.JUMP, Legs.fromString(args.get(0)));
            case "front" : return legs.move(Legs.Action.STOP, Legs.Dir.FRONT);
        }

        return "Zatim tohle tvoje "+ cmdName+"("+ Joiner.on(",").join(args) +") neumim, sorry chlape.";
    }

    @Override
    public void changeCam(Frame frameShowingThePlayer) {
        if (isHumanPlayer.get()) {

            // TODO vytknout pryč

            Int2D camPos = pos();
            Int2D frameSize = frameShowingThePlayer.getSize();
            frameShowingThePlayer.setCam(new Int2D(camPos.getX() - frameSize.getX() / 2, camPos.getY() - frameSize.getY() / 2));

        }
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
            coins.set(1+coins.get());
        }
    }

    private void resetSensors() {
        vision = new SimpleVision(parent());
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

}
