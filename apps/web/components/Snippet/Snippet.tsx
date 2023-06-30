
import SyntaxHighlighter from 'react-syntax-highlighter';
//a11yDark
import { stackoverflowDark } from 'react-syntax-highlighter/dist/esm/styles/hljs';

const Snippet = ({value, language='json'}) => {
    
    return (<div className='frame snippet'>
        <SyntaxHighlighter style={stackoverflowDark} language={language}>{
            `${language === 'json' && JSON.stringify(value, null, 2)}`
        }</SyntaxHighlighter>
    </div>);
}

export default Snippet;