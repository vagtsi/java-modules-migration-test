package javamodulesmigration.immutables;

import org.immutables.gson.Gson.TypeAdapters;
import org.immutables.value.Value.Immutable;

@Immutable
@TypeAdapters
public interface ExampleValue {
  String name();
}
