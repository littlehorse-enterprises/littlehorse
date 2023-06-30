
import SyntaxHighlighter from 'react-syntax-highlighter';
//a11yDark
import { stackoverflowDark } from 'react-syntax-highlighter/dist/esm/styles/hljs';

interface props {
}

const Snippet = ({json}: props) => {
    
    return (<div className='frame'>
        <SyntaxHighlighter style={stackoverflowDark} language="json">{
            `${JSON.stringify(json, null, 2)}`
        }</SyntaxHighlighter>
    </div>);
}

export default Snippet;