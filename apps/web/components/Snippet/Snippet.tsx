
import { useEffect, useState } from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
//a11yDark
import { stackoverflowDark } from 'react-syntax-highlighter/dist/esm/styles/hljs';

const Snippet = ({value, language='json'}) => {
    
    const [snippetLanguage, setSnippetLanguage] = useState('json');

    useEffect(() => {
        if (language === 'int' ||
            language === 'str') {
                setSnippetLanguage('javascript');
            }
        else {
            setSnippetLanguage('json')
        }
    }, [language])

    return (<div className='frame snippet'>
        <SyntaxHighlighter style={stackoverflowDark} language={snippetLanguage}>
            {snippetLanguage === 'json' ? JSON.stringify(value, null, 2) : value}
        </SyntaxHighlighter>
    </div>);
}

export default Snippet;