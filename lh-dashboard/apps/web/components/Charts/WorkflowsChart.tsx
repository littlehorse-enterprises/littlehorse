/*
 eslint '@typescript-eslint/no-shadow': 'off' -- disabling as a re work is needed to make it pass
*/

import * as d3 from 'd3'
import { useCallback, useEffect } from 'react'
import moment from 'moment'
import { NewScrollBar } from './NewScrollBar'

const _WIDTH = 1360

const ttTemplate=`
    <header>{HEADER}</header>
    <main>
        <div><div class="dot" style="background:#4D8FD6"></div><label>Started:</label> <div class="value">{B}</div></div>
        <div><div class="dot" style="background:#27CBB8"></div><label>Completed:</label> <div class="value">{C}</div></div>
        <div><div class="dot" style="background:#E6527F"></div><label>Failed:</label> <div class="value">{D}</div></div>
    </main>
`

// .range(["#B769E8", '#4D8FD6','#27CBB8', '#E6527F' ])
const updateToolTipContent = (data:any,template:string) => {
    return template
        .replace('{HEADER}',moment(data.windowStart).format('MMM DD, HH:00'))
        .replace('{A}',data.a)
        .replace('{B}',data.b)
        .replace('{C}',data.c)
        .replace('{D}',data.d)
}

const margin = { top: 10, right: 30, bottom: 60, left: 50 },
    width = _WIDTH - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom
const maxWBar = 74
const gap = 8
const minYAxisValue = 11
const visibleWindows = Math.ceil((width)/(maxWBar+gap))
const ShadowLight100 = '#3D4149'

let  _d3:d3.Selection<d3.BaseType, unknown, HTMLElement, any>
let svg:d3.Selection<SVGGElement, unknown, HTMLElement, any>
let  datam:any[] = []
let  stack:any
let  groups:any[] = []

interface WorkflowsChartProps {
    data:any[]
    type:string
}

