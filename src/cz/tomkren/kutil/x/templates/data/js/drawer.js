function mkDrawer(opts) {

    function getOrDef(val, defaultVal) {
        return (val === undefined ? defaultVal : val);
    }

    var canvasId   = getOrDef(opts.canvasId, undefined);
    var scale      = getOrDef(opts.scale, 1.0);
    var dx         = getOrDef(opts.dx, 0);
    var dy         = getOrDef(opts.dy, 0);
    var actScene   = getOrDef(opts.scene, 0);
    var zoomFactor = getOrDef(opts.zoomFactor, 2.0);
    var silent     = getOrDef(opts.silent, true);

    var $canvas;
    var isMouseDown = false;
    var mousePos;

    function init() {

        if (canvasId != undefined) {

            $canvas = $('#'+canvasId);

            $canvas.mousedown(function(e) {
                isMouseDown = true;
                mousePos = [e.pageX,e.pageY];
                log('down '+mousePos);
            });

            $canvas.mousemove(function(e) {
                if (isMouseDown) {

                    // drag ...

                    var newMousePos = [e.pageX,e.pageY];
                    var pxDelta = minus(newMousePos,mousePos);
                    mousePos = newMousePos;

                    log('drag '+pxDelta+' '+trInverse(pxDelta,false));

                    drag(pxDelta);
                }
            });

            $canvas.mouseup(function() {
                isMouseDown = false;
                log('up');
            });

            $canvas.bind('mousewheel DOMMouseScroll', function(e){
                if (e.originalEvent.wheelDelta > 0 || e.originalEvent.detail < 0) {
                    // scroll up
                    log('zoom in');
                    zoom(true);
                }
                else {
                    // scroll down
                    log('zoom out');
                    zoom(false);
                }
                return false;
            });

        }

    }

    function drag(pxDelta) {
        var delta = trInverse(pxDelta,false);
        dx += delta[0];
        dy += delta[1];

        log(dx+' '+dy);

        draw();

        //console.log(dx+" "+dy+" "+scale);
    }

    function zoom(isZoomIn) {
        scale *= (isZoomIn ? zoomFactor : 1/zoomFactor);
        draw();
    }


    function tr(pos, move) {
        if (move === undefined) {move = false;}
        var x = pos[0];
        var y = pos[1];
        return [(x+(move?dx:0))*scale, (y+(move?dy:0))*scale];
    }

    function trInverse(tpos, move) {
        if (move === undefined) {move = false;}
        var tx = tpos[0];
        var ty = tpos[1];
        return [tx/scale-(move?dx:0), ty/scale-(move?dy:0)];
    }

    function log(x) {
        if (!silent) {
            console.log(x);
        }
    }

    function minus(pos1,pos2) {
        return [pos1[0]-pos2[0],pos1[1]-pos2[1]];
    }

    function drawTestBone_hax(ctx,transformFun) {
        //console.log("HAX");
        var svgResource = mkSvgResource(gameState.svgs[1]);
        svgResource.draw(ctx,transformFun);
    }

    function draw(scene) {

        if (canvasId === undefined) {
            log('  ERROR : undefined canvasId');
            return;
        }

        scene = getOrDef(scene, actScene);
        actScene = scene;

        if (scene === undefined) {
            log('  ERROR : undefined scene');
            return;
        }

        log("Drawer.draw() called.");

        var canvas = document.getElementById(canvasId);

        if (!canvas.getContext){ // canvas-unsupported code here
            log('  ERROR : canvas unsupported');
            return;
        }

        log('  Canvas supported.');

        var ctx = canvas.getContext('2d');

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        drawTestBone_hax(ctx,tr);


        var sceneObjs = scene.inside;

        for (var i = 0; i < sceneObjs.length; i++) {
            var o = sceneObjs[i];


            var pos = o.pos ? o.pos.split(' ') : [0,0];
            pos = tr([parseFloat(pos[0]),parseFloat(pos[1])],true);

            var shape = o.shape ? o.shape.split(' ') : ['rectangle','32','32'];


            var size;

            if (shape[0] === 'rectangle') {


                size = tr([parseFloat(shape[1]),parseFloat(shape[2])],false);


            } else {
                size = tr([64,32],false);
            }




            ctx.fillStyle = "rgb(0, 0, 0)";//"rgba(0, 0, 200, 0.5)";
            ctx.fillRect(pos[0], pos[1], size[0], size[1]);


            //log(o);

        }
        log("  Drawing finished.");
    }

    log("Drawer created.");
    init();

    return {
        draw: draw,
        transform: tr,
        inverseTransform: trInverse
    };
}