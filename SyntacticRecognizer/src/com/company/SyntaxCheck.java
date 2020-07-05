package com.company;

import java.util.List;
import java.util.Stack;

public class SyntaxCheck {
    List<Row> grammer;
    List<Token> in;
    Token _currLexem;
    Integer _currentTableIndex;
    Integer _currentWordIndex;
    Stack<Integer> stackIndex = new Stack<>();


    public SyntaxCheck(List<Row> grammer, List<Token> in) {
        this.grammer = grammer;
        this.in = in;
    }

    public String Run()
    {
        _currentTableIndex = 0;
        _currentWordIndex = 0;
        _currLexem = in.get(_currentWordIndex);
        return CheckWords();
    }

    private boolean CheckRow()
    {
        if (grammer.get(_currentTableIndex).directionSet.contains(_currLexem.value)) {
            return true;
        } else {
            return grammer.get(_currentTableIndex).directionSet.contains("[" + _currLexem.tokentype.toString() + "]");
        }
    }

    private String CheckWords()
    {
        while (true) {
            if (CheckRow())  // проверяем можно ли обрабатывать строку в таблице
            {
                isShift();
                isStack();

                if (grammer.get(_currentTableIndex).dirNum != 0) { // переходим по dirNum
                    _currentTableIndex = grammer.get(_currentTableIndex).dirNum - 1;
                } else if (grammer.get(_currentTableIndex).dirNum == 0 && stackIndex.size() > 0) { // переходим по стеку, если нельзя по dirNum
                    _currentTableIndex = stackIndex.lastElement();
                    stackIndex.remove(stackIndex.size() - 1);
                } else if (stackIndex.size() == 0 && grammer.get(_currentTableIndex).isEnd == 1) {
                    return "OK";
                }

            } else if (grammer.get(_currentTableIndex).error == -1) {
                _currentTableIndex++;
            } else {
                System.out.println("Ошибка в " + _currLexem.line + ":" + _currLexem.pos);
                System.out.println("Ожидалось: " + grammer.get(_currentTableIndex).directionSet);
                System.out.println("Встретился: " + _currLexem.value);
                return "ERROR";
            }
        }
    }

    private void isShift()
    {
        if (grammer.get(_currentTableIndex).shift == -1)
        {
            return;
        }

        _currentWordIndex++;
        if (_currentWordIndex > in.size() - 1)
        {
            _currLexem = null;
            return;
        }

        _currLexem = in.get(_currentWordIndex);
    }

    private void isStack()
    {
        if (grammer.get(_currentTableIndex).stack == -1)
        {
            return;
        }
        stackIndex.push( _currentTableIndex + 1 );
    }
}