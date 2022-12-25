package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetArithmeticParser<T> {

  // castle&!(field|forest) ->

  public static Token OPEN = new Token(Pattern.compile("\\("));
  public static Token CLOSE = new Token(Pattern.compile("\\)"));
  public static Token AND = new Token(Pattern.compile("&"));
  public static Token OR = new Token(Pattern.compile("\\|"));
  public static Token NOT = new Token(Pattern.compile("!"));
  private static final List<Token> OPERATORS = Lists.newArrayList(AND, OR, NOT);
  public static Token GROUP = new Token(Pattern.compile("[a-zA-Z0-9-_.%]+"));
  private static final Token[] TOKENS = {OPEN, CLOSE, AND, OR, NOT, GROUP};
  private final Collection<T> scope;
  private final Function<T, Collection<String>> searchTermSupplier;
  private final Map<String, Collection<T>> cache;
  public SetArithmeticParser(Collection<T> scope,
                             Function<T, Collection<String>> searchTermSupplier) {
    this.scope = scope;
    this.searchTermSupplier = searchTermSupplier;
    this.cache = new HashMap<>();
  }

  public List<TokenMatch> tokenize(String input) {

    List<TokenMatch> tokens = new ArrayList<>();
    int index = 0;
    int length = input.length();
    String subString;

    while (index < length) {

      subString = input.substring(index);
      int currentIndex = index;

      for (Token token : TOKENS) {
        MatchResult match = token.match(subString);
        if (match != null && match.start() == 0) {
          tokens.add(new TokenMatch(token, subString.substring(match.start(), match.end())));
          index += match.end();
          subString = input.substring(index);
        }
      }
      if (currentIndex == index) {
        throw new IllegalArgumentException(
            "Could not parse '" + input + "', containing invalid character.");
      }
    }
    return tokens;
  }

  private int precedence(Token operator) {
    return operator.equals(NOT) ? 5 : operator.equals(AND) ? 3 : 1;
  }

  public List<TokenMatch> toRPN(List<TokenMatch> matches) {
    ArrayList<TokenMatch> out = new ArrayList<>();
    Stack<TokenMatch> stack = new Stack<>();

    for (TokenMatch token : matches) {
      Token t = token.token();
      if (OPERATORS.contains(t)) {
        while (!stack.isEmpty() && OPERATORS.contains(stack.peek().token())) {
          if (precedence(t) < precedence(stack.peek().token())) {
            out.add(stack.pop());
            continue;
          }
          break;
        }
        stack.push(token);
      } else if (t.equals(OPEN)) {
        stack.push(token);
      } else if (t.equals(CLOSE)) {
        while (!stack.empty() && !stack.peek().token().equals(OPEN)) {
          out.add(stack.pop());
        }
        stack.pop();
      } else {
        out.add(token);
      }
    }
    while (!stack.isEmpty()) {
      out.add(stack.pop());
    }
    return out;
  }

  public Collection<T> evaluateRPN(List<TokenMatch> matches) {
    cache.clear();
    Stack<Collection<T>> stack = new Stack<>();
    for (TokenMatch token : matches) {
      if (!OPERATORS.contains(token.token())) {
        stack.push(cache.computeIfAbsent(token.match(), s -> scope.stream()
            .filter(t -> searchTermSupplier.apply(t).contains(token.match()))
            .toList()));
      } else if (token.token().equals(NOT)) {
        Collection<T> a = stack.pop();
        Collection<T> res = new ArrayList<>(scope);
        res.removeAll(a);
        stack.push(res);
      } else {
        Collection<T> a = stack.pop();
        Collection<T> b = stack.pop();

        Collection<T> res = new ArrayList<>(a);
        if (token.token().equals(AND)) {
          res.removeIf(t -> !b.contains(t));
        } else {
          res.addAll(b);
        }
        stack.push(res);
      }
    }
    return stack.pop();
  }

  public Collection<T> parse(String input) {
    return evaluateRPN(toRPN(tokenize(input)));
  }

  public record Token(Pattern pattern) {
    MatchResult match(String s) {
      Matcher m = pattern.matcher(s);
      return m.find() ? m.toMatchResult() : null;
    }
  }

  public record TokenMatch(Token token, String match) {

  }
}
