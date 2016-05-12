package cz.tomkren.kutil.core;

import java.awt.Color;
import java.awt.Graphics2D;

import java.util.*;

import cz.tomkren.utils.Log;
import cz.tomkren.kutil.core.masters.*;
import cz.tomkren.kutil.items.*;
import cz.tomkren.kutil.kobjects.frame.Frame;
import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.DynamicShape;
import net.phys2d.raw.strategies.QuadSpaceStrategy;
import org.json.JSONObject;

/** TODO */

//TODO vyřešit pořádně ty hasPrevious a to jak je několik front na mazání atd (použít nějakej deque nebo nevim ale prostě pochopitelně a rychle aby to frčelo)

public class KObject implements KAttVal {

    private Kutil kutil;

    private KObject                 parent;     // objekt, v jehož vnitřku je tento object

    private Items                   items;      // drží všechny položky tohoto objektu pohromadě
    private KItem<List<KObject>>    inside;     // objekty tvořící vnitřek tohoto objektu


    private KItem<JSONObject>       args;



    private KItem<String>           type;       // typ tohoto objektu
    private KItem<String>           id;         // id tohoto objektu

    private KItem<Int2D>            pos;        // fyzická pozice tohoto objektu
    private KItem<String>           shapeCmd;   // příkaz definující tvar objektu
    private KItem<Boolean>          physical;   // určuje zda se jedná o fyzickální objekt
    private KItem<Boolean>          attached;   // určuje zda je fyzický objekt nepohyblivý(pro nefyz.nemá význam)

    private KItem<Boolean>          movable;    // určuje zda je možno objektem hýbat myší
    private KItem<Boolean>          rotable;    // určuje zda objekt může být rotován - true nedoporučeno
    private KItem<Boolean>          guiStuff;   // určuje zda je tento objekt součást gui a tedy s ním nejdou některé nepříjemnosti (smazat, vylézt nad)

    private KItem<Boolean>          main;       // určuje, zda se jedná o hlavní objekt


    private World   world;                // fyzikální simulace uvnitř objektu
    private Body    body;                 // těleso reprezentující tento objekt ve vnitřku parent objektu
    private boolean isAffectedByGravity;  // určuje, zda je objekt ovlivněn gravitací


    private KShape  shape;              // tvar objektu
    private boolean stepInside;         // určuje, zda je vnitřním objektům tohoto objektu posílaná instrukce na udělání kroku
    private double  rot;                // rotace objektu
    private Color   bgcolor;            // barva pozadí vnitřku
    private boolean isHighlighted ;     // udává, zda je objekt zvýrazněn (při o značení např.)

    private List<KObject> objectsToAdd;    //objekty čekající na přidaní na poslední místo do vnitřku
    private List<KObject> objectsToRemove; //objekty čekající na vymazání z vnitřku

    private String  oldCopyId;     // pomocná proměnná používaná při kopírování objektů
    private boolean isInitialised; // určuje zda už proběhla inicializace metodou init() // todo k čemu to tu vlastně je ???? PRavděpodobně hax pro: insertKObjectToSystem


    public KObject(KAtts kAtts, Kutil kutil) {
        this.kutil = kutil;

        items = new Items();

        type   = items.addString(kAtts, "type", null);
        id     = items.addString(kAtts, "id", null);

        inside = items.addList(kAtts, "inside");

        args = items.addJSONObject(kAtts, "args", new JSONObject());

        pos      = items.addInt2D(kAtts, "pos", Int2D.zero());
        shapeCmd = items.addString(kAtts, "shape", null);
        physical = items.addBoolean(kAtts, "physical", false);
        attached = items.addBoolean(kAtts, "attached", false);
        movable  = items.addBoolean(kAtts, "movable", true);
        rotable  = items.addBoolean(kAtts, "rotable", false);
        guiStuff = items.addBoolean(kAtts, "guiStuff", false);
        main     = items.addBoolean(kAtts, "main", false);


        create();
    }

