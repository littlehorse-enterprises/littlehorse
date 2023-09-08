"use client"
import { useCallback, useEffect } from "react";
import * as d3 from "d3";
import * as ReactDOMServer from 'react-dom/server';
import { TaskNode } from "./TaskNode";


const conditions = {
    EQUALS:'=',
    NOT_EQUALS:'!=',
    GREATER_THAN: '>',
    GREATER_THAN_EQ: '>=',
    LESS_THAN_EQ: '<=',
    LESS_THAN: '<'
}
let  _d3:d3.Selection<d3.BaseType, unknown, HTMLElement, any>
// let svg:d3.Selection<SVGGElement, unknown, HTMLElement, any>

const boxHeight = 300;
const width=936;
const height=900;

const conditionsRender = (cond:any) => {
    console.log('cond',cond)
    let left=``;
    if(cond?.condition?.left){
        left=`${cond.condition.left.variableName}${cond.condition.left.jsonPath ? `.jsonPath(${cond.condition.left.jsonPath})` : ''}`;
    }
    let right=''
    if(cond?.condition?.right){
        if( cond.condition.right.literalValue.type==='BOOL') right = cond.condition.right.literalValue.bool
        if( cond.condition.right.literalValue.type==='INT') right = cond.condition.right.literalValue.int
        if( cond.condition.right.literalValue.type==='STR') right = cond.condition.right.literalValue.str
    }
    let comparator=''
    if(cond?.condition?.comparator){
        comparator=conditions[cond.condition.comparator];
    }
    return `${left} ${comparator} ${right}`
}
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

