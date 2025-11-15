const fs = require('fs');
const crypto = require("crypto");
const AdmZip = require("adm-zip");

const privateKey = fs.readFileSync("./sdk/exp.pem", "utf8");

function createJadFromJar(path, outPath, jarUrl, infoUrl, sign) {
    const zip = new AdmZip(path);

    const mfEntry = zip.getEntries().find(e => e.entryName.toLowerCase() == "meta-inf/manifest.mf");
    if (!mfEntry) throw new Error("The JAR file does not have a manifest.");

    const mf = parseJad(mfEntry.getData().toString());
    const jar = fs.readFileSync(path);

    mf.set("MIDlet-Jar-Size", jar.length);
    mf.set("MIDlet-Jar-URL", jarUrl);
    mf.set("MIDlet-Info-URL", infoUrl);

    if (sign) {
        const signer = crypto.createSign("RSA-SHA1");
        signer.update(jar);
        signer.end();

        const signature = signer
            .sign(privateKey)
            .toString("base64")
            .match(/.{1,64}/g)
            .join("");

        mf.set("MIDlet-Jar-RSA-SHA1", signature);

        // hardcoded base64 of darkman cert
        mf.set("MIDlet-Certificate-1-1","MIIB7jCCAVcCBEWxvN0wDQYJKoZIhvcNAQEEBQAwPTELMAkGA1UEBhMCUlUxDTALBgNVBAoTBG5vbmUxDTALBgNVBAsTBG5vbmUxEDAOBgNVBAMTB0RhcmttYW4wIBcNMDcwMTIwMDY1NTI1WhgPMjA3NTA3MDIwNjU1MjVaMD0xCzAJBgNVBAYTAlJVMQ0wCwYDVQQKEwRub25lMQ0wCwYDVQQLEwRub25lMRAwDgYDVQQDEwdEYXJrbWFuMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYLFUb8RYT89sbTAEE14ApYFI8PVpnxXGgLuE8V6+XGQu4q5MtYwmip8EMm/STLXb73gQmDnQUpwBKzTScXLQDA4n9lLni4yl29/+X5Y0rIA6tlPmK3p9wpt0t9j/rWEYF4zFsiMTNobGHZOK/MAxOM+wPICRW8DFLQ/rYcNjcpQIDAQABMA0GCSqGSIb3DQEBBAUAA4GBAG1fDNKSjvcvvi20AREsMT80iJdO/YXqcVrUsYrQVaZL3scsA+EVKi7Dv76c8oqjxxiueOnn+fTTmlkAOO5ngzZhk13m3tcNxwUs0A/1GBMbVDbYlEc6vEYQde9x+07iyrxmtwj6qVR1r3zTEy2wS52poVmCkcrPXY0wylKesjFP");
    }

    fs.writeFileSync(outPath, createJad(mf));
}

function parseJad(jad) {
    const result = new Map();

    jad = jad.replace(/\r\n|\r/g, "\n");  // convert line endings

    jad.replace(/^\s*?([\w-]+)\s*\:\s+((.|\n )+?)$(?=\n[^ ])/gm, (_, key, value) => {
        if (result.has(key)) {
            throw new Error(`The JAD file has a duplicate attribute: "${key}"`);
        }
        result.set(key, value.replace(/\n /g, ""));
    })
    return result;
}

function createJad(map) {
    let result = "";

    map.forEach((value, key) => {
        value = value.toString();

        let availableCols = 68 - key.length;  // first line: 70 chars minus those used by the key and the ": " after it
        result += key + ":";

        while (value.length) {
            result += " " + value.slice(0, availableCols) + "\n";
            value = value.slice(availableCols);
            availableCols = 69;  // subsequent lines: 70 chars minus one leading space
        }
    })

    return result;
}

module.exports = {
    createJadFromJar,
    parseJad,
    createJad
}