export function WorkflowsChart({ data, type }:WorkflowsChartProps) {

    
    const onMoveScroll = (ix:number) => {
        if (!svg) {return null}
        // console.log('ix',ix)
        // d3.select(".rangeInput")
        // .property("value",ix+1);

        const num = (ix+1)
        // console.log('num', num)
        svg.selectAll('g.bar')
            .data(stack)
            .selectAll('rect')
            .data((d:any) => d )
            .attr('x',(_d, ix)  =>  {
                const val = ((maxWBar+gap) * (ix-(num-1))) 
                return val < 0 ? -800 : val
            })

        svg.select('g.xa').selectAll('text')
            .data(groups)
            .attr('x', (_d, ix) => {
                const val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
        svg.selectAll('.hours')
            .data(groups)
            .attr('x', (_d, ix) => {
                const val = ((ix-(num-1)) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
        svg.selectAll('.shadow')
            .data(datam)
            .attr('x', (_d, i)  =>  {
                const val = ((maxWBar+gap) * (i-(num-1))) 
                return val < 0 ? -800 : val
            })

    }
    // set the dimensions and margins of the graph
    
    // console.log('visibleWindows', visibleWindows)



    // const firstWindowDt = lastWindowStart.clone().subtract(windows * 2,'hours')
    // console.log('lastWindowStart',lastWindowStart.format())
    // console.log('firstWindowDt',firstWindowDt.format())

    const drawChart = useCallback((data:any[], type:string) => {
       
        datam = data.map(d => ({
            a:Number(d.data.totalScheduled) || 0,
            b:Number(d.data.totalStarted) || 0,
            c:Number(d.data.totalCompleted) || 0,
            d:Number(d.data.totalErrored) || 0,
            windowStart:d.label
        }))
        // console.log('values',values)
        // var data = values.reverse();

        // data = data.slice(-visibleWindows)

        // let  groups = dates.reverse()
        
        // let  groups = dates.reverse()
        groups = data.map(d => ({
            l1:moment(d.label).format(`MMM DD${type==='DAYS_1' ? '' : ','}`),
            l2:moment(d.label).format(`HH:${type==='HOURS_2' ? '00' : 'mm'}`),
        }))
        // console.log(datam)
        // console.log(groups)
        // groups = groups.slice(-visibleWindows)

        const he = datam.map((values:any) => values.a +values.b + values.c + values.d)
        const maxH = Math.max(...he) > (minYAxisValue*.9) ? Math.max(...he)+(minYAxisValue*.1) : minYAxisValue

        // const groups = ["1","2","3","4"]
        
        // d3.select(".rangeInput")
        // .property("min", 1)
        // .property("max", windows-visibleWindows+1)
        // // .property("value",windows-visibleWindows+1);
        // .property("value",1);

        // append the svg object to the body of the page
        _d3.select('svg').remove()
        svg = _d3.append('svg')
            .attr('width', width + margin.left + margin.right)
            .attr('height', height + margin.top + margin.bottom)
            .append('g')
            .attr('transform',
                `translate(${margin.left},${margin.top})`)

        // Add Y axis
        const y = d3.scaleLinear()
            .domain([ 0, maxH ])
            .range([ height, 0 ])
        svg.append('g')
            .call(d3.axisLeft(y).ticks(6))

        d3.select('.domain').remove()
        // d3.selectAll('.tick').attr('transform','traslate(0)')
        d3.selectAll('.tick').selectAll('line').remove()
        d3.selectAll('.tick').selectAll('text')
            .attr('fill','#9098A0')
            .style('font-size', '12px')
            .style('font-family', 'Inter')
            .attr('dy','-0.5em')
            .attr('x','-4em')
            .attr('text-anchor','start')
        d3.selectAll('.tick').append('line')
            .attr('x1','-4em')
            .attr('x2',width+25).attr('stroke','#3D4149')
        // .call(d3.axisLeft(y).tickSize(width))

        // X axis

        // const x = d3.scaleBand()
        // .domain(groups)
        // .range([0, width])
        // .padding(gap/100)

        // const x = d3.scaleLinear()
        // .domain(groups)
        // .range([0, width])



        // color palette = one color per subgroup
        const color = d3.scaleOrdinal()
            .domain([ 'a', 'b', 'c', 'd' ])
            .range([ '#B769E8', '#4D8FD6','#27CBB8', '#E6527F' ])

        const stackedData = d3.stack()
            .keys([ 'a', 'b', 'c', 'd' ])
            .order(d3.stackOrderNone)
            .offset(d3.stackOffsetNone)

        stack = stackedData(datam)

        svg.append('g')
            .selectAll('g')
            .data(datam)
            .enter().append('rect').classed('shadow', true)
            .style('opacity', 0)
            .attr('fill', ShadowLight100)
            .attr('x', (_d, i)  =>  {
                const val = ((maxWBar+gap) * (i)) 
                return val < 0 ? -800 : val
            })
            .attr('y', 0 )
            .attr('width', maxWBar)
            .attr('height', height)

        svg.append('g').classed('bars', true)
            .style('pointer-events', 'none')
            .selectAll('g')
            .data(stack)
            .enter().append('g').classed('bar', true)
            .style('pointer-events', 'none')
            .attr('fill', (d:any) => String(color(d.key)) )
            .selectAll('rect')

        // enter a second time = loop subgroup per subgroup to add all rectangles
            .data((d:any) => d )
            .enter().append('rect')
        // .attr("x", (_d, i)  =>  ((maxWBar+gap) * (i-(windows-visibleWindows))) || 0 )
            .attr('x',(_d, ix)  =>  {
                const val = ((maxWBar+gap) * (ix)) 
                return val < 0 ? -800 : val
            })
            .attr('y', (d:any) => y(d[1]) )
            .attr('width', maxWBar)
            .attr('height', (d:any) => y(d[0]) - y(d[1]) )

        // const xA = svg.append('g').classed('xa',true)
        // xA.append("line")
        //     .style("stroke", "white")
        //     .style("stroke-width", 1)
        //     .attr("x1", 0)
        //     .attr("y1", height+1)
        //     .attr("x2", width+(gap*visibleWindows))
        //     .attr("y2", height+1);
        // svg.select(".xa").selectAll("line")
        // .data(groups)
        // .enter().append("line")
        //     .style("stroke", "white")
        //     .style("stroke-width", 1)
        //     .attr("x1", (d,ix) => (ix * (maxWBar + gap))-(gap/2) )
        //     .attr("y1", height+1)
        //     .attr("x2", (_d, ix) => (ix * (maxWBar + gap))-(gap/2) )
        //     .attr("y2", height+5);
        svg.select('.xa').selectAll('text')
            .data(groups)
            .enter().append('text')
            .text( d => d.l1 )
            .attr('text-anchor', 'middle')
            .style('fill', '#9098A0')
            .style('font-size', '12px')
            .style('font-family', 'Inter')
            .attr('x', (_d, ix) => {
                const val = ((ix) * (maxWBar + gap))+(maxWBar/2)
                return val < 0 ? -800 : val
            } )
            .attr('y', height+18)

        if (type !== 'DAYS_1'){
            svg.select('.xa').append('g').selectAll('text')
                .data(groups)
                .enter().append('text').classed('hours',true)
                .text( d => d.l2 )
                .attr('text-anchor', 'middle')
                .style('fill', 'white')
                .style('font-size', '12px')
                .style('font-family', 'Inter')
                .attr('x', (_d, ix) => {
                    const val = ((ix) * (maxWBar + gap))+(maxWBar/2)
                    return val < 0 ? -800 : val
                } )
                .attr('y', height+35)   
        }

        
        d3.select('#workflows-chart-tooltip').html(updateToolTipContent({},ttTemplate))



        svg.selectAll('.shadow')
            .on('mouseover', () => {
                d3.select(this).transition()
                    .ease(d3.easeLinear)
                    .duration(150)
                    .style('opacity', 1)
                d3.select('#workflows-chart-tooltip').style('visibility', 'visible')
            })
            .on('mousemove', (e,d) => {
                d3.select('#workflows-chart-tooltip').html(updateToolTipContent(d,ttTemplate))
                    .style('top',`${e.layerY+10}px`)
                    .style('left',() => {
                        if (e.layerX > width){
                            return `${e.layerX-160}px`
                        }
                        return `${e.layerX+10}px`
                    })

            })
            .on('mouseout', () => {
                d3.select(this).transition()
                    .ease(d3.easeLinear)
                    .duration(150)
                    .style('opacity', 0)
                d3.select('#workflows-chart-tooltip').style('visibility', 'hidden')
            })


    },[])
 
    const setD3 = useCallback((data:any[], type:string) => {
        _d3 = d3.select('#workflows-chart')
        drawChart(data,type) 
    },[ drawChart ])

    useEffect( () => {
        setD3(data, type)
    },[ data, setD3, type ])
    
    useEffect( () => {
        return () => {
            _d3.select('svg').remove()
        }
    },[])
    
    return <>
        <div className="relative select-none" id="workflows-chart">
            <div className="mcToolTip" id="workflows-chart-tooltip" />
        </div>
        <div style={{ paddingLeft:'10px', paddingRight:'10px' }}>
            <NewScrollBar onChange={onMoveScroll} width={_WIDTH-20} windows={(data.length - visibleWindows) < 0 ? 1 : (data.length - visibleWindows)} />
        </div>
    </>
}