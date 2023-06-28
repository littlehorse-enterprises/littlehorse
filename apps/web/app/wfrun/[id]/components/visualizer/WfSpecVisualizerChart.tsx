"use client"
import { useCallback, useEffect } from "react";
import * as d3 from "d3";

let  _d3:d3.Selection<d3.BaseType, unknown, HTMLElement, any>
// let svg:d3.Selection<SVGGElement, unknown, HTMLElement, any>

const boxHeight = 300;
const width=936;
const height=900;

let nodes:any[] = [];
  nodes.push([width / 2, boxHeight / 1.5]);
  nodes.push([width / 4, boxHeight / 3]);


  function roundedRect(x, y, width, height, radius) {
    return "M" + ((x-(width/2))+radius) + "," + y
         + "h" + (width - radius*2)
         + "a" + radius + "," + radius + " 0 0 1 " + radius + "," + radius
         + "v" + (height - 2 * radius)
         + "a" + radius + "," + radius + " 0 0 1 " + -radius + "," + radius
         + "h" + (radius*2 - width)
         + "a" + radius + "," + radius + " 0 0 1 " + -radius + "," + -radius
         + "v" + ( 2 * radius -height)
         + "a" + radius + "," + radius + " 0 0 1 " + radius + "," + -radius
         + "z";
  }
  function rArrow(x, y, width, height, radius) {
    return "M" + ((x+(width/2))+radius) + "," + (y+(height/2))
         + "h" + (width - radius*2)
         + "a" + radius + "," + radius + " 0 0 1 " + radius + "," + radius
         + "v" + (height - 2 * radius)

  }
  function lArrow(x, y, width, height, radius) {
    return "M" + ((x-(width/2))-radius) + "," + (y+(height/2))
         + "h" + (-width + radius*2)
         + "a" + radius + "," + radius + " 1 0 0 " + -radius + "," + radius
         + "v" + (height - 2 * radius)

  }
  function rbArrow(x, y, width, height, radius) {
    return "M" + ((x+(width*1.5))) + "," + (y+(height*5)-3)
         + "v" + (height)
         + "a" + radius + "," + radius + " 0 0 1 " + -radius + "," + radius
         + "h" + (-width + radius*2)
         + "l" + 3 + "," + 3
         + "m" + -3 + "," + -3
         + "l" + 3 + "," + -3

  }
  function lbArrow(x, y, width, height, radius) {
    return "M" + ((x-(width*1.5))) + "," + (y+(height*5)-3)
        + "v" + (height)
        + "a" + radius + "," + radius + " 1 0 0 " + radius + "," + radius
         + "h" + (width - radius*2)
         + "l" + -3 + "," + -3
         + "m" + +3 + "," + +3
         + "l" + -3 + "," + +3
  }
  function backArrow(x, y, width, height, radius) {
    return "M" + ((x+(width*1.5))-50) + "," + (y+25)
        + "h" + (20)
        + "a" + radius + "," + radius + " 1 0 0 " + radius + "," + -radius
         + "v" + (-height+20)
         + "a" + radius + "," + radius + " 1 0 0 " + -radius + "," + -radius
         + "h" + (-20)
         + "l" + 3 + "," + 3
         + "m" + -3 + "," + -3
         + "l" + 3 + "," + -3
  }

