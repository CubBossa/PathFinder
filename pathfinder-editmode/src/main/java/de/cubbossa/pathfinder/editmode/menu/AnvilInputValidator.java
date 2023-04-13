package de.cubbossa.pathfinder.editmode.menu;

import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.translations.Message;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;

@Getter
@RequiredArgsConstructor
public class AnvilInputValidator<T> {

  private static final Predicate<String> PREDICATE_PERMISSION = s -> !s.trim().contains(" ");
  public static final AnvilInputValidator<String> VALIDATE_PERMISSION =
      new AnvilInputValidator<>(Messages.ERROR_PARSE_STRING,
          Component.text("<permission> (no spaces)"), PREDICATE_PERMISSION, String::trim);
  private static final Predicate<String> PREDICATE_PERCENT =
      s -> Pattern.matches("^-?[0-9]+([,.][0-9]{0,3})?%$", s.trim());
  public static final AnvilInputValidator<Double> VALIDATE_PERCENT =
      new AnvilInputValidator<>(Messages.ERROR_PARSE_PERCENT,
          Component.text("<float value>%"), PREDICATE_PERCENT,
          s -> Double.parseDouble(s.replace("%", "").trim()));
  private static final Predicate<String> PREDICATE_INT = s -> Pattern.matches("[0-9]+", s.trim());
  public static final AnvilInputValidator<Integer> VALIDATE_INT =
      new AnvilInputValidator<>(Messages.ERROR_PARSE_INTEGER,
          Component.text("Integer"), PREDICATE_INT, s -> Integer.parseInt(s.trim()));
  private static final Predicate<String> PREDICATE_FLOAT =
      s -> Pattern.matches("null|[0-9]+[,.][0-9]*", s.trim());
  public static final AnvilInputValidator<Double> VALIDATE_FLOAT =
      new AnvilInputValidator<>(Messages.ERROR_PARSE_DOUBLE,
          Component.text("Float"), PREDICATE_FLOAT, s -> Double.parseDouble(s.trim()));
  private static final Predicate<String> PREDICATE_KEY =
      s -> Pattern.matches("([a-zA-Z-_]:)?[a-zA-Z-_]", s.trim());
  public static final AnvilInputValidator<NamespacedKey> VALIDATE_KEY =
      new AnvilInputValidator<>(Messages.ERROR_PARSE_KEY,
          Component.text("[<namespace>:]<key>"), PREDICATE_KEY,
          s -> NamespacedKey.fromString(s, PathPlugin.getInstance()));


  private final Message errorMessage;
  private final ComponentLike requiredFormat;
  private final Predicate<String> inputValidator;
  private final Function<String, T> inputParser;
}
