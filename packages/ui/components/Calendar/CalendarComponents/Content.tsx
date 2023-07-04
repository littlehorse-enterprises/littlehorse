import * as React from "react";
import { useState, useEffect } from "react";
import { YearSelector } from "./YearSelector";
import moment from "moment";

export const Content = ({
  init,
  nextMonth,
  prevMonth,
  onApply,
  selectDay,
  updateSelectedH,
  updateSelectedM,
  updateEndSelectedH,
  updateEndSelectedM,
  selected,
  endSelected,
  type
}: {
  init: any;
  nextMonth: any;
  prevMonth: any;
  onApply: () => void;
  selectDay: any;
  updateSelectedH: any;
  updateSelectedM: any;
  updateEndSelectedH: any;
  updateEndSelectedM: any;
  selected: any;
  endSelected: any;
  type: any;
}) => {
  const [weeks, setWeeks] = useState<any[]>([]);
  const [busy, setBusy] = useState(true);
  const fillDays = (firstDay: moment.Moment, lastDay: moment.Moment) => {
    var dates: any = [];
    const month = moment(init).month();
    while (firstDay < lastDay) {
      dates.push({
        day: firstDay.format("DD"),
        selected: selected,
        endSelected: endSelected,
        date: firstDay.toDate(),
        otherMonth: firstDay.month() != month,
        inRange:
          selected &&
          endSelected &&
          firstDay > selected &&
          firstDay < endSelected,
        startRange:
          selected &&
          firstDay.format("YMMDD") === moment(selected).format("YMMDD"),
        endsRange:
          (selected &&
            !endSelected &&
            firstDay.format("YMMDD") === moment(selected).format("YMMDD")) ||
          (endSelected &&
            firstDay.format("YMMDD") === moment(endSelected).format("YMMDD")),
        today:
          firstDay.date() === moment().date() &&
          firstDay.month() === moment().month(),
        afterPresent: firstDay.format("YMMDD") > moment().format("YMMDD")
      });
      firstDay.add(1, "day");
    }
    return dates;
  };
  var enumerateWeeks = function () {
    const date = moment(init).startOf("month");
    let firstWeek = moment(init).startOf("month").week();
    const endWeek = moment(init).endOf("month").week();
    var dates: any = [];
    while (firstWeek <= endWeek) {
      let firstDay = date.clone().startOf("week");
      let lastDay = date.clone().endOf("week");
      dates.push(fillDays(firstDay, lastDay));
      date.add(1, "week");
      firstWeek++;
    }
    return dates;
  };
  const selectDayHandler = (day: any) => {
    if (day.afterPresent) return false;
    selectDay(day.date);
  };
  useEffect(() => {
    setWeeks(enumerateWeeks());
  }, [init, selected, endSelected]);

  return (
    <div
      style={{
        padding: "10px",
        backgroundColor: "#394150",
        borderRadius: "8px"
      }}
      className="flex"
    >
      <div
        style={{
          width: "287px",
          display: "block"
        }}
      >
        <YearSelector prevMonth={prevMonth} nextMonth={nextMonth} date={init} />
        <div className="flex flex-1 weeks">
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            S
          </div>
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            M
          </div>
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            T
          </div>
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            W
          </div>
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            T
          </div>
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            F
          </div>
          <div className="flex items-center justify-center flex-1 w-10 h-10 align-middle">
            S
          </div>
        </div>
        {weeks.map((week: any, ix: number) => (
          <div className="flex flex-1" key={ix}>
            {week.map((day: any) => (
              <div
                onClick={() => selectDayHandler(day)}
                style={{
                  fontSize: "14px",
                  lineHeight: "150%",
                  fontWeight: "700"
                }}
                className={`flex  flex-1 w-10 h-10 align-middle day 
            ${day.otherMonth ? "otherMonth" : ""}
            ${day.today ? "currentDay" : ""}
            ${day.endsRange ? "endsRange" : ""}
            ${day.startRange ? "startRange" : ""}
            ${day.inRange ? "inRange" : ""}
            ${day.afterPresent ? "afterPresent" : ""}
            `}
                key={day.day}
              >
                <div className="items-center justify-center">
                  {day.day} {day.today && <div className="dot"></div>}
                </div>
              </div>
            ))}
          </div>
        ))}
        {type === "DAYS_1" && (
          <div onClick={onApply} className="applyButton mt10">
            Apply range
          </div>
        )}
      </div>

      {type != "DAYS_1" && (
        <div className="timePickerCanvas">
          <div className="block">
            <div className="label">From</div>
            <div className="monthAndYear">
              <span className="day">{moment(selected).format("DD")}</span>
              <span className="month">{moment(selected).format("MMMM")}</span>
              <span className="year">{moment(selected).format("Y")}</span>
            </div>
            <div className="pickers">
              <select
                value={moment(selected).format("HH")}
                onChange={(e) => updateSelectedH(e.target.value)}
              >
                <option>00</option>
                <option>01</option>
                <option>02</option>
                <option>03</option>
                <option>04</option>
                <option>05</option>
                <option>06</option>
                <option>07</option>
                <option>08</option>
                <option>09</option>
                <option>10</option>
                <option>11</option>
                <option>13</option>
                <option>14</option>
                <option>15</option>
                <option>16</option>
                <option>17</option>
                <option>18</option>
                <option>19</option>
                <option>20</option>
                <option>21</option>
                <option>22</option>
                <option>23</option>
              </select>
              <div className="dots">:</div>
              <select
                className="bg-slate-700"
                disabled={type === "HOURS_2"}
                value={
                  type === "HOURS_2" ? "00" : moment(selected).format("mm")
                }
                onChange={(e) => updateSelectedM(e.target.value)}
              >
                <option>00</option>
                <option>05</option>
                <option>10</option>
                <option>15</option>
                <option>20</option>
                <option>25</option>
                <option>30</option>
                <option>35</option>
                <option>40</option>
                <option>45</option>
                <option>50</option>
                <option>55</option>
              </select>
            </div>
          </div>
          <div className="block">
            <div className="label">To</div>
            <div className="monthAndYear">
              <span className="day">
                {endSelected
                  ? moment(endSelected).format("DD")
                  : moment(selected).format("DD")}
              </span>
              <span className="month">
                {endSelected
                  ? moment(endSelected).format("MMMM")
                  : moment(selected).format("MMMM")}
              </span>
              <span className="year">
                {endSelected
                  ? moment(endSelected).format("Y")
                  : moment(selected).format("Y")}
              </span>
            </div>
            <div className="pickers">
              <select
                value={
                  endSelected
                    ? moment(endSelected).format("HH")
                    : moment(selected).format("HH")
                }
                onChange={(e) => updateEndSelectedH(e.target.value)}
              >
                <option>00</option>
                <option>01</option>
                <option>02</option>
                <option>03</option>
                <option>04</option>
                <option>05</option>
                <option>06</option>
                <option>07</option>
                <option>08</option>
                <option>09</option>
                <option>10</option>
                <option>11</option>
                <option>13</option>
                <option>14</option>
                <option>15</option>
                <option>16</option>
                <option>17</option>
                <option>18</option>
                <option>19</option>
                <option>20</option>
                <option>21</option>
                <option>22</option>
                <option>23</option>
              </select>
              <div className="dots">:</div>
              <select
                className="bg-slate-700"
                disabled={type === "HOURS_2"}
                value={
                  type === "HOURS_2"
                    ? "00"
                    : endSelected
                    ? moment(endSelected).format("mm")
                    : moment(selected).format("mm")
                }
                onChange={(e) => updateEndSelectedM(e.target.value)}
              >
                <option>00</option>
                <option>05</option>
                <option>10</option>
                <option>15</option>
                <option>20</option>
                <option>25</option>
                <option>30</option>
                <option>35</option>
                <option>40</option>
                <option>45</option>
                <option>50</option>
                <option>55</option>
              </select>
            </div>
          </div>
          <button className="applyButton " onClick={onApply}>
            Apply range
          </button>
        </div>
      )}
    </div>
  );
};
