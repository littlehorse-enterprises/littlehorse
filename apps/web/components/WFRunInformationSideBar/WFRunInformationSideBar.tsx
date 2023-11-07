import Image from 'next/image'
import Snippet from '../Snippet'

function WFRunInformationSideBar({
  toggleSideBar,
  setToggleSideBar,
  output,
  errorLog,
  language
}) {

  if (!toggleSideBar) {
    return null
  }

  return (<div className="flex-1">
    <div className="pop-up-wfrun-information">
      <div className="header" style={{
        cursor:'pointer'
      }}>
        <Image
          alt="chevron-right"
          height={18}
          onClick={(e) => setToggleSideBar(false)}
          src="/chevron_right.svg"
          width={11.1}
        />
        <h2 className="flex-1" style={{
          paddingBottom:'8px',
          paddingTop:'8px',
          fontSize:'20px'
        }} >{errorLog ? 'Exception log' : 'JSON / Array values'}</h2>

      </div>
      <Snippet language={language} value={output} />
    </div>
  </div>)
}

export default WFRunInformationSideBar