export const WfRunVisualizerChart = ({data, onClick, run}:{data:any, onClick:(n:any) => void, run:any}) => {
    const clickHandler = (_p:any, d:any) => {
        // console.log(_p,d)
        onClick(d.name)
        // console.log(d.name)
        // console.log('run',run)
        _d3.select('.selected-node').classed('selected-node',false)
        _d3.select('.c'+d.name).classed('selected-node',true)
       
    }
    
    const minHeight = 862
    const drawChart = useCallback((data:any[], run:any) => {
        _d3.select("svg").remove();
        let svg = _d3.append("svg")
            .attr("width",width)
            .attr("height",(Math.max(...data.map(d => d.level))*120)+100 < minHeight ? minHeight : (Math.max(...data.map(d => d.level))*120)+100);
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
                // if(d.level === 1)  return (d.level * 110)
                return (d.level * 110)
            })
            .attr("x2", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y2", (d) => ((d.level) * 110) +55  )

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
            .attr("y1", (d) => ((d.level) * 110) +55 -3)
            .attr("x2", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y2", (d) => ((d.level) * 110) +55 )
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
            .attr("y1", (d) => ((d.level) * 110) +55 -3)
            .attr("x2", d => {
                let px = 0
                if(d.px === 'left') px = -150
                if(d.px === 'right') px = 150
                return (width/2)+px
            })
            .attr("y2", (d) => ((d.level) * 110) +55)
        

   

        
        // NOP LINES
        let lineG2 = svg.append("g").selectAll("g");
        lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d,i) => rArrow((width/2)-45, (d.level * 110) +85 , 130, 48, 12)).attr('fill','none').attr('stroke','#B3B3B3')

        lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d) => lArrow((width/2)+45, (d.level * 110) +85 , 130, 48, 12)).attr('fill','none').attr('stroke','#B3B3B3')


        lineG2.data(data.filter( d => d.cNOP))
            .enter()
            .append("path")
            .attr("class", d => d.name)
            .attr("d", (d) => {
                // console.log('CNOP', data.find( dd => dd.name === d.cNOP))
                const cnopl =  data.find( dd => dd.name === d.cNOP).level
                // console.log((cnopl-d.level)*66)
                const condh = d.type === 'NOP' ? 100 : 0
                if(d.px === 'left'){
                    return lbArrow((width/2)+15, ((d.level-1) * 110)-85 +condh - ((cnopl-d.level)*400), 110, ((cnopl-d.level)*81.5)+54, 12)
                }
                return rbArrow((width/2)-15, ((d.level-1) * 110)-85 +condh - ((cnopl-d.level)*400), 110, ((cnopl-d.level)*81.5)+54, 12)

                //function backArrow(x, y, width, height, radius) {
            })
            .attr('fill','none').attr('stroke','#B3B3B3')

        // lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        //     .enter()
        //     .append("path")
        //     .attr("class", d => d.name)
        //     .attr("d", (d) => rbArrow(width/2, (d.level * 130) +50 , 100, 52, 15)).attr('fill','none').attr('stroke','#B3B3B3')
        // lineG2.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        //     .enter()
        //     .append("path")
        //     .attr("class", d => d.name)
        //     .attr("d", (d) => lbArrow(width/2, (d.level * 130) +50 , 100, 51, 12)).attr('fill','none').attr('stroke','#B3B3B3')

        // WAIT_FOR_THREAD
        lineG2.data(data.filter( d => d.type === "WAIT_FOR_THREAD"))
            .enter()
            .append("path")
            .attr("d", (d) => backArrow(width/2, (d.level * 110) +65 , 110+(d.px === 'left' ? -110 : (d.px === 'right' ? 150 : 0)), ((d.level * 110))-((d.wlevel * 110)+5), 12)).attr('fill','none').attr('stroke','#7F7AFF')

            

        //TEXT
        let textGN = svg.append("g").selectAll("g");
        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text('CONDITION')
            .attr("text-anchor", "middle")
            .style('fill', "#9098A0")
            .style('font-size', "10px")
            .style('font-family', "Inter")
            .style('font-weight', "400")
            .attr("x", d => {
                return (width/2)-150
            })
            .attr("y", (d) => (d.level * 110) +175)
            .style("pointer-events", "none")

        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text( d => conditionsRender(d.node.outgoingEdges[0]))
            .attr("text-anchor", "middle")
            .style('font-family', "Inter")
            .style('font-weight', "400")
            .style('fill', "white")
            .style('font-size', "14px")
            .attr("x", d => {
                return (width/2)-150
            })
            .attr("y", (d) => (d.level * 110) +175 + 20)
            .style("pointer-events", "none")


        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text('CONDITION')
            .attr("text-anchor", "middle")
            .style('fill', "#9098A0")
            .style('font-size', "10px")
            .style('font-family', "Inter")
            .style('font-weight', "400")
            .attr("x", d => {
                return (width/2)+155
            })
            .attr("y", (d) => (d.level * 110) +175)
            .style("pointer-events", "none")
        textGN.data(data.filter( d => d.type === "NOP" && d.node.outgoingEdges.length > 1))
        .enter()
            .append("text")
            .text( d => conditionsRender(d.node.outgoingEdges[1]))
            .attr("text-anchor", "middle")
            .style('font-family', "Inter")
            .style('font-weight', "400")
            .style('fill', "white")
            .style('font-size', "14px")
            .attr("x", d => {
                return (width/2)+155
            })
            .attr("y", (d) => (d.level * 110) +175 +20)
            .style("pointer-events", "none")



                 //NODES
        let nodes = svg.append("g").selectAll("g");
        nodes.data(data)
        .enter()
            .append("foreignObject")
                .attr('width', 400)
                .attr('x', (d,i) => ( (width/2)-200)+(d.px === 'left' ? -150 : (d.px === 'right' ? 150 : 0)) )
                .attr('height',110)
                .attr('y', (d,i) => (110*d.level)+50)
            .append('xhtml:div')
                .attr('style', 'width:100%; height:100%; display:flex;')
                .html((d:any) => ReactDOMServer.renderToString(<TaskNode d={d} run={run} />))
                .select('.node').on("click",clickHandler)
        

    },[])


    const setD3 = useCallback((data:any[], run:any) => {
        _d3 = d3.select("#visualizer")
        drawChart(data, run) 
    },[drawChart])


    useEffect( () => {
        if(run) setD3(data, run)
    },[data, setD3, run ])

    useEffect( () => {
        return () => {
            if(_d3) _d3.select("svg").remove();
        }
    },[])
    return (
        <>
            <div id="visualizer"></div>
        </>
    )
}