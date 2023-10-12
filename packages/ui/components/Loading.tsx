interface Props {
    className?: string;
    style?: any;
}

const Loading = ({
    className, style
}: Props) => {
    return <div style={style}>Loading ...</div>
    // return (<div className={`flex grid content-center justify-center ${className || ''}`} style={style}>
    //     <div className="flex gap gap-2 items-center loading">
    //         Loading
    //         
    //         {/* <span className="sr-only">Loading...</span>  */}
    //     </div>
    // </div>);
}

export default Loading;
