import stripJsonComments from 'strip-json-comments';
import fs from 'fs';

fs.readdirSync(".")
    .filter(f => f.endsWith('.jsonc'))
    .forEach(file => {
        const content = fs.readFileSync(file).toString();
        const minified = JSON.stringify(JSON.parse(stripJsonComments(content)));
        
        fs.writeFileSync(`../res/${file.replace('.jsonc', '.json')}`, minified);
    })