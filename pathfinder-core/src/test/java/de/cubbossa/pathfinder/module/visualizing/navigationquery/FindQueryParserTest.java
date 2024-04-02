package de.cubbossa.pathfinder.module.visualizing.navigationquery;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.cubbossa.pathapi.visualizer.query.SearchQueryAttribute;
import de.cubbossa.pathapi.visualizer.query.SearchTerm;
import de.cubbossa.pathapi.visualizer.query.SearchTermHolder;
import de.cubbossa.pathfinder.navigationquery.FindQueryException;
import de.cubbossa.pathfinder.navigationquery.FindQueryParser;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FindQueryParserTest {

  private static final SearchTermHolder GROUP_PARKING = new SimpleSearchTermHolder(Sets.newHashSet(
      new SimpleSearchTerm("parking"),
      new SimpleSearchTerm("parkinglot"),
      new SimpleSearchTerm("empty")
  ));

  private static final SearchTermHolder GROUP_CLASSROOM =
      new SimpleSearchTermHolder(Sets.newHashSet(
          new SimpleSearchTerm("classroom"),
          new SimpleSearchTerm("class"),
          new SimpleSearchTerm("school"),
          new SimpleSearchTerm("public"),
          new SimpleSearchTerm("empty")
      ));

  private static final SearchTermHolder GROUP_SHOP = new SimpleSearchTermHolder(Sets.newHashSet(
      new SimpleSearchTerm("shop", Map.of(
          "eq", (comparator, o) -> comparator.equals(SearchQueryAttribute.Comparator.EQUALS)
              && o.toString().equals("val"),
          "gt", (comparator, o) -> comparator.equals(SearchQueryAttribute.Comparator.GREATER_THAN)
              && !o.toString().equals("xyz")
      )),
      new SimpleSearchTerm("buy"),
      new SimpleSearchTerm("sell"),
      new SimpleSearchTerm("public")
  ));

  private static final List<SearchTermHolder> scope = List.of(
      GROUP_PARKING,
      GROUP_CLASSROOM,
      GROUP_SHOP
  );

  @Test
  void testParse1() {

    Assertions.assertEquals(Lists.newArrayList(GROUP_CLASSROOM),
        new FindQueryParser().parse("classroom", scope));
    Assertions.assertEquals(Lists.newArrayList(GROUP_CLASSROOM),
        new FindQueryParser().parse("class", scope));
    Assertions.assertEquals(Lists.newArrayList(GROUP_CLASSROOM),
        new FindQueryParser().parse("school", scope));
    Assertions.assertEquals(Lists.newArrayList(GROUP_PARKING, GROUP_CLASSROOM),
        new FindQueryParser().parse("!shop", scope));
    Assertions.assertEquals(Lists.newArrayList(GROUP_PARKING, GROUP_CLASSROOM),
        new FindQueryParser().parse("!buy", scope));
    Assertions.assertEquals(Lists.newArrayList(GROUP_PARKING, GROUP_CLASSROOM),
        new FindQueryParser().parse("!sell", scope));
  }

  @Test
  void testParse2() {

    Assertions.assertEquals(
        Sets.newHashSet(GROUP_CLASSROOM, GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("public", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_CLASSROOM, GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("shop | school", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(),
        Sets.newHashSet(new FindQueryParser().parse("shop &school", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("shop & !school", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_PARKING, GROUP_CLASSROOM, GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("!shop | !school", scope))
    );
  }

  @Test
  void testParse3() {
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("shop[eq=val]", scope))
    );
    // must not find group because eq must be equals "val"
    Assertions.assertEquals(
        Sets.newHashSet(),
        Sets.newHashSet(new FindQueryParser().parse("shop[eq=Val]", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("shop[gt>=XYZ]", scope))
    );
    // must not find group because eq must not be equals "xyz"
    Assertions.assertEquals(
        Sets.newHashSet(),
        Sets.newHashSet(new FindQueryParser().parse("shop[gt>=xyz]", scope))
    );
  }

  @Test
  void testParse4() {
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("(((shop)))", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("!(!(!(!shop)))", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_CLASSROOM),
        Sets.newHashSet(new FindQueryParser().parse("empty & public", scope))
    );
    Assertions.assertEquals(
        Sets.newHashSet(GROUP_SHOP),
        Sets.newHashSet(new FindQueryParser().parse("(shop | none) & public", scope))
    );
  }


  @Getter
  @AllArgsConstructor
  public static class SimpleSearchTerm implements SearchTerm {

    private final String identifier;
    private Map<String, BiFunction<SearchQueryAttribute.Comparator, Object, Boolean>>
        attributeHandler;

    public SimpleSearchTerm(String identifier) {
      this(identifier, new HashMap<>());
    }

    @Override
    public boolean matches(Collection<SearchQueryAttribute> attributes) {
      return attributes.stream().allMatch(attribute -> {
        BiFunction<SearchQueryAttribute.Comparator, Object, Boolean> func =
            attributeHandler.get(attribute.identifier());
        if (func == null) {
          throw new FindQueryException("Unknown attribute '" + attribute.identifier() + "'");
        }
        return func == null || func.apply(attribute.comparator(), attribute.value());
      });
    }

    @Override
    public int hashCode() {
      return identifier.hashCode();
    }

    @Override
    public String toString() {
      return identifier;
    }
  }

  @Getter
  @RequiredArgsConstructor
  public static class SimpleSearchTermHolder implements SearchTermHolder {

    private final HashSet<SearchTerm> searchTerms;

    @Override
    public void addSearchTerms(Collection<SearchTerm> searchTerms) {
      this.searchTerms.addAll(searchTerms);
    }

    @Override
    public void removeSearchTerms(Collection<SearchTerm> searchTerms) {
      this.searchTerms.removeAll(searchTerms);
    }

    @Override
    public void clearSearchTerms() {
      this.searchTerms.clear();
    }

    @Override
    public boolean matches(SearchTerm searchTerm) {
      return searchTerms.contains(searchTerm);
    }

    @Override
    public boolean matches(SearchTerm searchTerm, Collection<SearchQueryAttribute> attributes) {
      return searchTerms.contains(searchTerm) && searchTerm.matches(attributes);
    }

    @Override
    public boolean matches(String term) {
      return searchTerms.stream().anyMatch(searchTerm -> searchTerm.getIdentifier().equals(term));
    }

    @Override
    public boolean matches(String term, Collection<SearchQueryAttribute> attributes) {
      return searchTerms.stream().anyMatch(
          searchTerm -> searchTerm.getIdentifier().equals(term) && searchTerm.matches(attributes));
    }

    @Override
    public String toString() {
      return searchTerms.toString();
    }
  }
}