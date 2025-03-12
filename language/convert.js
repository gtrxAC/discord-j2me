import stripJsonComments from 'strip-json-comments';
import fs from 'fs';

fs.readdirSync(".")
    .filter(f => f.endsWith('.jsonc'))
    .forEach(file => {
        const content = fs.readFileSync(file).toString();
        const minified = JSON.stringify(JSON.parse(stripJsonComments(content)));

        file = file.replace('.jsonc', '.json');

        const path = (file == "en.json" || file == "en-compact.json") ?
            `../res/${file}` :
            `../proxy/static/lang/${file}`;

        fs.writeFileSync(path, minified);
    })