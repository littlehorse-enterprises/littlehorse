import * as d3 from "d3";
import { useCallback, useEffect } from "react";
import moment from "moment"
import { NewScrollBar } from "./NewScrollBar";

const _WIDTH = 1360

const ttTemplate=`
<header>{HEADER}</header>
<main>
    <div class="title">Scheduled to Start</div>
    <div><div class="dot" style="background:#FDBB45; border:1px solid #EDEEF0"></div><label>Average:</label> <div class="value">{A}</div></div>
    <div><div class="dot" style="background:#FDBB45; border:1px solid #FFCC00"></div><label>Maximum:</label> <div class="value">{B}</div></div>
    <div class="title">Start to Finish</div>
    <div><div class="dot" style="background:#E6527F; border:1px solid #EDEEF0"></div><label>Average:</label> <div class="value">{C}</div></div>
    <div><div class="dot" style="background:#E6527F; border:1px solid #E6527F"></div><label>Maximum:</label> <div class="value">{D}</div></div>
</main>
`


// .range(["#B769E8", '#4D8FD6','#27CBB8', '#E6527F' ])
const updateToolTipContent = (data:any,template:string) => {
    return template
        .replace('{HEADER}',moment(data.windowStart).format('MMM DD, HH:00'))
        .replace('{A}',(data.scheduleToStartAvg ? data.scheduleToStartAvg : 0).toFixed(1)+'s' )
        .replace('{B}',(data.scheduleToStartMax ? data.scheduleToStartMax : 0).toFixed(1)+'s' )
        .replace('{C}',(data.startToCompleteAvg ? data.startToCompleteAvg : 0).toFixed(1)+'s' )
        .replace('{D}',(data.startToCompleteMax ? data.startToCompleteMax : 0).toFixed(1)+'s' )
}

const margin = {top: 10, right: 30, bottom: 60, left: 50},
    width = _WIDTH - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom;
const maxWBar = 76
const minYAxisValue = 8000
const gap = 8
const visibleWindows = Math.ceil((width)/(maxWBar+gap))
const ShadowLight_100 = "#3D4149"

let  datam:any[] = []
let  groups:any[] = []
let  _d3:d3.Selection<d3.BaseType, unknown, HTMLElement, any>
let svg:d3.Selection<SVGGElement, unknown, HTMLElement, any>

interface Props {
    data:any[]
    type:string
}

