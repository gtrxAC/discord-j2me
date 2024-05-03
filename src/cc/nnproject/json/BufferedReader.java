/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/


package cc.nnproject.json;

import java.io.IOException;
import java.io.Reader;

/**
 * Buffered wrapper for Readers.
 *
 * @see java.io.BufferedReader
 */
public class BufferedReader extends Reader
{
    /** Default buffer size. */
    private static final int BUF_SIZE = 16384;

    /** Reader given in the constructor. */
    private Reader iReader = null;

    /** Character buffer. */
    private char[] iBuf = null;

    /** Amount of characters in the buffer.
        Value must be between zero and iBuf.length. */
    private int iBufAmount = 0;

    /** Current read position in the buffer.
        Value must be between zero and iBuf.length. */
    private int iBufPos = 0;

    /**
     * @see java.io.BufferedReader#Constructor(java.io.Reader)
     */
    public BufferedReader(Reader aIn)
    {
        this(aIn, BUF_SIZE);
    }

    /**
     * @see java.io.BufferedReader#Constructor(java.io.Reader, int)
     */
    public BufferedReader(Reader aIn, int aSize)
    {
        if (aSize <= 0)
        {
            throw new IllegalArgumentException(
                "BufferedReader: Invalid buffer size");
        }
        iBuf = new char[aSize];
        iReader = aIn;
    }

    /**
     * @see java.io.BufferedReader#close()
     */
    public void close() throws IOException
    {
        iBuf = null;
        iBufAmount = 0;
        iBufPos = 0;
        if (iReader != null)
        {
            iReader.close();
        }
    }

    /**
     * @see java.io.BufferedReader#read()
     */
    public int read() throws IOException
    {
        int result = 0;
        if (iBufPos >= iBufAmount)
        {
            result = fillBuf();
        }
        if (result > -1)
        {
            result = iBuf[iBufPos++];
        }
        return result;
    }

    /**
     * @see java.io.BufferedReader#read(char[])
     */
    public int read(char[] aBuf) throws IOException
    {
        return read(aBuf, 0, aBuf.length);
    }

    /**
     * @see java.io.BufferedReader#read(char[], int, int)
     */
    public int read(char[] aBuf, int aOffset, int aLength) throws IOException
    {
        if (aOffset < 0 || aOffset >= aBuf.length)
        {
            throw new IllegalArgumentException(
                "BufferedReader: Invalid buffer offset");
        }
        int charsToRead = aBuf.length - aOffset;
        if (charsToRead > aLength)
        {
            charsToRead = aLength;
        }
        int bufCharCount = iBufAmount - iBufPos;
        int readCount = 0;
        if (charsToRead <= bufCharCount)
        {
            // All characters can be read from the buffer.
            for (int i = 0; i < charsToRead; i++)
            {
                aBuf[aOffset+i] = iBuf[iBufPos++];
            }
            readCount += charsToRead;
        }
        else
        {
            // First read characters from the buffer,
            // then read more characters from the Reader.
            for (int i = 0; i < bufCharCount; i++)
            {
                aBuf[aOffset+i] = iBuf[iBufPos++];
            }
            readCount += bufCharCount;
            // Whole buffer has now been read, fill the buffer again.
            if (fillBuf() > -1)
            {
                // Read the remaining characters.
                readCount += read(aBuf, aOffset+readCount, aLength-readCount);
            }
        }
        if (readCount <= 0)
        {
            // Nothing has been read, return -1 to indicate end of stream.
            readCount = -1;
        }
        return readCount;
    }

    /**
     * @see java.io.BufferedReader#readLine()
     */
    public String readLine() throws IOException
    {
        if (!ensureBuf())
        {
            // End of stream has been reached.
            return null;
        }
        StringBuffer line = new StringBuffer();
        while (ensureBuf())
        {
            if (skipEol())
            {
                // End of line found.
                break;
            }
            else
            {
                // Append characters to result line.
                line.append(iBuf[iBufPos++]);
            }
        }
        return line.toString();
    }

    /**
     * @see java.io.BufferedReader#ready()
     */
    public boolean ready() throws IOException
    {
        if (iBufPos < iBufAmount)
        {
            return true;
        }
        if (iReader != null)
        {
            return iReader.ready();
        }
        return false;
    }

    /**
     * @see java.io.BufferedReader#skip()
     */
    public long skip(long aAmountToSkip) throws IOException
    {
        if (aAmountToSkip < 0)
        {
            throw new IllegalArgumentException(
                "BufferedReader: Cannot skip negative amount of characters");
        }
        long skipped = 0;
        int bufCharCount = iBufAmount - iBufPos;
        if (aAmountToSkip <= bufCharCount)
        {
            // There is enough characters in buffer to skip.
            iBufPos += aAmountToSkip;
            skipped += aAmountToSkip;
        }
        else
        {
            // First skip characters that are available in the buffer,
            // then skip characters from the Reader.
            iBufPos += bufCharCount;
            skipped += bufCharCount;
            if (iReader != null)
            {
                skipped += iReader.skip(aAmountToSkip - skipped);
            }
        }
        return skipped;
    }

    /**
     * If current read position in the buffer is end of line,
     * move position over end of line and return true, otherwise
     * return false. Also in the end of stream case this method
     * returns true.
     */
    private boolean skipEol() throws IOException
    {
        if (!ensureBuf())
        {
            // End of stream has been reached.
            return true;
        }
        boolean eolFound = false;
        if (iBufAmount > iBufPos && iBuf[iBufPos] == '\r')
        {
            iBufPos += 1;
            eolFound = true;
            ensureBuf();
        }
        if (iBufAmount > iBufPos && iBuf[iBufPos] == '\n')
        {
            iBufPos += 1;
            eolFound = true;
        }
        return eolFound;
    }

    /**
     * Ensures that the buffer has characters to read.
     *
     * @return True if the buffer has characters to read,
     * false if end of stream has been reached.
     */
    private boolean ensureBuf() throws IOException
    {
        boolean result = true;
        if (iBufPos >= iBufAmount)
        {
            if (fillBuf() == -1)
            {
                result = false;
            }
        }
        return result;
    }

    /**
     * Fills the buffer from the Reader and resets the buffer counters.
     *
     * @return The number of characters read, or -1 if the end of
     * stream has been reached.
     */
    private int fillBuf() throws IOException
    {
        if (iReader == null)
        {
            return -1;
        }
        // Fill the buffer.
        int readCount = iReader.read(iBuf);
        if (readCount > -1)
        {
            // Reset the buffer counters only if reading succeeded.
            iBufAmount = readCount;
            iBufPos = 0;
        }
        return readCount;
    }
}
