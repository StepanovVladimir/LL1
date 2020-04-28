package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main
{
    private static String axiom;

    public static void main(String[] args)
    {
        try
        {
            var grammar = readGrammar(args[0]);
            convertGrammar(grammar);
            var relationFirst = createRelationFirst(grammar);
            createGuideSets(grammar, relationFirst);
            printGrammar(grammar, args[1]);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static Map<String, ArrayList<Rule>> readGrammar(String fileName) throws IOException
    {
        var grammar = new TreeMap<String, ArrayList<Rule>>();
        for (String line : Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)
                .collect(Collectors.toList()))
        {
            Scanner scanner = new Scanner(line);
            String nontermChar = scanner.next();

            if (axiom == null)
            {
                axiom = nontermChar;
            }

            ArrayList<Rule> rules;
            if (grammar.containsKey(nontermChar))
            {
                rules = grammar.get(nontermChar);
            }
            else
            {
                rules = new ArrayList<>();
                grammar.put(nontermChar, rules);
            }

            Rule rule = new Rule();
            rules.add(rule);
            scanner.next();
            while (scanner.hasNext())
            {
                String str = scanner.next();
                rule.rule.add(str);
            }
        }

        return grammar;
    }

    private static void convertGrammar(Map<String, ArrayList<Rule>> grammar)
    {
        Map<String, ArrayList<Rule>> newNontermChars = new TreeMap<>();
        for (Map.Entry<String, ArrayList<Rule>> rules : grammar.entrySet())
        {
            boolean thereIsLeftRecursion = false;
            for (Rule rule : rules.getValue())
            {
                if (rules.getKey().equals(rule.rule.get(0)))
                {
                    thereIsLeftRecursion = true;
                    break;
                }
            }

            if (thereIsLeftRecursion)
            {
                String newNontermChar = "<" + newNontermChars.size() + ">";
                ArrayList<Rule> newRules1 = new ArrayList<>();
                ArrayList<Rule> newRules2 = new ArrayList<>();
                Rule emptyRule = new Rule();
                emptyRule.rule.add("@");
                newRules2.add(emptyRule);
                for (Rule rule : rules.getValue())
                {
                    if (!rules.getKey().equals(rule.rule.get(0)))
                    {
                        Rule newRule = new Rule();
                        newRule.rule.addAll(rule.rule);
                        newRule.rule.add(newNontermChar);
                        newRules1.add(newRule);
                    }
                    else
                    {
                        Rule newRule = new Rule();
                        newRule.rule.addAll(rule.rule);
                        newRule.rule.remove(0);
                        newRule.rule.add(newNontermChar);
                        newRules2.add(newRule);
                    }
                }
                rules.getValue().clear();
                rules.getValue().addAll(newRules1);
                newNontermChars.put(newNontermChar, newRules2);
            }

            String recurringChar = null;
            Set<String> firstChars = new TreeSet<>();
            for (Rule rule : rules.getValue())
            {
                if (!firstChars.contains(rule.rule.get(0)))
                {
                    firstChars.add(rule.rule.get(0));
                }
                else
                {
                    recurringChar = rule.rule.get(0);
                }
            }

            if (recurringChar != null)
            {
                ArrayList<Integer> recurringRulesIndexes = new ArrayList<>();
                for (int i = 0; i < rules.getValue().size(); ++i)
                {
                    if (rules.getValue().get(i).rule.get(0).equals(recurringChar))
                    {
                        recurringRulesIndexes.add(i);
                    }
                }

                int recurringLength = 0;
                boolean repetitionsAreOver = false;
                while (!repetitionsAreOver)
                {
                    ++recurringLength;
                    if (rules.getValue().get(recurringRulesIndexes.get(0)).rule.size() <= recurringLength)
                    {
                        break;
                    }
                    String str = rules.getValue().get(recurringRulesIndexes.get(0)).rule.get(recurringLength);
                    for (int i = 1; i < recurringRulesIndexes.size(); ++i)
                    {
                        if (rules.getValue().get(recurringRulesIndexes.get(i)).rule.size() <= recurringLength
                                || !rules.getValue().get(recurringRulesIndexes.get(i)).rule.get(recurringLength).equals(str))
                        {
                            repetitionsAreOver = true;
                            break;
                        }
                    }
                }

                String newNontermChar = "<" + newNontermChars.size() + ">";
                Rule newRule = new Rule();
                ArrayList<Rule> newRules = new ArrayList<>();
                for (int i = 0; i < recurringLength; ++i)
                {
                    newRule.rule.add(rules.getValue().get(recurringRulesIndexes.get(0)).rule.get(i));
                }
                newRule.rule.add(newNontermChar);

                for (Integer recurringRulesIndex : recurringRulesIndexes)
                {
                    if (rules.getValue().get(recurringRulesIndex).rule.size() == recurringLength)
                    {
                        Rule rule = new Rule();
                        rule.rule.add("@");
                        newRules.add(rule);
                    }
                    else
                    {
                        Rule rule = new Rule();
                        for (int j = recurringLength; j < rules.getValue().get(recurringRulesIndex).rule.size(); ++j)
                        {
                            rule.rule.add(rules.getValue().get(recurringRulesIndex).rule.get(j));
                        }
                        newRules.add(rule);
                    }
                }

                for (int i = recurringRulesIndexes.size() - 1; i >= 0; --i)
                {
                    rules.getValue().remove(recurringRulesIndexes.get(i).intValue());
                }
                rules.getValue().add(newRule);
                newNontermChars.put(newNontermChar, newRules);
            }
        }
        grammar.putAll(newNontermChars);
    }

    private static Map<String, Set<String>> createRelationFirst(Map<String, ArrayList<Rule>> grammar)
    {
        Map<String, Set<String>> relationFirst = new TreeMap<>();
        for (Map.Entry<String, ArrayList<Rule>> rules : grammar.entrySet())
        {
            Set<String> guideSet = new TreeSet<>();
            relationFirst.put(rules.getKey(), guideSet);
            for (Rule rule : rules.getValue())
            {
                String firstChar = rule.rule.get(0);
                if (!firstChar.equals("@"))
                {
                    rule.guideSet.add(firstChar);
                    guideSet.add(firstChar);
                }
                else
                {
                    if (rules.getKey().equals(axiom))
                    {
                        rule.guideSet.add("#");
                        guideSet.add("#");
                    }
                    Set<String> researchedNontermChars = new TreeSet<>();
                    researchedNontermChars.add(rules.getKey());
                    rule.guideSet.addAll(findNext(rules.getKey(), grammar, researchedNontermChars));
                    guideSet.addAll(rule.guideSet);
                }
            }
        }

        for (Map.Entry<String, Set<String>> guideSet : relationFirst.entrySet())
        {
            Set<String> termChars = new TreeSet<>();
            for (String str : guideSet.getValue())
            {
                if (isNontermChar(str))
                {
                    termChars.addAll(getTermChars(str, relationFirst));
                }
            }
            guideSet.getValue().addAll(termChars);
        }

        return relationFirst;
    }

    private static void createGuideSets(Map<String, ArrayList<Rule>> grammar, Map<String, Set<String>> relationFirst)
    {
        for (Map.Entry<String, ArrayList<Rule>> rules : grammar.entrySet())
        {
            for (Rule rule : rules.getValue())
            {
                Set<String> nontermChars = new TreeSet<>();
                Set<String> termChars = new TreeSet<>();
                for (String str : rule.guideSet)
                {
                    if (isNontermChar(str))
                    {
                        nontermChars.add(str);
                        for (String guideChar : relationFirst.get(str))
                        {
                            if (!isNontermChar(guideChar))
                            {
                                termChars.add(guideChar);
                            }
                        }
                    }
                }
                rule.guideSet.removeAll(nontermChars);
                rule.guideSet.addAll(termChars);
            }
        }
    }

    private static void printGrammar(Map<String, ArrayList<Rule>> grammar, String fileName) throws IOException
    {
        try (FileWriter writer = new FileWriter(fileName))
        {
            for (Rule rule : grammar.get(axiom))
            {
                writer.write(axiom + " - ");
                for (String str : rule.rule)
                {
                    writer.write(str + " ");
                }
                writer.write("/");
                for (String str : rule.guideSet)
                {
                    writer.write(" " + str);
                }
                writer.write("\n");
            }
            for (Map.Entry<String, ArrayList<Rule>> rules : grammar.entrySet())
            {
                if (!rules.getKey().equals(axiom))
                {
                    for (Rule rule : rules.getValue())
                    {
                        writer.write(rules.getKey() + " - ");
                        for (String str : rule.rule)
                        {
                            writer.write(str + " ");
                        }
                        writer.write("/");
                        for (String str : rule.guideSet)
                        {
                            writer.write(" " + str);
                        }
                        writer.write("\n");
                    }
                }
            }
        }
    }

    private static Set<String> findNext(String nontermChar, Map<String, ArrayList<Rule>> grammar, Set<String> researchedNontermChars)
    {
        Set<String> guideSet = new TreeSet<>();
        for (Map.Entry<String, ArrayList<Rule>> rules : grammar.entrySet())
        {
            for (Rule rule : rules.getValue())
            {
                for (int i = 0; i < rule.rule.size(); ++i)
                {
                    if (rule.rule.get(i).equals(nontermChar))
                    {
                        if (rule.rule.size() > i + 1)
                        {
                            guideSet.add(rule.rule.get(i + 1));
                        }
                        else if (!researchedNontermChars.contains(rules.getKey()))
                        {
                            if (rules.getKey().equals(axiom))
                            {
                                guideSet.add("#");
                            }
                            researchedNontermChars.add(rules.getKey());
                            guideSet.addAll(findNext(rules.getKey(), grammar, researchedNontermChars));
                            researchedNontermChars.remove(rules.getKey());
                        }
                    }
                }
            }
        }

        return guideSet;
    }

    private static Set<String> getTermChars(String nontermChar, Map<String, Set<String>> relationFirst)
    {
        Set<String> termChars = new TreeSet<>();
        for (String str : relationFirst.get(nontermChar))
        {
            if (!isNontermChar(str))
            {
                termChars.add(str);
            }
            else
            {
                termChars.addAll(getTermChars(str, relationFirst));
            }
        }

        return termChars;
    }

    private static boolean isNontermChar(String str)
    {
        return str.length() > 2 && str.charAt(0) == '<' && str.charAt(str.length() - 1) == '>';
    }
}