export const LatencyTaskChart = ({data, type}:Props) => {


    const onMoveScroll = (ix:number) => {
        if(!svg) return null
        const num = (ix+1)
        
            svg.selectAll(".line-start")
            .data(datam)
            .attr("x1", (_d, ix) => {
                let val = (((ix-(num-1))) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            .attr("x2", (_d, ix) => {
                let val = (((ix-(num-1))) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            svg.selectAll(".line-complete")
            .data(groups)
            .attr("x1", (_d, ix) => {
                let val = (((ix-(num-1))) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )
            .attr("x2", (_d, ix) => {
                let val = (((ix-(num-1))) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )

            svg.selectAll(".dot-start-max")
            .data(datam)
            .attr("cx", (_d, ix) => {
                let val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )

            svg.selectAll(".dot-start-avg")
            .data(datam)
            .attr("cx", (_d, ix) => {
                let val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            svg.selectAll(".dot-complete-max")
            .data(datam)
            .attr("cx", (_d, ix) => {
                let val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )
            svg.selectAll(".dot-complete-avg")
            .data(datam)
            .attr("cx", (_d, ix) => {
                let val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )


            svg.select("g.xa").selectAll('text')
            .data(groups)
            .attr("x", (_d, ix) => {
                let val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
            svg.selectAll(".hours")
            .data(groups)
            .attr("x", (_d, ix) => {
                let val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
            svg.selectAll(".shadow")
            .data(datam)
            .attr("x", (_d, i)  =>  {
                let val = ((maxWBar+gap) * (i-(num-1))) 
                return val < 0 ? -800 : val
            })
    }



    const drawChart = useCallback((data:any[], type:string) => {
       

        datam = data.map(d => ({
            a:+d.data.totalScheduled || 0,
            b:+d.data.totalStarted || 0,
            c:+d.data.totalCompleted || 0,
            d:+d.data.totalErrored || 0,
            windowStart:d.label,
            scheduleToStartMax: +d.data.scheduleToStartMax ? +d.data.scheduleToStartMax/1000 : 0,
            scheduleToStartAvg: +d.data.scheduleToStartAvg ? +d.data.scheduleToStartAvg/1000 : 0,
            startToCompleteMax: +d.data.startToCompleteMax ? +d.data.startToCompleteMax/1000 : 0,
            startToCompleteAvg: +d.data.startToCompleteAvg ? +d.data.startToCompleteAvg/1000 : 0
        }))

        
        groups = data.map(d => ({
            l1:moment(d.label).format(`MMM DD${type==='DAYS_1' ? '' : ','}`),
            l2:moment(d.label).format(`HH:${type==='HOURS_2' ? '00' : 'mm'}`),
        }))


        let he = datam.map(values => Math.max(values.scheduleToStartMax, values.startToCompleteMax))
        let maxH = Math.max(...he) > 9 ? Math.max(...he)+10 : 10
        maxH = maxH * 1.1
        console.log('maxH',maxH)

        // const groups = ["1","2","3","4"]
    

        // append the svg object to the body of the page
        _d3.select("svg").remove();
        console.log('herr',_d3)
        svg = _d3.append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform",
            "translate(" + margin.left + "," + margin.top + ")");

        // Add Y axis
        var y = d3.scaleLinear()
        .domain([0, maxH])
        .range([ height, 0 ]);
        svg.append("g")
        .call(d3.axisLeft(y));



            svg.append("g")
            .selectAll("g")
            .data(datam)
            .enter().append("rect").classed('shadow', true)
            .style("opacity", 0)
            .attr("fill", ShadowLight_100)
            .attr("x", (_d, i)  =>  {
                let val = ((maxWBar+gap) * (i)) 
                return val < 10 ? -800 : val
            })
            .attr("y", 0 )
            .attr("width", maxWBar)
            .attr("height", height)

            let dotg = svg.append("g").classed('dots', true)
            .style("pointer-events", "none")
            .selectAll("g")
            


            // xA.append("line")
            // .style("stroke", "white")
            // .style("stroke-width", 1)
            // .attr("x1", 0)
            // .attr("y1", height+1)
            // .attr("x2", width+(gap*visibleWindows))
            // .attr("y2", height+1);
            dotg.data(datam)
            .enter().append("line").classed('line-start', true)
            .attr("stroke", "#FFCC00")
            .style("stroke-dasharray", ("6, 4")) 
            .attr("x1", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            .attr("x2", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            .attr("y1", d => y(d.scheduleToStartMax) )
            .attr("y2", d => y(d.scheduleToStartAvg) )

            dotg.data(datam)
            .enter().append("line").classed('line-complete', true)
            .attr("stroke", "#E6527F")
            .style("stroke-dasharray", ("6, 4")) 
            .attr("x1", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )
            .attr("x2", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )
            .attr("y1", d => y(d.startToCompleteMax) )
            .attr("y2", d => y(d.startToCompleteAvg) )
            
            dotg.data(datam)
            .enter().append("circle").classed('dot-start-max', true)
            .attr("fill", "#FDBB45")
            .attr("stroke", "#FFCC00")
            .attr("cx", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            .attr("cy", d => y(d.scheduleToStartMax) )
            .attr("r",6)

            dotg.data(datam)
            .enter().append("circle").classed('dot-start-avg', true)
            .attr("fill", "#FDBB45")
            .attr("stroke", "#EDEEF0")
            .attr("cx", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)-16
                return val < 0 ? -800 : val
            } )
            .attr("cy", d => y(d.scheduleToStartAvg) )
            .attr("r",6)

            dotg.data(datam)
            .enter().append("circle").classed('dot-complete-max', true)
            .attr("fill", "#E6527F")
            .attr("cx", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )
            .attr("cy", d => y(d.startToCompleteMax) )
            .attr("r",6)

            dotg.data(datam)
            .enter().append("circle").classed('dot-complete-avg', true)
            .attr("fill", "#E6527F")
            .attr("stroke", "#EDEEF0")
            .attr("cx", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)+16
                return val < 0 ? -800 : val
            } )
            .attr("cy", d => y(d.startToCompleteAvg) )
            .attr("r",6)

        const xA = svg.append("g").classed('xa',true)
        xA.append("line")
            .style("stroke", "white")
            .style("stroke-width", 1)
            .attr("x1", 0)
            .attr("y1", height+1)
            .attr("x2", width+(gap*visibleWindows))
            .attr("y2", height+1);
        svg.select(".xa").selectAll("line")
        .data(groups)
        .enter().append("line")
            .style("stroke", "white")
            .style("stroke-width", 1)
            .attr("x1", (d,ix) => (ix * (maxWBar + gap))-(gap/2) )
            .attr("y1", height+1)
            .attr("x2", (_d, ix) => (ix * (maxWBar + gap))-(gap/2) )
            .attr("y2", height+5);
        svg.select(".xa").selectAll("text")
            .data(groups)
            .enter().append("text")
            .text( d => d.l1 )
            .attr("text-anchor", "middle")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
            .attr("y", height+15);
        
        if(type != 'DAYS_1'){
            svg.select(".xa").append("g").selectAll("text")
            .data(groups)
            .enter().append("text").classed('hours',true)
            .text( d => d.l2 )
            .attr("text-anchor", "middle")
            .style('fill', "white")
            .style('font-size', "10px")
            .attr("x", (_d, ix) => {
                let val = ((ix) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
            .attr("y", height+30); 
        } 
        
        d3.select("#latency-chart-tooltip").html(updateToolTipContent({},ttTemplate))



        svg.selectAll(".shadow")
        .on("mouseover", function(d,i) {
            d3.select(this).transition()
                    .ease(d3.easeLinear)
                    .duration(150)
                    .style("opacity", 1);
            d3.select("#latency-chart-tooltip").style("visibility", "visible");
        })
        .on("mousemove", (e,d) => {
            d3.select("#latency-chart-tooltip").html(updateToolTipContent(d,ttTemplate))
            .style('top',e.layerY+"px")
            .style('left',e.offsetX+"px")

        })
        .on("mouseout", function(e) {
            d3.select(this).transition()
                .ease(d3.easeLinear)
                .duration(150)
                .style("opacity", 0);
            d3.select("#latency-chart-tooltip").style("visibility", "hidden");
        })

    },[])
 
    const setD3 = useCallback((data:any[], type:string) => {
        _d3 = d3.select("#latency-chart")
        drawChart(data,type) 
    },[drawChart])

    useEffect( () => {
        setD3(data, type)
    },[data, setD3, type ])
    
    useEffect( () => {
        return () => {
            _d3.select("svg").remove();
        }
    },[])
    return <>
        <div id="latency-chart" className="relative">
            <div className="mcToolTip" id="latency-chart-tooltip"></div>
        </div>
        <div style={{ paddingLeft:"10px", paddingRight:"10px" }}>
            <NewScrollBar width={_WIDTH-20} windows={(data.length - visibleWindows) < 0 ? 1 : (data.length - visibleWindows)} onChange={onMoveScroll} />
        </div>
    </>
}