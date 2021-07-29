package com.daedalus.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class CompletionType implements DataType<Map<String, List<String>>> {

  private static final String TERMS_PROPERTY = "input";

  @Override
  public Map<String, List<String>> parse(Object rawObject) throws IncorrectTypeException {
    Map<String, List<String>> properties = null;

    if (rawObject != null) {
      properties = new HashMap<>();
      try {
        if (rawObject instanceof String) {
          properties.put(TERMS_PROPERTY, convertToListOfTerms((String) rawObject));
        } else if (isListOfStrings(rawObject)) {
          properties.put(TERMS_PROPERTY, (List<String>) rawObject);
        } else {
          properties.put(TERMS_PROPERTY, Arrays.asList((String[]) rawObject));
        }
      } catch (ClassCastException e) {
        throw new IncorrectTypeException("Unable to parse " + rawObject + " to Completion");
      }
    }

    return properties;
  }

  private boolean isListOfStrings(final Object rawObject) {
    return rawObject instanceof List
        && ((List<?>) rawObject).stream().allMatch(String.class::isInstance);
  }

  private List<String> convertToListOfTerms(String data) {
    var completionTerms = new ArrayList<String>();
    if (!data.isEmpty()) {
      completionTerms.addAll(this.splitIntoTerms(data));
    }
    return completionTerms;
  }

  protected List<String> splitIntoTerms(final String data) {
    var splitWord = new LinkedList<>(List.of(data.split(" ")));
    var terms = new ArrayList<String>();

    int words = splitWord.size();
    for (var count = 0; count < words; count++) {
      terms.add(String.join(" ", splitWord));
      splitWord.removeFirst();
    }

    return terms;
  }

  @Override
  public boolean isA(final Object object) {
    var isCompletion = object instanceof String;

    if (object instanceof Map) {
      var mapOfProperties = (Map<?, ?>) object;
      isCompletion  = Optional.ofNullable(mapOfProperties.get(TERMS_PROPERTY))
          .map(v-> v instanceof String || this.isListOfStrings(v))
          .get();

    }else if(object instanceof List){
      isCompletion = this.isListOfStrings(object);

      if(!isCompletion){
        isCompletion = ((List<?>) object).stream().allMatch(this::isA);
      }
    }
    return isCompletion;
  }
}