    // TODO | bylo zatím používáno jen v Text, který už není KOBject, pokudmožno uplně odstranit, ale nejprve prozkoumat, zda není potřeba v budoucnu jinde
    // todo | No používá se to pro dědice co potřebujou vznikat např Kispem, aka Box()...
    /*
    public KObject(Kutil kutil){
        this.kutil = kutil;

        items = new Items();

        type    = items.add("type", null);
        id      = items.add("id", null);
        inside  = items.addEmptyList("inside");

        pos      = items.add("pos", Int2D.zero());
        shapeCmd = items.add("shape", null);
        physical = items.add("physical", false, false);
        attached = items.add("attached", false, false);
        movable  = items.add("movable", true, true);
        rotable  = items.add("rotable", false, false);
        guiStuff = items.add("guiStuff", false, false);
        main     = items.add("main", false, false);

        create();
    }
    */

    public KObject copy() {return new KObject(this, kutil);}


    // TODO celý nějaký podezřelý, maj ostatní taky kopírovací konstruktory?? něšlo by to automatizovaně přes xml-string repre s dodatečnym updatama líp?  atd... PROMYSLET
    // TODO nestrácí to třeba ty vlastnosti co tomu přidá potomek bez overdajdlího kopírovacího konstruktoru??? atd
    public KObject(KObject b, Kutil kutil) {
        this.kutil = kutil;

        items = new Items();

        type     = items.add("type", null);
        id       = items.add("id", null);
        inside   = items.addEmptyList("inside");

        // TODO zčečit že fakt funguje ok
        args = items.add("args", new JSONObject(b.args().toString()) );


        pos      = items.add("pos", b.pos.get().copy());
        shapeCmd = items.add("shape", b.shapeCmd.get());
        physical = items.add("physical", b.physical.get(), false);
        attached = items.add("attached", b.attached.get(), false);
        movable  = items.add("movable", b.movable.get(), true);
        rotable  = items.add("rotable", b.rotable.get(), false);
        guiStuff = items.add("guiStuff", b.guiStuff.get(), false);
        main     = items.add("main", b.main.get(), false);

        for (KObject o :  b.inside()) {
            KObject copy = o.copy();
            inside.get().add(copy);
        }

        oldCopyId = b.id();

        create();
    }




    private void create() {
        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

        isInitialised = false;

        if (main.get()) {
            simulationMaster().setMain(this);
        }

        shape = kutil.shapeFactory().newKShape(shapeCmd.get(), kutil);

        stepInside = true;

        bgcolor = Color.white; // TODO prozatímní zjednodušení !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        rot = 0f;

        isAffectedByGravity = true;

        world = new World( new Vector2f(0.0f, 10.0f), 10, new QuadSpaceStrategy(20,5) );

        objectsToAdd    = new ArrayList<>();
        objectsToRemove = new ArrayList<>();
    }

    public void setShape(KShape kShape) {
        shape = kShape;
    }

    /** Tato metoda je volána při vytváření objektu pro informovaní objektu o jeho rodiči. */
    public void parentInfo(KObject parent) {
        this.parent = parent;
        inside.get().forEach(o -> o.parentInfo(this));
    }

    /** Voláním této metody se dokončí druhá fáze inicializace objektu. */
    public void init() {

        if (isInitialised) {return;}
        isInitialised = true;

        // pokud objekt nemá explicitní id, dostane nějaké přiděleno.
        if (id.get() == null) {
            id.set( idDB().getUniqueID() );
            idDB().put(id.get(), this);
            Log.it("přidáno implicitní id " + id.get());
        }

        // pokud tento objekt vznikl jako kopie
        if (isCopied()) {
            kutil().getIdChangeDB().put(oldCopyId, id.get());
        }

        // pokud se jedná o fyzikální objekt:
        // - vytvoříme vhodné fyzikální těleso
        // - toto těleso vložíme do světa parent objektu
        if (physical.get()) {

            DynamicShape phys2dShape = shape.getPhys2dShape();
            body = attached.get() ? new StaticBody(phys2dShape) : new Body(phys2dShape, 100.0f);

            body.setGravityEffected(isAffectedByGravity);
            body.setRotatable(rotable.get());
            body.setUserData(this);

            setBodyPos(pos.get());

            World parentWorld = getParentWorld();
            if( parentWorld != null ){
                parentWorld.add(body);
            }
        }

        /* todo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if( onInit.get() != null ){
            Global.rucksack().cmd( onInit.get() );
        }
        */

        inside.get().forEach(KObject::init);

    }

