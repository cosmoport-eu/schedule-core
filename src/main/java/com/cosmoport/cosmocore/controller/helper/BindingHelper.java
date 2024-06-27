package com.cosmoport.cosmocore.controller.helper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.function.Predicate.not;

public final class BindingHelper {
    private BindingHelper() {
    }

    public static <T, A> void updateAttributes(List<A> newAttributes,
                                               T event,
                                               Function<T, Collection<A>> attributesGetter,
                                               Function<A, Set<T>> eventGetter) {
        final Collection<A> oldAttributes = attributesGetter.apply(event);

        final List<A> attributesToAdd = newAttributes.stream()
                .filter(not(oldAttributes::contains))
                .toList();

        final List<A> attributesToDelete = oldAttributes.stream()
                .filter(not(newAttributes::contains))
                .toList();

        attributesToAdd.forEach(attributeToAdd -> {
            eventGetter.apply(attributeToAdd).add(event);
            attributesGetter.apply(event).add(attributeToAdd);
        });

        attributesToDelete.forEach(attributeToDelete -> {
            eventGetter.apply(attributeToDelete).remove(event);
            attributesGetter.apply(event).remove(attributeToDelete);
        });
    }
}
