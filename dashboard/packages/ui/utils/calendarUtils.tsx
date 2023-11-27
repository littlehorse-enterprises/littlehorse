import moment from 'moment'

export const getFirstDate = (date:Date, type:string, windows:number) => {
  const dt = moment(date)
  if (type==='DAYS_1') {return dt.subtract(windows,'days').toDate()}
  if (type==='HOURS_2') {return dt.subtract(windows*2,'hours').toDate()}
  if (type==='MINUTES_5') {return dt.subtract(windows*5,'minutes').toDate()}
  return date
}