    public void step() {

        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! zkopčeno jen minimum, potřeba rozšířit pak !!!!!!!!!!!!!!!!!!!!!!

        if (physical.get()) {
            pos.set( shape.getPosByPhys2dCenter(body.getPosition()) );
            rot = body.getRotation();
        }

        if (stepInside && simulationMaster().isSimulationRunning() && world.getBodies().size() > 0) {
            world.step();
            world.step();
            world.step();
            world.step();
            world.step();
        }

        if (!objectsToRemove.isEmpty()) {
            internalRemove();
        }

        if(!objectsToAdd.isEmpty()) {
            internalAdd();
        }

        if (stepInside) {
            for (KObject o : inside.get()) {
                o.step();
                /* TODO !!!
                if( o instanceof Field ) manageField( (Field) o );
                */
            }
        }

    }

    public void setType(String t) {type.set(t);}

    public KObject parent(){return parent;}
    public void setParent( KObject newParent ){parent = newParent;}

    public String id(){return id.get();}
    public Items items() {return items;}
    public List<KObject> inside(){return inside.get();}

    public JSONObject args() {return args.get();}

    public Kutil kutil() {return kutil;}
    public IdDB idDB() {return kutil.getIdDB();}

    public KeyboardMaster keyboardMaster() {return kutil.getKeyboardMaster();}
    public MouseMaster mouseMaster() {return kutil.getMouseMaster();}
    public PopupMaster popupMaster() {return kutil.getPopupMaster();}
    public SelectionMaster selectionMaster() {return kutil.getSelectionMaster();}
    public CmdMaster cmdMaster() {return kutil.getCmdMaster();}
    public GuiMaster drawingMaster() {return kutil.getGuiMaster();}
    public SimulationMaster simulationMaster() {return kutil.getSimulationMaster();}


    /** Vrací zda tento objekt vznikl kopírováním. */
    public boolean isCopied() {
        return oldCopyId != null;
    }

    /**
     * Pokud objekt potřebuje zareagovat na to, že byl zkopírován, dělá to v této metodě. Tato metoda volá tuto metodu svých vnitřních objektů.
     * TODO adept na zneškodnění nebo nějak elegantnějc celý spolčně s insertKObjectToSystem kterej to volá
     */
    public void resolveCopying() {
        if (isCopied()) {
            inside.get().forEach(KObject::resolveCopying);
        }
    }

    // TODO nové, pokus vyřešit problém při undo, lépe pojmenovat začlenit etc
    public void resolveCopying_2(Set<String> ids) {
        inside().forEach(o -> o.resolveCopying_2(ids));
    }

    //POZN nové (#k2), pokus vyřešit problém při undo
    public Set<String> getIds() {
        Set<String> ids = new HashSet<>();
        addIds(ids);
        return ids;
    }
    private void addIds(Set<String> ids) {
        ids.add(id.get());
        inside().forEach(o -> o.addIds(ids));
    }


    public KShape shape() {return shape;}

    public Int2D pos() {return pos.get();}

    public void setPos( Int2D p ){
        pos.set(p);
        setBodyPos(p);
    }

    public boolean getIsMovable(){return movable.get();}
    public void setMovable (boolean b) {movable.set(b);}

