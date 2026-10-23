package com.badspell.formula;

import java.util.*;

/** Immutable graph revision. Positions are editor coordinates, edges define execution. */
public record Formula(UUID id, UUID owner, int revision, String title, List<Node> nodes,
                      List<Edge> edges, String output, long energyBudget) {
    public Formula {
        Objects.requireNonNull(id); Objects.requireNonNull(owner); Objects.requireNonNull(title); Objects.requireNonNull(output);
        nodes = List.copyOf(nodes); edges = List.copyOf(edges);
        if (revision < 1 || energyBudget < 0 || title.length() > 256) throw new IllegalArgumentException("formula metadata");
    }
    public record Node(String id, String rune, int x, int y, Map<String, Value> parameters) {
        public Node {
            Objects.requireNonNull(id); Objects.requireNonNull(rune);
            if (!id.matches("[a-zA-Z0-9_]{1,64}")) throw new IllegalArgumentException("node id");
            parameters = Map.copyOf(parameters);
        }
    }
    public record Edge(String from, String to, String port) {
        public Edge { Objects.requireNonNull(from); Objects.requireNonNull(to); Objects.requireNonNull(port); }
    }
}
