import Image from "next/image";
import Snippet from "../Snippet";

const WFRunInformationSideBar = ({
    toggleSideBar,
    setToggleSideBar,
    output,
    errorLog,
    language
}) => {

    if (!toggleSideBar) {
        return null;
    }

    return (<div className="flex-1">
        <div className="pop-up-wfrun-information">
            <div className="header">
                <Image
                    src="/chevron_right.svg"
                    onClick={(e) => setToggleSideBar(false)}
                    width={11.1}
                    height={18}
                    alt="chevron-right"
                />
                <h2 className="flex-1" style={{
                    paddingBottom:'8px',
                    paddingTop:'8px',
                    fontSize:"20px"
                }} >{errorLog ? 'Exception log' : 'JSON / Array values'}</h2>

            </div>
            <Snippet value={output} language={language} />
        </div>
    </div>);
}

export default WFRunInformationSideBar;