    public boolean getIsGuiStuff() {return guiStuff.get();}
    public void setIsGuiStuff(boolean b){
        guiStuff.set(b);
    }

    public Color bgcolor(){
        return bgcolor;
    }

    public void setHighlighted(boolean isHighlighted) {this.isHighlighted = isHighlighted;}
    public boolean isHighlighted() {return isHighlighted;}


    // --- PHYS STUFF ---

    public void setIsPhysical(boolean newVal) {physical.set(newVal);}
    public void setIsAttached(boolean newVal) {attached.set(newVal);}

    public boolean isPhysical() {return physical.get();}
    public void setIsAffectedByGravity(boolean b) {isAffectedByGravity = b;}

    public Body getBody(){return body;}

    private void setBodyPos(Int2D newPos) {
        if (body == null) {return;}
        ROVector2f bodyPos = shape.getPhys2dCenter(newPos);
        body.setPosition(bodyPos.getX(), bodyPos.getY());
    }

    public void setSpeed( Int2D v ){
        if (body == null) {return;}
        ROVector2f vel = body.getVelocity();
        body.adjustVelocity(new Vector2f(v.getX() - vel.getX(), v.getY() - vel.getY()));
    }

    public World getWorld(){return world;}

    public World getParentWorld(){
        if( parent != null ){
            return parent.getWorld();
        }
        return null;
    }






    public void drawInside(Graphics2D g, Int2D center, int frameDepth, double zoom, Int2D zoomCenter) {
        for (KObject o : inside.get()) {
            o.drawOutside(g, center, frameDepth, zoom, zoomCenter);
        }
    }


    public void drawOutside(Graphics2D g, Int2D center, int frameDepth, double zoom, Int2D zoomCenter) {
        shape.draw(g, isHighlighted, drawingMaster().showInfo() ? getInfoString() : null, pos.get(), center, rot, rotable.get(), zoom, zoomCenter);
    }






    public String getInfoString() {
        return id.get() + " ["+pos.get()+"]";
    }

    public Xml toXml(){
        XmlElement ret = new XmlElement(XmlLoader.OBJECT_TAG);
        items.addAttsToXmlElement(ret);
        return ret;
    }

    public JSONObject toJson(){
        JSONObject ret = new JSONObject();

        for (KItem item : items.getItemList()) {
            item.addToJson(ret);
        }

        return ret;
    }











    /**
     * Vrací, zda je objekt zasažen kliknutím.
     * @param clickPos pozice kliknutí
     * @return zda byl objekt zasažen
     */
    public boolean isHit(Int2D clickPos, double zoom, Int2D center) {
        return shape.isHit(pos.get(), clickPos, rot, zoom, center);
    }




    // todo ? možná ať vrací jestli něco bylo zasahlý a frejm ať se nastaví sám, když to volá
    public void clickInside(Frame frame, Int2D clickPos, double zoom, Int2D center) {
        selectionMaster().setSelectedFrame(frame);
        ListIterator<KObject> it = getLastIteratorOfInside();
        while (it.hasPrevious()) {
            KObject o = it.previous();
            if( o.isHit(clickPos, zoom, center) ){
                o.click(clickPos);
                return;
            }
        }
        selectionMaster().setSelected(frame);
    }

    // TODO divný co se tam uvnitř děje, zjistit co to přesně dělá a co je záměr
    public void releaseInside(Int2D clickPos, double zoom, Int2D center) {
        ListIterator<KObject> iter = getLastIteratorOfInside();
        while (iter.hasPrevious()) {
            KObject o = iter.previous();
            if (o.isHit(clickPos, zoom, center)) {
                if (this instanceof Frame || o.getIsMovable()) {
                    o.release(clickPos, this);
                } else {
                    mouseMaster().pasteFromCursor(this, clickPos);
                }
                return;
            }
        }
        mouseMaster().pasteFromCursor(this, clickPos);
    }

