package com.folone.replbf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Brainfuck {

    InputStream input;
    PrintStream output;

    ArrayList infiniteTape;
    int headPosition;

    Brainfuck(InputStream in, PrintStream out) {
        input = in;
        output = out;

        infiniteTape = new ArrayList();

        for (int i = 0; i != 50; i++)
            infiniteTape.add(new Byte((byte) 0x00));

        headPosition = 0;
    }

    void interpret(byte[] code) throws IOException {

        int instructionPointer = 0;

        Stack nestedLoops = new Stack();

        while (instructionPointer != code.length)
            switch (code[instructionPointer]) {

            case '>':
                headPosition++;
                if (headPosition >= infiniteTape.size())
                    stretchTapeEnd();
                instructionPointer++;
                break;
            case '<':
                headPosition--;
                if (headPosition <= 0)
                    stretchTapeStart();
                instructionPointer++;
                break;
            case '+':
                setSymbol((byte) (getSymbol() + 0x01));
                instructionPointer++;
                break;
            case '-':
                setSymbol((byte) (getSymbol() - 0x01));
                instructionPointer++;
                break;
            case ',':
                setSymbol((byte) input.read());
                instructionPointer++;
                break;
            case '.':
                output.print((char) getSymbol());
                instructionPointer++;
                break;
            case '[':
                if (getSymbol() != 0x00) {
                    nestedLoops.push(new Integer(++instructionPointer));
                } else
                    instructionPointer = skipLoop(code, instructionPointer);
                break;
            case ']':
                if (nestedLoops.size() == 0)
                    throw new IOException("mismatched ]");

                if (getSymbol() != (byte) 0x00) {
                    instructionPointer = ((Integer) nestedLoops.peek())
                            .intValue();
                } else {
                    nestedLoops.pop();
                    instructionPointer++;
                }
                break;
            default:
                instructionPointer++;
                break;
            }
    }

    byte getSymbol() {
        Byte symbol = (Byte) infiniteTape.get(headPosition);
        return symbol.byteValue();
    }

    void setSymbol(byte value) {
        Byte symbol = new Byte(value);
        infiniteTape.set(headPosition, symbol);
    }

    void stretchTapeEnd() {
        for (int i = 0; i != 20; i++)
            infiniteTape.add(new Byte((byte) 0x00));
    }

    void stretchTapeStart() {
        for (int i = 0; i != 20; i++)
            infiniteTape.add(0, new Byte((byte) 0x00));
        headPosition += 20;
    }

    int skipLoop(byte[] code, int from) {
        int subLoops = 0;

        do
            switch (code[++from]) {
            case '[':
                subLoops++;
                break;
            case ']':
                subLoops--;
                break;
            default:
                break;
            }
        while (subLoops != -1);

        return ++from;
    }

    public static String evaluate(String script) throws IOException {

        if (!Pattern.matches("^(\\-*\\+*\\<*\\>*\\[*\\]*\\.*,*)+$", script)) {
            return "Not a valid BF code, should only contain: +-><[].,";
        }
        byte[] code;

        code = new byte[script.length()];

        InputStream fileIn = new ByteArrayInputStream(script.getBytes());

        for (int i = 0; i != code.length; i++)
            code[i] = (byte) fileIn.read();

        fileIn.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream os = new PrintStream(baos);

        Brainfuck interpreter = new Brainfuck(System.in, os);
        interpreter.interpret(code);

        return baos.toString();
    }

}