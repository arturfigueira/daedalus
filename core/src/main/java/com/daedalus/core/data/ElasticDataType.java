package com.daedalus.core.data;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ElasticDataType {
  BOOLEAN(BooleanType::new),
  COMPLETION(CompletionType::new),
  KEYWORD(TextType::new),
  DATE(DateType::new),
  TEXT(TextType::new),
  LONG(() -> new NumberType<>(Long.class)),
  INTEGER(() -> new NumberType<>(Integer.class)),
  SHORT(() -> new NumberType<>(Short.class)),
  BYTE(() -> new NumberType<>(Byte.class)),
  DOUBLE(() -> new NumberType<>(Double.class)),
  FLOAT(() -> new NumberType<>(Float.class)),
  IP(IpType::new);

  private final Supplier<DataType<?>> supplier;

  DataType<?> produce() {
    return this.supplier.get();
  }

  @Override
  public String toString() {
    return "DataType{" + this.name() + "}";
  }
}