    public void dragInside(Frame frame, Int2D clickPos, Int2D delta, double zoom, Int2D center) {
        selectionMaster().setSelectedFrame(frame);
        mouseMaster().onDrag(clickPos);
        ListIterator<KObject> iter = getLastIteratorOfInside();
        while (iter.hasPrevious()) {
            KObject o = iter.previous();
            if(o.isHit(clickPos,zoom,center)) {
                o.drag(clickPos, delta, frame);
                return;
            }
        }
        if (!mouseMaster().somethingOnCursor()) {
            frame.moveCam(delta);
        }
        selectionMaster().setSelected(frame);
    }

    // TODO "co sem patří ??" znělo todo uvnitř metody, domyslet!

    public void wheelInside(Int2D wheelPos, double zoom, Int2D center) {
        ListIterator<KObject> iter = getLastIteratorOfInside();
        while (iter.hasPrevious()) {
            KObject o = iter.previous();
            if (o.isHit(wheelPos, zoom, center)) {
                o.wheel(wheelPos);
                return;
            }
        }
    }





    private ListIterator<KObject> getLastIteratorOfInside() {
        List<KObject> xs = inside.get();
        return xs.listIterator(xs.size());
    }








    //------------------------------------------------------------------------------------------------------------------



    /**
     * Reakce na táhnutí myši na samotném objektu (na jeho vnějšku).
     * @param clickPos pozice začátku táhnutí
     * @param delta vektor táhnutí (kolik se táhlo)
     */
    public void drag(Int2D clickPos, Int2D delta, Frame f) {
        if (movable.get()) {
            mouseMaster().cutToCursor(this, clickPos);
        }
        else{
            if( mouseMaster().somethingOnCursor() ) return;
            selectionMaster().setSelected(this);
            f.moveCam(delta);
        }
    }

    /**
     * Reakce na kliknutí myši na samotném objektu (na jeho vnějšku).
     * @param clickPos pozice kliknutí
     */
    public void click(Int2D clickPos) {
        selectionMaster().setSelected(this);
    }

    public void wheel(Int2D wheelPos) {
        // asi nic ale ve framu se overridne
    }


    /**
     * Reakce na pustění tlačítka myši na samotném objektu (na jeho vnějšku).
     * @param clickPos pozice puštění
     */
    public void release(Int2D clickPos , KObject obj ){
        //rucksack().pasteFromCursor(this , Int2D.zero ); //pokud tam je tak umožnuje vkladat dovnitř
        // objektu pouhým přetažením

        mouseMaster().pasteFromCursor(obj, clickPos);
    }















    // ============= přidávání mazání etc =============  TODO chtělo by nějakou pořádnou revizi imho

    // -- Přidávání --

    /**
     * Přidá daný objekt do vntřku (na konec) tohoto objektu.
     * @param o objekt k přidání
     */
    public void add(KObject o) {
        objectsToAdd.add(o);
    }

    private void internalAdd() {
        for( KObject o : objectsToAdd ){
            if (o.isPhysical()) {
                world.add(o.getBody());
            }
            inside.get().add(o);
        }
        objectsToAdd.clear();
    }



    // -- Mazání --

    /** Úplně smaže objekt. */
    public void delete(){
        remove();
        idDB().remove(id());
    }

    /** Odstraní objekt ze svého rodiče. */
    public void remove(){
        if( parent != null ){
            parent.remove(this);
        }
    }

    /**
     * Odstraní daný objekt z vntřku tohoto objektu.
     * @param o objekt k vymazání
     */
    public void remove(KObject o) {
        objectsToRemove.add(o);
    }

    private void internalRemove(){

        for (KObject o : objectsToRemove) {

            /* TODO field stuff ... !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            for( KObject f : inside.get() ){
                if( f instanceof Field ) ( (Field) f ).informFieldAboutDeletation(o);
            }
            */

            if (o.isPhysical()) {
                world.remove(o.getBody());
            }
            inside.get().remove(o);
        }

        objectsToRemove.clear();
    }







}
