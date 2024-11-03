import stripJsonComments from 'strip-json-comments';
import fs from 'fs';

// fs.readdirSync(".")
//     .filter(f => f.endsWith('.jsonc'))
//     .forEach(file => {
//         const content = fs.readFileSync(file).toString();
//         const minified = JSON.stringify(JSON.parse(stripJsonComments(content)));
//         fs.writeFileSync(`../res/${file.replace('.jsonc', '.json')}`, minified);
//     })

const enStrings = JSON.parse(stripJsonComments(fs.readFileSync("en.jsonc").toString()));

fs.readdirSync(".")
    .filter(f => f.endsWith('.jsonc'))
    .forEach(file => {
        const content = fs.readFileSync(file).toString();
        const parsed = JSON.parse(stripJsonComments(content));
        const minified = parsed.map((str, i) => {
            if (str == null) return 0;
            if (file != "en.jsonc" && str == enStrings[i]) return 0;
            return str;
        })
        let truncatePos = 0;
        minified.forEach((str, i) => {
            if (str != 0) truncatePos = i;
        })
        const truncated = minified.slice(0, truncatePos + 1);
        fs.writeFileSync(`../res/${file.replace('.jsonc', '.json')}`, JSON.stringify(truncated));
    })