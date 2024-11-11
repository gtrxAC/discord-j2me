import stripJsonComments from 'strip-json-comments';
import fs from 'fs';

fs.readdirSync(".")
    .filter(f => f.endsWith('.jsonc'))
    .forEach(file => {
        const content = fs.readFileSync(file).toString();
        const minified = JSON.stringify(JSON.parse(stripJsonComments(content)));

        const path = (file == "en.jsonc") ?
            "../res/en.json" :
            `../proxy/static/lang/${file.replace('.jsonc', '.json')}`;

        fs.writeFileSync(path, minified);
    })