export const WfSpecVisualizerChart = ({data, onClick}:{data:any, onClick:(n:any) => void}) => {
    const clickHandler = (_p:any, d:any) => {
        console.log(_p,d)
        onClick(d)
    }
    
    const minHeight = 862
    const drawChart = useCallback((data:any[]) => {
        _d3.select("svg").remove();
        let svg = _d3.append("svg")
            .attr("width",width)
            .attr("height",data.length*110 < minHeight ? minHeight : data.length*110);
            // .attr("height",height);



        //ARROWS
        let lineG = svg.append("g").selectAll("g");
        lineG.data(data)
        .enter()
            .append("line")
            .style("stroke-width", 1)
            .style("stroke", (d, ix) => {
                if(!ix || (d.type==='NOP' && d.node.outgoingEdges.length === 1)) return "transparent"
                return "#B3B3B3"
            } )
            .attr("x1", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y1", (d) => {
                if(d.level === 1)  return (d.level * 90) +38
                return (d.level * 90) +30
            })
            .attr("x2", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y2", (d) => ((d.level+1) * 90) -32)

        lineG.data(data)
        .enter()
            .append("line")
            .style("stroke-width", 1)
            .style("stroke", (d, ix) => {
                if(!ix || (d.type==='NOP' && d.node.outgoingEdges.length === 1)) return "transparent"
                return "#B3B3B3"
            } )
            .attr("x1", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)-3+px
            })
            .attr("y1", (d) => ((d.level+1) * 90) -35)
            .attr("x2", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y2", (d) => ((d.level+1) * 90) -32)
        lineG.data(data)
        .enter()
            .append("line")
            .style("stroke-width", 1)
            .style("stroke", (d, ix) => {
                if(!ix || (d.type==='NOP' && d.node.outgoingEdges.length === 1)) return "transparent"
                return "#B3B3B3"
            } )
            .attr("x1", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+3+px
            })
            .attr("y1", (d) => ((d.level+1) * 90) -35)
            .attr("x2", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y2", (d) => ((d.level+1) * 90) -32)
        

        //NODES
        let nodes = svg.append("g").selectAll("g");
        nodes.data(data)
        .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", function(d,i) {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                if(d.type === 'ENTRYPOINT')  return roundedRect((width/2)+px, (d.level * 90) +65 , 120, 56, 28);
                if(d.type === 'EXIT')  return roundedRect((width/2)+px, (d.level * 90) +65 , 100, 56, 28);
                if(d.type === 'TASK')  return roundedRect((width/2)+px, (d.level * 90) +65 , 180, 48, 12);
                if(d.type === 'START_THREAD')  return roundedRect((width/2)+px, (d.level * 90) +65 , 180, 48, 12);
                if(d.type === 'WAIT_FOR_THREAD')  return roundedRect((width/2)+px, (d.level * 90) +65 , 180, 48, 12);
                return roundedRect((width/2)+px, (d.level * 90) +65 , 97, 48, 12);
            })
            .attr("fill-opacity",(d) => {
                if(d.type === 'ENTRYPOINT')  return .2;
                if(d.type === 'EXIT')  return .2;
                return 1;
            })
            .attr("fill",(d) => {
                if(d.type === 'ENTRYPOINT')  return '#007AFF';
                if(d.type === 'EXIT')  return '#34C759';
                return '#363A41';
            })
            .attr("stroke",(d) => {
                if(d.type === 'ENTRYPOINT')  return '#007AFF';
                if(d.type === 'EXIT')  return '#34C759';
                return '#363A41';
            }).on("click",clickHandler)
            .style("cursor", "pointer")
     
        //ICONS
        svg.selectAll("image")
        .data(data)
        .enter()
        .append("image")
        .attr("xlink:href",(d) => "/"+d.type+".svg")
        .attr("x", (d) => {
            let px = 0
            if(d.px === 'left') px = -150
            if(d.px === 'right') px = 150
            if(d.type === 'ENTRYPOINT')  return (width/2 - 45)
            if(d.type === 'EXIT')  return (width/2 - 30)
            if(d.type === 'NOP')  return (width/2 - 35)
            if(d.type === 'START_THREAD')  return (width/2 - 75)+px
            if(d.type === 'WAIT_FOR_THREAD')  return (width/2 -75)+px
            return (width/2 - 75)+px
        })
        .attr("y", (d, i) => {
            if(d.type === 'ENTRYPOINT')  return (d.level*90)+79;
            if(d.type === 'EXIT')  return (d.level*90)+79;
            if(d.type === 'START_THREAD')  return (d.level*90)+77;
            if(d.type === 'WAIT_FOR_THREAD')  return (d.level*90)+77;
            return  (d.level*90)+77
        })
        .attr("width", 24)
        .attr("height", 24);

        //TEXT
        let textG = svg.append("g").selectAll("g");
        textG.data(data)
        .enter()
            .append("text")
            .text( d => d.name.split('-').slice(1, -1).join(' '))
            .attr("text-anchor", "left")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                if(d.type === 'ENTRYPOINT')  return (width/2 - 10)
                if(d.type === 'EXIT')  return (width/2 +5)
                if(d.type === 'START_THREAD')  return (width/2 - 35)+px
                if(d.type === 'WAIT_FOR_THREAD')  return (width/2 -35)+px
                if(d.type === 'TASK')  return (width/2 -35)+px
                return (width/2)+px
            })
            .attr("y", (d) => {
                if(d.type === 'ENTRYPOINT')  return (d.level * 90) +95
                if(d.type === 'EXIT')  return (d.level * 90) +95
                if(d.type === 'TASK')  return (d.level * 90) +90
                return (d.level * 90) +92
            })
            .style("pointer-events", "none")

        
        // NOP LINES
        let lineG2 = svg.append("g").selectAll("g");
        lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d,i) => rArrow(width/2, (d.level * 90) +65 , 100, 48, 12)).attr('fill','none').attr('stroke','#B3B3B3')
        lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d) => lArrow(width/2, (d.level * 90) +65 , 100, 48, 12)).attr('fill','none').attr('stroke','#B3B3B3')
        lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d) => rbArrow(width/2, (d.level * 90) +65 , 100, 48, 12)).attr('fill','none').attr('stroke','#B3B3B3')
        lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d) => lbArrow(width/2, (d.level * 90) +65 , 100, 48, 12)).attr('fill','none').attr('stroke','#B3B3B3')

        // WAIT_FOR_THREAD
        lineG2.data(data.filter( d => d.type === "WAIT_FOR_THREAD"))
            .enter()
            .append("path")
            .attr("d", (d) => backArrow(width/2, (d.level * 90) +65 , 100, (d.level * 90)-(d.wlevel * 90), 12)).attr('fill','none').attr('stroke','#7F7AFF')

            

        //TEXT
        let textGN = svg.append("g").selectAll("g");
        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text( d => d.node.outgoingEdges[0].condition.comparator)
            .attr("text-anchor", "center")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", d => {
                return (width/2)-180
            })
            .attr("y", (d) => (d.level * 90) +150)
            .style("pointer-events", "none")

        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text( d => d.node.outgoingEdges[0].sinkNodeName)
            .attr("text-anchor", "center")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", d => {
                return (width/2)-180
            })
            .attr("y", (d) => (d.level * 90) +150 + 30)
            .style("pointer-events", "none")


        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text( d => d.node.outgoingEdges[1].condition.comparator)
            .attr("text-anchor", "center")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", d => {
                return (width/2)+120
            })
            .attr("y", (d) => (d.level * 90) +150)
            .style("pointer-events", "none")
        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text( d => d.node.outgoingEdges[1].sinkNodeName)
            .attr("text-anchor", "center")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", d => {
                return (width/2)+120
            })
            .attr("y", (d) => (d.level * 90) +150 +30)
            .style("pointer-events", "none")




        

    },[])


    const setD3 = useCallback((data:any[]) => {
        _d3 = d3.select("#visualizer")
        drawChart(data) 
    },[drawChart])


    useEffect( () => {
        setD3(data)
    },[data, setD3 ])

    useEffect( () => {
        return () => {
            _d3.select("svg").remove();
        }
    },[])
    return (
        <>
            <div id="visualizer"></div>
        </>
    )
}