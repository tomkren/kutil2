function mkSvgResource(svgJson) {

    function getSvgJson() {
        return svgJson;
    }

    function add(pos1,pos2) {
        return [pos1[0]+pos2[0],pos1[1]+pos2[1]];
    }

    function draw(ctx, transformFun) {
        _.each(svgJson.paths, function(path) {
            drawPath(path, ctx, transformFun);
        });
    }

    function drawPath(path, ctx, transformFun) {

        path = _.filter(path,_.isArray);

        ctx.strokeStyle = "rgb(10, 200, 30)";
        ctx.beginPath();

        var nextStart = path[0];
        var tNextStart = transformFun(nextStart,true);

        ctx.moveTo(tNextStart[0],tNextStart[1]);

        for (var i = 1; i < path.length; i+=3) {

            var bez1 = add(nextStart, path[i]);
            var bez2 = add(nextStart, path[i+1]);
            var bez3 = add(nextStart, path[i+2]);

            var tBez1 = transformFun(bez1,true);
            var tBez2 = transformFun(bez2,true);
            var tBez3 = transformFun(bez3,true);

            ctx.bezierCurveTo(tBez1[0],tBez1[1],tBez2[0],tBez2[1],tBez3[0],tBez3[1]);

            nextStart = bez3;
        }

        ctx.closePath();
        ctx.stroke();


    }


    return {
        draw: draw,
        getSvgJson : getSvgJson
    };
}
